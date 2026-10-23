package net.ribs.vintagedelight.block.custom;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.SafetyNetBlock;

public class VineNetBlock extends SafetyNetBlock {
   public static final EnumProperty<VineNetBlock.Position> POSITION = EnumProperty.create("position", VineNetBlock.Position.class);

   public VineNetBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(POSITION, VineNetBlock.Position.MIDDLE));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{POSITION});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos blockpos = context.getClickedPos();
      FluidState fluid = context.getLevel().getFluidState(blockpos);
      Direction direction = context.getClickedFace();
      double hitY = context.getClickLocation().y - (double)blockpos.getY();
      VineNetBlock.Position position;
      if (direction == Direction.DOWN || hitY > 0.66) {
         position = VineNetBlock.Position.TOP;
      } else if (direction != Direction.UP && !(hitY < 0.33)) {
         position = VineNetBlock.Position.MIDDLE;
      } else {
         position = VineNetBlock.Position.BOTTOM;
      }

      return (BlockState)((BlockState)this.defaultBlockState().setValue(POSITION, position)).setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return switch ((VineNetBlock.Position)state.getValue(POSITION)) {
         case TOP -> Block.box(0.0, 13.0, 0.0, 16.0, 15.9, 16.0);
         case MIDDLE -> Block.box(0.0, 7.0, 0.0, 16.0, 9.0, 16.0);
         case BOTTOM -> Block.box(0.0, 0.1, 0.0, 16.0, 3.0, 16.0);
      };
   }

   public static enum Position implements StringRepresentable {
      TOP,
      MIDDLE,
      BOTTOM;

      public String getSerializedName() {
         return this.name().toLowerCase();
      }
   }
}
