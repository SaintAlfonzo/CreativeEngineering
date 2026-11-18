// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.hearth.mixin;

import com.SaintAlfonzo.creativedelight.hearth.salt.SaltEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every placement of The Salt's Rock Salt Block after world generation makes a salt lick (plan_post_scratch_1115 G11,
 * {@link SaltEvents#afterPlace}). The Salt's block class ({@code SaltBlock}) does not override {@code onPlace}, so the
 * hook sits on the inherited {@code BlockBehaviour.onPlace}; any other block returns at the identity check.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockPlaceLickMixin {
    @Inject(method = "m_6807_", at = @At("HEAD"), remap = false)
    private void hearthandharvest$placeLick(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving, CallbackInfo ci) {
        if ((Object) this instanceof Block block) {
            SaltEvents.afterPlace(block, level, pos, oldState, isMoving);
        }
    }
}
