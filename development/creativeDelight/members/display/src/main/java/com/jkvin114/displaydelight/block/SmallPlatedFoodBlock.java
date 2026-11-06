package com.jkvin114.displaydelight.block;

import com.jkvin114.displaydelight.init.BlockAssociations;
import com.jkvin114.displaydelight.init.DisplayBlocks;
import com.jkvin114.displaydelight.init.DisplayProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallPlatedFoodBlock extends AbstractItemBlock {
   public static final BooleanProperty PLATE_HIDDEN = DisplayProperties.PLATE_HIDDEN;

   public SmallPlatedFoodBlock(Properties props) {
      super(props);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PLATE_HIDDEN, false)).setValue(SUPPORT, false));
   }

   public BlockState getStateFrom(LevelAccessor level, BlockState plate, BlockPos pos, Direction direction) {
      return plate.is((Block)DisplayBlocks.SMALL_PLATE.get())
         ? (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction.getOpposite()))
            .setValue(AbstractItemBlock.SUPPORT, BlockHelper.needSupport(level, pos))
         : this.defaultBlockState();
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      ItemStack stk = this.getStackFor();
      if (stk.getItem().equals(Items.AIR)) {
         stk = new ItemStack(state.getBlock().asItem());
      }

      return stk;
   }

   @Override
   public ItemStack getStackFor() {
      return new ItemStack(BlockAssociations.getSmallPlatedItemFor(this));
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
      return true;
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{PLATE_HIDDEN});
   }

   public boolean hasPlate(BlockState state) {
      return !(Boolean)state.getValue(PLATE_HIDDEN);
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return box(4.0, 0.0, 4.0, 12.0, 5.0, 12.0);
   }
}
