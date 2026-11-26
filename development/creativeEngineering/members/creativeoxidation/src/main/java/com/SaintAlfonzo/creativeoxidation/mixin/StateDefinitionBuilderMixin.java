// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationProperties;
import com.SaintAlfonzo.creativeoxidation.OxidationTargets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the stage and waxed properties to a block's state definition only when Registrate is building a target id on
 * this thread (B1 fix, {@link OxidationTargets}); upstream matched 48 classes, vanilla IronBarsBlock and DoorBlock
 * among them.
 */
@Mixin(StateDefinition.Builder.class)
public abstract class StateDefinitionBuilderMixin<O, S extends StateHolder<O, S>> {
	@Shadow
	@Final
	private O owner;

	@Inject(method = "create", at = @At("HEAD"))
	private void creativeoxidation$addProperties(CallbackInfoReturnable<StateDefinition<O, S>> cir) {
		if (OxidationTargets.takeForStateDefinition(owner instanceof Block)) {
			((StateDefinition.Builder<?, ?>) (Object) this).add(OxidationProperties.STAGE, OxidationProperties.WAXED);
		}
	}
}
