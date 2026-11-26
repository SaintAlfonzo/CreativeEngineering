// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationProperties;
import com.SaintAlfonzo.creativeoxidation.OxidationStage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Default state of a target block: unaffected, not waxed (StateDefinition.any() would pick waxed = true, the first
 * boolean value). Existing worlds load placed blocks with the default, so they start unaffected.
 */
@Mixin(Block.class)
public abstract class BlockMixin {
	@ModifyVariable(method = "registerDefaultState", at = @At("HEAD"), argsOnly = true)
	private BlockState creativeoxidation$defaultStage(BlockState state) {
		if (!OxidationProperties.hasProperties(state)) return state;
		return state.setValue(OxidationProperties.STAGE, OxidationStage.UNAFFECTED).setValue(OxidationProperties.WAXED, false);
	}
}
