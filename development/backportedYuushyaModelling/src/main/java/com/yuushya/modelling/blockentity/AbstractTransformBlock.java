// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/AbstractTransformBlock.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity;

import com.yuushya.modelling.block.AbstractYuushyaBlock;
import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.utils.YuushyaDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTransformBlock extends AbstractYuushyaBlock implements EntityBlock {
   public static final BooleanProperty ENABLE_AO = BooleanProperty.create("enable_ao");
   public static final BooleanProperty FULL_BLOCK = BooleanProperty.create("full_block");
   public static final Boolean DEFAULT_ENABLE_AO = false;
   public static final Boolean DEFAULT_FULL_BLOCK = false;

   public AbstractTransformBlock(Properties properties, Integer tipLines) {
      super(properties, tipLines);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.POWERED, false))
                     .setValue(YuushyaBlockStates.LIT, 0))
                  .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH))
               .setValue(ENABLE_AO, DEFAULT_ENABLE_AO))
            .setValue(FULL_BLOCK, DEFAULT_FULL_BLOCK)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> stateBuilder) {
      stateBuilder.add(new Property[]{YuushyaBlockStates.LIT})
         .add(new Property[]{BlockStateProperties.POWERED})
         .add(new Property[]{BlockStateProperties.HORIZONTAL_FACING})
         .add(new Property[]{YuushyaBlockStates.SHAPES})
         .add(new Property[]{ENABLE_AO})
         .add(new Property[]{FULL_BLOCK});
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
      if (blockPlaceContext.getPlayer() != null
         && blockPlaceContext.getPlayer()
            .isHolding((Item)Registry.ITEM.get(new ResourceLocation("yuushya", "rot_trans_item")))) {
         BlockState blockState = this.defaultBlockState();
         blockState.setValue(ENABLE_AO, DEFAULT_ENABLE_AO);
         blockState.setValue(FULL_BLOCK, DEFAULT_FULL_BLOCK);
         return blockPlaceContext.getClickedFace().getAxis() == Axis.Y
            ? (BlockState)blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection())
            : (BlockState)blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, blockPlaceContext.getClickedFace().getOpposite());
      } else {
         return super.getStateForPlacement(blockPlaceContext);
      }
   }

   @NotNull
   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
   }

   @NotNull
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      ItemStack itemStack = new ItemStack(this);
      YuushyaDataTags.setLit(itemStack, (Integer)pState.getValue(YuushyaBlockStates.LIT));
      YuushyaDataTags.setShapes(itemStack, ((BlockShape)pState.getValue(YuushyaBlockStates.SHAPES)).getSerializedName());
      return itemStack;
   }
}
