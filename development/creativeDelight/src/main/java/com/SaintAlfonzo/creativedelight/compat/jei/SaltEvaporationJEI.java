package com.SaintAlfonzo.creativedelight.compat.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * The Salt's cauldron evaporation page with the pack's yields (plan_post_scratch_1115 G7.7, salt.md 6 S1): The Salt's own
 * page shows 1 salt, but the pack's loot tables ({@code salt:cauldron_evaporation/salt_full}, {@code salt_level_2},
 * {@code salt_level_1}) give 8, 5 and 3. This page reads those tables when the server is in the same process (single
 * player, LAN host); a client of a dedicated server cannot see server loot tables and shows the pack defaults 8, 5, 3.
 * Same art and layout as The Salt's page (its texture used in place); The Salt's category is hidden.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class SaltEvaporationJEI implements IModPlugin {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation ID = new ResourceLocation(CreativeDelight.MOD_ID, "salt_evaporation");
	public static final RecipeType<Evaporation> TYPE = RecipeType.create(CreativeDelight.MOD_ID, "salt_evaporation", Evaporation.class);
	/** The Salt's own page, hidden at runtime. */
	private static final ResourceLocation SALT_PAGE = new ResourceLocation("salt", "salt_evaporation");

	private static final ResourceLocation SALT_DUST = new ResourceLocation("vintagedelight", "salt_dust");
	private static final ResourceLocation SALT_ITEM = new ResourceLocation("salt", "salt");
	private static final ResourceLocation FULL = new ResourceLocation("salt", "cauldron_evaporation/salt_full");
	private static final ResourceLocation TWO_THIRDS = new ResourceLocation("salt", "cauldron_evaporation/salt_level_2");
	private static final ResourceLocation ONE_THIRD = new ResourceLocation("salt", "cauldron_evaporation/salt_level_1");

	/** Yields by fill level: full, two thirds, one third. */
	public record Evaporation(ItemStack salt, int full, int twoThirds, int oneThird) {
	}

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	private static boolean active() {
		return ModList.get().isLoaded("salt");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (active()) {
			registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (active()) {
			registration.addRecipeCatalyst(new ItemStack(Items.CAULDRON), TYPE);
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (!active()) {
			return;
		}
		Item salt = ForgeRegistries.ITEMS.getValue(SALT_DUST);
		if (salt == null || salt == Items.AIR) {
			salt = ForgeRegistries.ITEMS.getValue(SALT_ITEM);
		}
		if (salt == null || salt == Items.AIR) {
			return;
		}
		int full = tableYield(FULL, 8);
		int two = tableYield(TWO_THIRDS, 5);
		int one = tableYield(ONE_THIRD, 3);
		registration.addRecipes(TYPE, List.of(new Evaporation(new ItemStack(salt, Math.max(1, full)), full, two, one)));
	}

	/** Items one roll of the table gives (the tables are a single set_count entry), or {@code fallback} without a server. */
	private static int tableYield(ResourceLocation table, int fallback) {
		try {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server == null) {
				return fallback;
			}
			ServerLevel level = server.overworld();
			LootTable loot = server.getLootTables().get(table);
			if (loot == LootTable.EMPTY) {
				return fallback;
			}
			int n = 0;
			for (ItemStack s : loot.getRandomItems(new LootContext.Builder(level).create(LootContextParamSets.EMPTY))) {
				n += s.getCount();
			}
			return n;
		} catch (RuntimeException e) {
			LOGGER.debug("Salt evaporation page: {} not read ({}), showing {}", table, e.toString(), fallback);
			return fallback;
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		if (!active()) {
			return;
		}
		try {
			runtime.getRecipeManager().createRecipeCategoryLookup().includeHidden().get()
					.filter(c -> c.getRecipeType().getUid().equals(SALT_PAGE)).toList()
					.forEach(c -> runtime.getRecipeManager().hideRecipeCategory(c.getRecipeType()));
		} catch (RuntimeException e) {
			LOGGER.warn("Salt evaporation page: could not hide The Salt's own page", e);
		}
	}

	/** The Salt's layout: water cauldron and heat source on the left, the salt cauldron on the right, salt above it. */
	static final class Category implements IRecipeCategory<Evaporation> {
		private static final ResourceLocation TEXTURE = new ResourceLocation("salt", "textures/gui/jei/salt_evaporation.png");
		private final IDrawable background;
		private final IDrawable icon;

		Category(IGuiHelper gui) {
			background = gui.createDrawable(TEXTURE, 0, 0, 168, 90);
			Item salt = ForgeRegistries.ITEMS.getValue(SALT_DUST);
			icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(salt == null ? Items.CAULDRON : salt));
		}

		@Override
		public RecipeType<Evaporation> getRecipeType() {
			return TYPE;
		}

		@Override
		public Component getTitle() {
			return Component.translatable("gui.jei.category.salt_evaporation");
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return icon;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Evaporation recipe, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 14).addItemStack(recipe.salt())
					.addTooltipCallback((view, tooltip) -> {
						tooltip.add(Component.translatable("creativedelight.jei.salt_evaporation.full", recipe.full()).withStyle(ChatFormatting.GRAY));
						tooltip.add(Component.translatable("creativedelight.jei.salt_evaporation.two_thirds", recipe.twoThirds()).withStyle(ChatFormatting.GRAY));
						tooltip.add(Component.translatable("creativedelight.jei.salt_evaporation.one_third", recipe.oneThird()).withStyle(ChatFormatting.GRAY));
					});
		}

		@Override
		public List<Component> getTooltipStrings(Evaporation recipe, IRecipeSlotsView view, double x, double y) {
			List<Component> out = new ArrayList<>();
			if (x >= 15 && x < 60 && y >= 8 && y < 50) {
				out.add(Component.translatable("block.minecraft.water_cauldron"));
			} else if (x >= 15 && x < 60 && y >= 50 && y < 85) {
				out.add(Component.translatable("gui.jei.category.salt_evaporation.heat_source.tooltip"));
				out.add(Component.translatable("gui.jei.category.salt_evaporation.heat_source.tooltip_2").withStyle(ChatFormatting.GRAY)
						.append(Component.literal("#salt:heaters").withStyle(ChatFormatting.GOLD)));
			} else if (x >= 106 && x < 153 && y >= 31 && y < 76) {
				out.add(Component.translatable("block.salt.salt_cauldron"));
			}
			return out;
		}
	}
}
