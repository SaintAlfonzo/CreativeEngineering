// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationTargets;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Names the block Registrate is constructing (owner mod id + builder name) for the length of the factory call, so
 * StateDefinitionBuilderMixin can tell a target block from every other block of the same class (B1 fix). Registrate
 * 1.1.5 is nested in Create 0.5.1; Create, Create Deco and Design n' Decor register their blocks through it.
 */
@Mixin(value = BlockBuilder.class, remap = false)
public abstract class RegistrateBlockBuilderMixin {
	@Inject(method = "createEntry()Lnet/minecraft/world/level/block/Block;", at = @At("HEAD"), remap = false)
	private void creativeoxidation$begin(CallbackInfoReturnable<Block> cir) {
		AbstractBuilder<?, ?, ?, ?> self = (AbstractBuilder<?, ?, ?, ?>) (Object) this;
		OxidationTargets.beginBuild(self.getOwner().getModid() + ":" + self.getName());
	}

	@Inject(method = "createEntry()Lnet/minecraft/world/level/block/Block;", at = @At("RETURN"), remap = false)
	private void creativeoxidation$end(CallbackInfoReturnable<Block> cir) {
		OxidationTargets.endBuild();
	}
}
