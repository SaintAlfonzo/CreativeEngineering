package com.jkvin114.displaydelight.block;

import com.jkvin114.displaydelight.init.DisplayProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EmptyPlateBlock extends Block {
   public static final BooleanProperty SUPPORT = DisplayProperties.SUPPORT;

   public EmptyPlateBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(SUPPORT, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{SUPPORT});
   }

   // 1.19.2: no Properties.pushReaction; 1.7.0's plates are destroyed by pistons
   @Override
   public PushReaction getPistonPushReaction(BlockState state) {
      return PushReaction.DESTROY;
   }

   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      return true;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      return (BlockState)this.defaultBlockState().setValue(SUPPORT, BlockHelper.needSupport(level, context.getClickedPos()));
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.DOWN
         ? (BlockState)state.setValue(SUPPORT, BlockHelper.needSupport(level, neighborPos, neighborState))
         : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }
}
