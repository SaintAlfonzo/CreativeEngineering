package com.jkvin114.displaydelight.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FoodBlock extends AbstractItemBlock {
   public FoodBlock(Properties props) {
      super(props);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);
   }
}
