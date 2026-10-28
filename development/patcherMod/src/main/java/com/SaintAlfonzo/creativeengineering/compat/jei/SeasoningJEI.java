package com.SaintAlfonzo.creativeengineering.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.SaintAlfonzo.creativeengineering.compat.jei.SeasoningCategory.Display;
import com.SaintAlfonzo.creativeengineering.food.SeasoningRecipe;
import com.SaintAlfonzo.creativeengineering.food.Seasonings;
import com.mojang.logging.LogUtils;

import io.github.mortuusars.salt.Salting;
import io.github.mortuusars.salt.crafting.recipe.SaltingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Moves seasoning crafts out of the Crafting and Mixing pages onto one Seasoning page ({@link SeasoningCategory}):
 * <ol>
 *   <li>Salt's {@code salt:salting} special craft (Salt shows it as a Crafting extension): one page, any
 *       {@code #salt:can_be_salted} food + salt, food and salted food cycling together;</li>
 *   <li>Create: The Salt's mixes (food + salt -> the same food with {@code Salted}): one page with the mixer;</li>
 *   <li>the pack's seasoning crafts ({@link SeasoningRecipe}, special, so never on the Crafting page) and crafting
 *       recipes with ids {@code creativeengineering:shapeless/season/...}, a page each.</li>
 * </ol>
 * {@link #onRuntimeAvailable} hides exactly what was re-shown. Gated by {@code [jei] seasoningCategory}; re-run on
 * every JEI reload.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class SeasoningJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, "seasoning");
    private static final ResourceLocation SALT = new ResourceLocation("salt", "salt");
    private static final ResourceLocation SALT_DUST = new ResourceLocation("vintagedelight", "salt_dust");
    private static final ResourceLocation MIXER = new ResourceLocation("create", "mechanical_mixer");
    private static final ResourceLocation CREATE_MIXING = new ResourceLocation("create", "mixing");
    private static final String SEASON_PREFIX = "shapeless/season/";

    /** Recipe ids now on the Seasoning page, per page they came from (set by the last reload). */
    private Set<ResourceLocation> movedCrafting = Set.of();
    private Set<ResourceLocation> movedMixing = Set.of();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // The Salt's salt is merged into Vintage Delight's salt dust (food/seasoning #6)
        ItemStack icon = stack(SALT_DUST);
        if (icon.isEmpty()) icon = stack(SALT);
        registration.addRecipeCategories(new SeasoningCategory(registration.getJeiHelpers().getGuiHelper(),
                icon.isEmpty() ? new ItemStack(Items.BREAD) : icon));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), SeasoningCategory.TYPE);
        ItemStack mixer = stack(MIXER);
        if (!mixer.isEmpty()) {
            registration.addRecipeCatalyst(mixer, SeasoningCategory.TYPE);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        movedCrafting = Set.of();
        movedMixing = Set.of();
        if (Minecraft.getInstance().level == null || !CEConfig.JEI.seasoningCategory.get()) {
            return;
        }
        try {
            RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
            ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
            List<Display> displays = new ArrayList<>();
            Set<ResourceLocation> crafting = new HashSet<>();
            Set<ResourceLocation> mixing = new HashSet<>();
            boolean salt = ModList.get().isLoaded("salt");

            for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
                Display display = recipe instanceof SeasoningRecipe seasoning ? pack(seasoning, table) : null;
                if (display == null && salt) {
                    display = SaltPages.salting(recipe, table);
                }
                if (display == null && isPackSeasoning(recipe)) {
                    display = plain(recipe, table);
                }
                if (display != null) {
                    displays.add(display);
                    crafting.add(recipe.getId());
                }
            }

            if (salt) {
                Display mixes = SaltPages.mixing(manager, stack(MIXER), mixing);
                if (mixes != null) {
                    displays.add(mixes);
                }
            }

            registration.addRecipes(SeasoningCategory.TYPE, displays);
            movedCrafting = crafting;
            movedMixing = mixing;
            LOGGER.info("[jei] Seasoning page: {} page(s), moved {} crafting and {} mixing recipe(s)",
                    displays.size(), crafting.size(), mixing.size());
        } catch (Throwable t) {
            movedCrafting = Set.of();
            movedMixing = Set.of();
            LOGGER.warn("[jei] couldn't build the Seasoning page, leaving seasoning crafts where they are", t);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        try {
            IRecipeManager recipes = runtime.getRecipeManager();
            int hidden = 0;
            if (!movedCrafting.isEmpty()) {
                List<CraftingRecipe> hide = recipes.createRecipeLookup(RecipeTypes.CRAFTING).includeHidden().get()
                        .filter(r -> movedCrafting.contains(r.getId()))
                        .toList();
                if (!hide.isEmpty()) {
                    recipes.hideRecipes(RecipeTypes.CRAFTING, hide);
                }
                hidden += hide.size();
            }
            if (!movedMixing.isEmpty()) {
                Optional<mezz.jei.api.recipe.RecipeType<?>> type = recipes.getRecipeType(CREATE_MIXING);
                if (type.isPresent()) {
                    hidden += hideById(recipes, type.get(), movedMixing);
                }
            }
            LOGGER.info("[jei] hid {} seasoning recipe(s) from the Crafting and Mixing pages", hidden);
        } catch (Throwable t) {
            LOGGER.warn("[jei] couldn't hide seasoning recipes from the Crafting and Mixing pages", t);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> int hideById(IRecipeManager recipes, mezz.jei.api.recipe.RecipeType<T> type, Set<ResourceLocation> ids) {
        List<T> hide = recipes.createRecipeLookup(type).includeHidden().get()
                .filter(r -> r instanceof Recipe<?> recipe && ids.contains(recipe.getId()))
                .toList();
        if (!hide.isEmpty()) {
            recipes.hideRecipes(type, hide);
        }
        return hide.size();
    }

    /** A pack seasoning craft: {@code creativeengineering:shapeless/season/...}, short enough for the page. */
    private static boolean isPackSeasoning(CraftingRecipe recipe) {
        ResourceLocation id = recipe.getId();
        int size = recipe.getIngredients().size();
        return id.getNamespace().equals(CreativeEngineering.MODID) && id.getPath().startsWith(SEASON_PREFIX)
                && size >= 2 && size <= SeasoningCategory.MAX_INPUTS;
    }

    /** A {@link SeasoningRecipe} page: every food it takes, seasoned in step; null if it matches nothing or is too long. */
    private static Display pack(SeasoningRecipe recipe, ItemStack machine) {
        // a pair craft's food ingredient is the first seasoning's tag; keep the foods both take
        List<ItemStack> foods = Arrays.stream(recipe.getFood().getItems())
                .filter(food -> Seasonings.accepts(food, recipe.seasonings())).toList();
        if (foods.isEmpty() || recipe.getParts().size() + 1 > SeasoningCategory.MAX_INPUTS) {
            return null;
        }
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(foods);
        for (SeasoningRecipe.Part part : recipe.getParts()) {
            inputs.add(Arrays.asList(part.ingredient().getItems()));
        }
        List<ItemStack> outputs = new ArrayList<>(foods.size());
        for (ItemStack food : foods) {
            outputs.add(Seasonings.with(food.copy(), recipe.seasonings()));
        }
        return new Display(recipe.getId(), machine, inputs, outputs, true);
    }

    private static Display plain(CraftingRecipe recipe, ItemStack machine) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(Arrays.asList(ingredient.getItems()));
        }
        return new Display(recipe.getId(), machine, inputs, List.of(recipe.getResultItem()), false);
    }

    private static ItemStack stack(ResourceLocation id) {
        return ForgeRegistries.ITEMS.containsKey(id) ? new ItemStack(ForgeRegistries.ITEMS.getValue(id)) : ItemStack.EMPTY;
    }

    /** Everything that touches Salt's classes; only loaded when Salt is. */
    private static final class SaltPages {

        /** Salt's salting craft as one page, or null if the recipe isn't one (or matches nothing). */
        static Display salting(CraftingRecipe recipe, ItemStack machine) {
            if (!(recipe instanceof SaltingRecipe salting)) {
                return null;
            }
            List<ItemStack> foods = Arrays.asList(salting.getFoodIngredient().getItems());
            if (foods.isEmpty() || (foods.size() == 1 && foods.get(0).is(Items.BARRIER))) {
                return null;
            }
            List<List<ItemStack>> inputs = new ArrayList<>();
            inputs.add(foods);
            for (Ingredient ingredient : salting.getIngredients()) {
                inputs.add(Arrays.asList(ingredient.getItems()));
            }
            if (inputs.size() > SeasoningCategory.MAX_INPUTS) {
                return null;
            }
            return new Display(recipe.getId(), machine, inputs, salted(foods), true);
        }

        /**
         * Create: The Salt's mixes (food + salt -> the same food, {@code Salted}) folded into one mixer page. Adds
         * their ids to {@code moved}; null if there are none or Create isn't loaded.
         */
        static Display mixing(RecipeManager manager, ItemStack mixer, Set<ResourceLocation> moved) {
            if (mixer.isEmpty()) {
                return null;
            }
            List<ItemStack> foods = new ArrayList<>();
            List<ItemStack> salts = List.of();
            for (Recipe<?> recipe : manager.getRecipes()) {
                ResourceLocation type = ForgeRegistries.RECIPE_TYPES.getKey(recipe.getType());
                if (!CREATE_MIXING.equals(type)) {
                    continue;
                }
                ItemStack result = recipe.getResultItem();
                if (!Salting.isSalted(result) || recipe.getIngredients().size() != 2) {
                    continue;
                }
                ItemStack plain = new ItemStack(result.getItem());
                Ingredient food = null;
                Ingredient seasoning = null;
                for (Ingredient ingredient : recipe.getIngredients()) {
                    if (food == null && ingredient.test(plain)) {
                        food = ingredient;
                    } else {
                        seasoning = ingredient;
                    }
                }
                if (food == null || seasoning == null) {
                    continue;
                }
                foods.add(plain);
                if (salts.isEmpty()) {
                    salts = Arrays.asList(seasoning.getItems());
                }
                moved.add(recipe.getId());
            }
            if (foods.isEmpty()) {
                return null;
            }
            return new Display(new ResourceLocation(CreativeEngineering.MODID, "seasoning/salt_mixing"), mixer,
                    List.of(foods, salts), salted(foods), true);
        }

        private static List<ItemStack> salted(List<ItemStack> foods) {
            List<ItemStack> out = new ArrayList<>(foods.size());
            for (ItemStack food : foods) {
                out.add(Salting.setSalted(food.copy()));
            }
            return out;
        }
    }
}
