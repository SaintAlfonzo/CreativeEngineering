package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class PizzaBlock extends Block {
   public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);
   protected static final VoxelShape[] SHAPES = new VoxelShape[]{
      Block.box(0.0, 0.0, 0.0, 8.0, 2.0, 8.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 8.0),
      Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 8.0), Block.box(0.0, 0.0, 8.0, 8.0, 2.0, 16.0)),
      Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)
   };

   public PizzaBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(SERVINGS, 3));
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(SERVINGS)];
   }

   // Port: 1.21 useWithoutItem. 1.21 reaches it for the main hand when the held item does nothing; 1.19.2's use() is
   // called for both hands, so the off hand is skipped to keep one slice per click.
   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      if (hand != InteractionHand.MAIN_HAND) {
         return InteractionResult.PASS;
      }
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }
      return this.takeServing(level, pos, state, player, hand);
   }

   private InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand handIn) {
      int servings = state.getValue(SERVINGS);
      ItemStack heldStack = player.getItemInHand(handIn);
      if (!heldStack.isEmpty() && !heldStack.getItem().equals(BnCItems.PIZZA_SLICE)) {
         popResource(level, pos, new ItemStack(BnCItems.PIZZA_SLICE, 1));
      } else if (heldStack.getItem().equals(BnCItems.PIZZA_SLICE) && heldStack.getCount() < heldStack.getMaxStackSize()) {
         heldStack.setCount(heldStack.getCount() + 1);
      } else {
         player.setItemInHand(handIn, new ItemStack(BnCItems.PIZZA_SLICE));
      }

      level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (level.getBlockState(pos).getValue(SERVINGS) == 0) {
         level.destroyBlock(pos, false);
      } else if (level.getBlockState(pos).getValue(SERVINGS) > 0) {
         level.setBlock(pos, state.setValue(SERVINGS, servings - 1), 3);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
      return facing == Direction.DOWN && !this.canSurvive(state, level, pos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(state, facing, facingState, level, pos, facingPos);
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return canSupportCenter(level, pos.below(), Direction.UP);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(SERVINGS);
   }

   @Override
   public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
      return blockState.getValue(SERVINGS);
   }

   @Override
   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
      return false;
   }
}
