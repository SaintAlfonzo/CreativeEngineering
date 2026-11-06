package com.jkvin114.displaydelight.block;

import com.jkvin114.displaydelight.init.BlockAssociations;
import com.jkvin114.displaydelight.init.DisplayProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractStackablePlatedFoodBlock extends AbstractItemBlock {
   public static final IntegerProperty STACKS = DisplayProperties.STACKS;
   public static final BooleanProperty PLATE_HIDDEN = DisplayProperties.PLATE_HIDDEN;

   public AbstractStackablePlatedFoodBlock(Properties props) {
      super(props);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(STACKS, 6))
               .setValue(PLATE_HIDDEN, false))
            .setValue(SUPPORT, false)
      );
   }

   public BlockState getStateFrom(LevelAccessor level, BlockState plate, BlockPos pos, Direction direction, int count) {
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction.getOpposite()))
            .setValue(STACKS, Math.min(count, this.getMaxStackable())))
         .setValue(AbstractItemBlock.SUPPORT, BlockHelper.needSupport(level, pos));
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      ItemStack stk = this.getStackFor();
      if (stk.getItem().equals(Items.AIR)) {
         stk = new ItemStack(state.getBlock().asItem());
      }

      return stk;
   }

   public boolean hasPlate(BlockState state) {
      return !(Boolean)state.getValue(PLATE_HIDDEN);
   }

   @Override
   public ItemStack getStackFor() {
      return new ItemStack(BlockAssociations.getPlatedItemFor(this));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{STACKS, PLATE_HIDDEN});
   }

   public int getStacks(BlockState state) {
      return Math.min(this.getMaxStackable(), (Integer)state.getValue(STACKS));
   }

   public BlockState getDecrementedState(BlockState state) {
      int stk = this.getStacks(state) - 1;
      if (this.getStacks(state) >= this.getMaxStackable()) {
         stk = this.getMaxStackable() - 1;
      }

      return (BlockState)state.setValue(STACKS, Math.max(1, stk));
   }

   public BlockState getIncrementedState(BlockState state, int count) {
      return (BlockState)state.setValue(STACKS, Math.min(this.getStacks(state) + count, this.getMaxStackable()));
   }

   public abstract int getMaxStackable();

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box(1.0, 0.0, 1.0, 15.0, 6.0, 15.0);
   }
}
