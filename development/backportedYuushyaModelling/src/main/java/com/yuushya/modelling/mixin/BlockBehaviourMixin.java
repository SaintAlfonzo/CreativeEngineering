// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 mixin/BlockBehaviourMixin.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.mixin;

import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockStateBase.class})
public abstract class BlockBehaviourMixin {
   @Inject(
      at = {@At("HEAD")},
      method = {"isCollisionShapeFullBlock"},
      cancellable = true
   )
   private void isCollisionShapeFullBlock(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      BlockState state = (BlockState)(Object)this;
      if (state.is((Block)BlockRegistry.ITEM_BLOCK.get())) {
         boolean isFull = (Boolean)state.getValue(AbstractTransformBlock.FULL_BLOCK);
         cir.setReturnValue(isFull);
      }
   }
}
