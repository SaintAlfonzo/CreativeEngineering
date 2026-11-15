package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.crops.SoilYieldPicks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Soil yield on vanilla sweet berry bushes's own pick (plan_crop_yields_wave2.md 4, {@link SoilYieldPicks}). The pick's {@code use}
 * (SRG {@code m_6227_}) drops its produce with {@code popResource} ({@code m_49840_}) and never rolls a loot table;
 * this hands that stack to {@link SoilYieldPicks#withSoilExtra}, which adds the soil extra. Only the age 3 pick (2 to 3 berries) gets the extra.
 * The call's owner in the bytecode is the block class itself, so the target names no owner.
 */
@Pseudo
@Mixin(targets = {"net.minecraft.world.level.block.SweetBerryBushBlock"}, remap = false)
public abstract class SweetBerryPickSoilYieldMixin {

	@ModifyArg(method = "m_6227_", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, remap = false,
					target = "m_49840_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"),
			index = 2)
	private ItemStack creativedelight$soilExtra(Level level, BlockPos pos, ItemStack stack) {
		return SoilYieldPicks.withSoilExtra(level, pos, stack);
	}
}
