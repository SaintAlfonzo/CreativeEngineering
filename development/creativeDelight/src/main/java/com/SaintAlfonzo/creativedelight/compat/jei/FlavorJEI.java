package com.SaintAlfonzo.creativedelight.compat.jei;

import com.SaintAlfonzo.creativedelight.CDConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.compat.jei.FlavorCategory.Display;
import com.SaintAlfonzo.creativedelight.food.FlavorRecipe;
import com.SaintAlfonzo.creativedelight.food.Flavors;
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
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Moves flavor crafts out of the Crafting and Mixing pages onto one Flavor page ({@link FlavorCategory}):
 * <ol>
 *   <li>Salt's {@code salt:salting} special craft (Salt shows it as a Crafting extension): one page, any
 *       {@code #salt:can_be_salted} food + salt, food and salted food cycling together;</li>
 *   <li>Create: The Salt's mixes (food + salt -> the same food with {@code Salted}): one page with the mixer;</li>
 *   <li>the pack's flavor crafts ({@link FlavorRecipe}, special, so never on the Crafting page) and crafting
 *       recipes with ids {@code creativedelight:shapeless/flavor/...}, a page each.</li>
 * </ol>
 * {@link #onRuntimeAvailable} hides exactly what was re-shown. Gated by {@code [jei] seasoningCategory}; re-run on
 * every JEI reload.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class FlavorJEI implements IModPlugin {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation ID = new ResourceLocation(CreativeDelight.MOD_ID, "flavor");
	private static final ResourceLocation SALT = new ResourceLocation("salt", "salt");
	private static final ResourceLocation SALT_DUST = new ResourceLocation("vintagedelight", "salt_dust");
	private static final ResourceLocation MIXER = new ResourceLocation("create", "mechanical_mixer");
	private static final ResourceLocation CREATE_MIXING = new ResourceLocation("create", "mixing");
	private static final String FLAVOR_PREFIX = "shapeless/flavor/";

	/** Recipe ids now on the Flavor page, per page they came from (set by the last reload). */
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
		registration.addRecipeCategories(new FlavorCategory(registration.getJeiHelpers().getGuiHelper(),
				icon.isEmpty() ? new ItemStack(Items.BREAD) : icon));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), FlavorCategory.TYPE);
		ItemStack mixer = stack(MIXER);
		if (!mixer.isEmpty()) {
			registration.addRecipeCatalyst(mixer, FlavorCategory.TYPE);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		movedCrafting = Set.of();
		movedMixing = Set.of();
		if (Minecraft.getInstance().level == null || !com.SaintAlfonzo.creativecompat.CompatConfig.JEI.seasoningCategory.get()) {
			return;
		}
		try {
			RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
			ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
			List<Display> displays = new ArrayList<>();
			Set<ResourceLocation> crafting = new HashSet<>();
			Set<ResourceLocation> mixing = new HashSet<>();
			boolean salt = CDConfig.member("salt");

			for (CraftingRecipe recipe : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
				Display display = recipe instanceof FlavorRecipe flavor ? pack(flavor, table) : null;
				if (display == null && salt) {
					display = SaltPages.salting(recipe, table);
				}
				if (display == null && isPackFlavor(recipe)) {
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

			registration.addRecipes(FlavorCategory.TYPE, displays);
			movedCrafting = crafting;
			movedMixing = mixing;
			LOGGER.info("[jei] Flavor page: {} page(s), moved {} crafting and {} mixing recipe(s)",
					displays.size(), crafting.size(), mixing.size());
		} catch (Throwable t) {
			movedCrafting = Set.of();
			movedMixing = Set.of();
			LOGGER.warn("[jei] couldn't build the Flavor page, leaving flavor crafts where they are", t);
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
			LOGGER.info("[jei] hid {} flavor recipe(s) from the Crafting and Mixing pages", hidden);
		} catch (Throwable t) {
			LOGGER.warn("[jei] couldn't hide flavor recipes from the Crafting and Mixing pages", t);
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

	/** A pack flavor craft: {@code creativedelight:shapeless/flavor/...}, short enough for the page. */
	private static boolean isPackFlavor(CraftingRecipe recipe) {
		ResourceLocation id = recipe.getId();
		int size = recipe.getIngredients().size();
		return id.getNamespace().equals(CreativeDelight.MOD_ID) && id.getPath().startsWith(FLAVOR_PREFIX)
				&& size >= 2 && size <= FlavorCategory.MAX_INPUTS;
	}

	/** A {@link FlavorRecipe} page: every food it takes, flavored in step; null if it matches nothing or is too long. */
	private static Display pack(FlavorRecipe recipe, ItemStack machine) {
		// a pair craft's food ingredient is the first flavor's tag; keep the foods both take
		List<ItemStack> foods = Arrays.stream(recipe.getFood().getItems())
				.filter(food -> Flavors.accepts(food, recipe.flavors())).toList();
		if (foods.isEmpty() || recipe.getParts().size() + 1 > FlavorCategory.MAX_INPUTS) {
			return null;
		}
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(foods);
		for (FlavorRecipe.Part part : recipe.getParts()) {
			inputs.add(Arrays.asList(part.ingredient().getItems()));
		}
		List<ItemStack> outputs = new ArrayList<>(foods.size());
		for (ItemStack food : foods) {
			outputs.add(Flavors.with(food.copy(), recipe.flavors()));
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
			if (inputs.size() > FlavorCategory.MAX_INPUTS) {
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
				Ingredient flavor = null;
				for (Ingredient ingredient : recipe.getIngredients()) {
					if (food == null && ingredient.test(plain)) {
						food = ingredient;
					} else {
						flavor = ingredient;
					}
				}
				if (food == null || flavor == null) {
					continue;
				}
				foods.add(plain);
				if (salts.isEmpty()) {
					salts = Arrays.asList(flavor.getItems());
				}
				moved.add(recipe.getId());
			}
			if (foods.isEmpty()) {
				return null;
			}
			return new Display(new ResourceLocation(CreativeDelight.MOD_ID, "flavor/salt_mixing"), mixer,
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
