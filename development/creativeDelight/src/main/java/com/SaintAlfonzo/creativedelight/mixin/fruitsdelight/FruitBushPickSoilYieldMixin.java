package com.SaintAlfonzo.creativedelight.mixin.fruitsdelight;

import com.SaintAlfonzo.creativedelight.crops.SoilYieldPicks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Soil yield on Fruits Delight blueberry and cranberry bushes's own pick (plan_crop_yields_wave2.md 4, {@link SoilYieldPicks}). The pick's {@code use}
 * (SRG {@code m_6227_}) drops its produce with {@code popResource} ({@code m_49840_}) and never rolls a loot table;
 * this hands that stack to {@link SoilYieldPicks#withSoilExtra}, which adds the soil extra. Age 4 pick of 1 to 2 berries; other FruitBushBlock bushes have no row in soil_yield.json and are unchanged. The lemon tree (DoubleFruitBushBlock) is in LemonTreePickYieldMixin.
 * The call's owner in the bytecode is the block class itself, so the target names no owner.
 */
@Pseudo
@Mixin(targets = {"dev.xkmc.fruitsdelight.content.block.FruitBushBlock"}, remap = false)
public abstract class FruitBushPickSoilYieldMixin {

	@ModifyArg(method = "m_6227_", remap = false, require = 1,
			at = @At(value = "INVOKE", ordinal = 0, remap = false,
					target = "m_49840_(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V"),
			index = 2)
	private ItemStack creativedelight$soilExtra(Level level, BlockPos pos, ItemStack stack) {
		return SoilYieldPicks.withSoilExtra(level, pos, stack);
	}
}
