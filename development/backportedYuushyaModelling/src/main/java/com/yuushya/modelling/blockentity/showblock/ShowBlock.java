// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/showblock/ShowBlock.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.showblock;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.BlockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowBlock extends AbstractTransformBlock {
   public ShowBlock(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (level.getBlockState(pos).is(state.getBlock()) && level.getBlockEntity(pos) instanceof ShowBlockEntity showBlockEntity) {
         if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "gui_item")))) {
            showBlockEntity.setShowFrame();
         } else if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "rot_trans_item")))) {
            showBlockEntity.setShowRotAxis();
            showBlockEntity.setShowText();
         } else if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "pos_trans_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "micro_pos_trans_item")))) {
            showBlockEntity.setShowPosAxis();
            showBlockEntity.setShowText();
         } else if (context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "slot_trans_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "get_showblock_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "move_transformdata_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "get_blockstate_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "scale_trans_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "debug_stick_item")))
            || context.isHoldingItem((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "destroy_item")))) {
            showBlockEntity.setShowText();
         }
      }

      return super.getShape(state, level, pos, context);
   }

   @NotNull
   public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
      return ((BlockShape)blockState.getValue(YuushyaBlockStates.SHAPES)).voxelShape;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
      return new ShowBlockEntity(blockPos, blockState);
   }

   @NotNull
   public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
      if (worldIn.getBlockEntity(currentPos) instanceof ShowBlockEntity showBlockEntity) {
         BlockState blockState = showBlockEntity.getTransformData(0).blockState;
         Block block = blockState.getBlock();
         if (facingState.getBlock() instanceof ShowBlock) {
            showBlockEntity.saveChanged();
            return stateIn;
         }

         if (!(block instanceof AirBlock)) {
            BlockState blockState1 = blockState.updateShape(facing, facingState, worldIn, currentPos, facingPos);
            if (!blockState1.isAir()) {
               showBlockEntity.getTransformData(0).blockState = blockState1;
               showBlockEntity.saveChanged();
               return (BlockState)stateIn.setValue(BlockStateProperties.POWERED, !(Boolean)stateIn.getValue(BlockStateProperties.POWERED));
            }
         }

         showBlockEntity.saveChanged();
      }

      return (BlockState)stateIn.setValue(BlockStateProperties.POWERED, !(Boolean)stateIn.getValue(BlockStateProperties.POWERED));
   }
}
