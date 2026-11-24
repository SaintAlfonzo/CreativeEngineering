// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.SaintAlfonzo.creativedelight.CDConfig;

import net.minecraft.world.level.block.BeehiveBlock;

/**
 * {@code BeehiveBlock.dropHoneycomb} ({@code m_49600_}) pops {@code new ItemStack(Items.HONEYCOMB, 3)}. Both shear
 * paths call it: the hive's own {@code use} and the shears dispenser behaviour. The 3 is multiplied by
 * {@code [bees] honeycombMultiplier} (pack 2, so 6 per shear; farming_crops A9 / Q7). Hives from other mods that
 * call this method get the same; ones with their own drop code don't. Moved from Creative Common in the wave 2 carve
 * (plan_wave2_publish.md); the config value is carried over once from creativecommon-common.toml.
 */
@Mixin(BeehiveBlock.class)
public abstract class BeehiveHoneycombMixin {

	@ModifyConstant(method = "m_49600_", remap = false, require = 1, constant = @Constant(intValue = 3))
	private static int creativedelight$honeycombCount(int vanilla) {
		return vanilla * CDConfig.BEES.honeycombMultiplier.get();
	}
}
