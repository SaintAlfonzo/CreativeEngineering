package com.SaintAlfonzo.creativeengineering.compat.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.ModList;

/**
 * Moves the can crafts off the Crafting page onto two pages of their own:
 * <ul>
 *   <li><b>Canning</b> (icon: empty can + crafting table): crafts that take an empty can and make a can
 *       ({@code #creativeengineering:cans});</li>
 *   <li><b>Using Cans</b> (icon: crushed can + crafting table): crafts that take a can (NCH's {@code cans} argument:
 *       uncanning, pies from a can, ...).</li>
 * </ul>
 * The icons are Create's two-item tab icon (big item, small crafting table on top), as on Create's Automated Shaped
 * Crafting page. {@link #onRuntimeAvailable} hides exactly what was re-shown. Gated by {@code [jei] canCategories};
 * re-run on every JEI reload.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class CanPagesJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, "can_pages");

    public static final mezz.jei.api.recipe.RecipeType<CraftingRecipe> CANNING =
            mezz.jei.api.recipe.RecipeType.create(CreativeEngineering.MODID, "canning", CraftingRecipe.class);
    public static final mezz.jei.api.recipe.RecipeType<CraftingRecipe> CAN_USES =
            mezz.jei.api.recipe.RecipeType.create(CreativeEngineering.MODID, "can_uses", CraftingRecipe.class);

    private static final ResourceLocation EMPTY_CAN = new ResourceLocation("canned_goods", "empty_can");
    private static final ResourceLocation CRUSHED_CAN = new ResourceLocation("canned_goods", "crushed_empty_can");
    private static final TagKey<Item> CANS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "cans"));
    /** Seasoning crafts have their own page ({@link SeasoningJEI}). */
    private static final String SEASON_PREFIX = "shapeless/season/";

    /** Recipe ids now on these pages (set by the last reload). */
    private Set<ResourceLocation> moved = Set.of();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new CanCraftingCategory(gui, CANNING, "creativeengineering.jei.canning", icon(gui, EMPTY_CAN)),
                new CanCraftingCategory(gui, CAN_USES, "creativeengineering.jei.can_uses", icon(gui, CRUSHED_CAN)));
    }

    /** The can with a small crafting table on top (Create's DoubleItemIcon), or the can alone without Create. */
    private static IDrawable icon(IGuiHelper gui, ResourceLocation can) {
        ItemStack stack = RecipeRows.stack(can);
        if (stack.isEmpty()) {
            stack = new ItemStack(Items.CRAFTING_TABLE);
        }
        if (ModList.get().isLoaded("create")) {
            return CreateIcon.of(stack, new ItemStack(Items.CRAFTING_TABLE));
        }
        return gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), CANNING, CAN_USES);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(CraftingMenu.class, MenuType.CRAFTING, CANNING, 1, 9, 10, 36);
        registration.addRecipeTransferHandler(CraftingMenu.class, MenuType.CRAFTING, CAN_USES, 1, 9, 10, 36);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        moved = Set.of();
        if (Minecraft.getInstance().level == null || !CEConfig.JEI.canCategories.get()) {
            return;
        }
        try {
            List<CraftingRecipe> canning = new ArrayList<>();
            List<CraftingRecipe> uses = new ArrayList<>();
            ItemStack emptyCan = RecipeRows.stack(EMPTY_CAN);
            for (CraftingRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
                ResourceLocation id = recipe.getId();
                if (recipe.isSpecial() || recipe.getResultItem().isEmpty()
                        || (id.getNamespace().equals(CreativeEngineering.MODID) && id.getPath().startsWith(SEASON_PREFIX))) {
                    continue;
                }
                if (recipe.getResultItem().is(CANS) && !emptyCan.isEmpty() && takes(recipe, stack -> stack.is(emptyCan.getItem()))) {
                    canning.add(recipe);
                } else if (takes(recipe, stack -> stack.is(CANS))) {
                    uses.add(recipe);
                }
            }
            Comparator<CraftingRecipe> byId = Comparator.comparing(r -> r.getId().toString());
            canning.sort(byId);
            uses.sort(byId);
            registration.addRecipes(CANNING, canning);
            registration.addRecipes(CAN_USES, uses);
            Set<ResourceLocation> ids = new HashSet<>();
            canning.forEach(r -> ids.add(r.getId()));
            uses.forEach(r -> ids.add(r.getId()));
            moved = ids;
            LOGGER.info("[jei] Canning: {} recipe(s); Using Cans: {} recipe(s)", canning.size(), uses.size());
        } catch (Throwable t) {
            moved = Set.of();
            LOGGER.warn("[jei] couldn't build the Canning / Using Cans pages, leaving can crafts on the Crafting page", t);
        }
    }

    /** True if some ingredient of the recipe accepts an item that matches {@code test}. */
    private static boolean takes(CraftingRecipe recipe, java.util.function.Predicate<ItemStack> test) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : ingredient.getItems()) {
                if (test.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        try {
            int hidden = RecipeRows.hide(runtime.getRecipeManager(), Map.of(RecipeTypes.CRAFTING.getUid(), moved));
            LOGGER.info("[jei] hid {} can craft(s) from the Crafting page", hidden);
        } catch (Throwable t) {
            LOGGER.warn("[jei] couldn't hide can crafts from the Crafting page", t);
        }
    }

    /** Everything that touches Create's classes; only loaded when Create is. */
    private static final class CreateIcon {
        static IDrawable of(ItemStack big, ItemStack small) {
            return new com.simibubi.create.compat.jei.DoubleItemIcon(() -> big, () -> small);
        }
    }
}
