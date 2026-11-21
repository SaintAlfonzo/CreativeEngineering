// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import net.darkhax.botanypots.block.BlockEntityBotanyPot;
import net.darkhax.botanypots.data.recipes.crop.Crop;
import net.darkhax.botanypots.data.recipes.soil.Soil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nether crops grow in a Botany Pot only in the Nether, End crops only in the End (owner ruling R11,
 * plan_owner_rows_1120.md; botany_pots.md 3). Botany Pots 9 has no dimension condition on a crop or a soil, and its
 * Forge events only swap the looked-up crop or the drops, so the gate sits on {@code BotanyPotHelper.canCropGrow}, the
 * one check the pot's tick ({@code areGrowthConditionsMet}) runs before it adds growth. The seed in the pot decides:
 * {@code #creativecompat:botany_pots/nether_only} and {@code /end_only}, written by tools/gen_botany_crops.py from
 * kubejs/generation_scripts/recipes/botany_pot_crops.csv. Elsewhere the seed stays in the pot and does not grow, like a
 * crop on the wrong soil; JEI still lists the recipe.
 */
@Mixin(targets = "net.darkhax.botanypots.BotanyPotHelper", remap = false)
public abstract class BotanyPotDimensionMixin {

	private static final TagKey<Item> creativecompat$NETHER_ONLY =
			TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("creativecompat", "botany_pots/nether_only"));
	private static final TagKey<Item> creativecompat$END_ONLY =
			TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("creativecompat", "botany_pots/end_only"));

	@Inject(method = "canCropGrow", at = @At("HEAD"), cancellable = true, require = 1)
	private static void creativecompat$dimensionGate(Level level, BlockPos pos, BlockEntityBotanyPot pot, Soil soil,
			Crop crop, CallbackInfoReturnable<Boolean> cir) {
		if (level == null || pot == null || crop == null) return;
		ItemStack seed = pot.getInventory().getCropStack();
		if (seed.isEmpty()) return;
		if (seed.is(creativecompat$NETHER_ONLY) && level.dimension() != Level.NETHER) cir.setReturnValue(false);
		else if (seed.is(creativecompat$END_ONLY) && level.dimension() != Level.END) cir.setReturnValue(false);
	}
}
