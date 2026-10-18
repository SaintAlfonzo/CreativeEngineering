package umpaz.brewinandchewin.common.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;

public class CoasterBlock extends BaseEntityBlock {
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 4);
   public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
   protected static final VoxelShape COASTER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
   protected static final VoxelShape TRAY_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);

   public CoasterBlock() {
      super(Properties.copy(Blocks.BROWN_CARPET).sound(SoundType.WOOD).instabreak());
      this.registerDefaultState(this.getStateDefinition().any().setValue(ROTATION, 0).setValue(SIZE, 0).setValue(INVISIBLE, false));
   }

   @Override
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      int size = state.getValue(SIZE);
      return size > 1 ? TRAY_SHAPE : COASTER_SHAPE;
   }

   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return state.getValue(INVISIBLE) ? Shapes.empty() : super.getCollisionShape(state, worldIn, pos, context);
   }

   // Port: 1.21 useItemOn, then useWithoutItem for the main hand when that passes.
   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      if (level.getBlockEntity(pos) instanceof CoasterBlockEntity coasterBlockEntity) {
         InteractionResult result = coasterBlockEntity.useItemOn(player.getItemInHand(hand), level, state, pos, player, hand);
         if (result.consumesAction()) {
            return result;
         }
         if (hand == InteractionHand.MAIN_HAND) {
            return coasterBlockEntity.useWithoutItem(state, level, pos, player, hitResult);
         }
      }
      return InteractionResult.PASS;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      // Port: 1.20 RotationSegment.convertToSegment, as 1.19.2's standing sign does it.
      return this.defaultBlockState().setValue(ROTATION, Mth.floor((double)((context.getRotation() + 180.0F) * 16.0F / 360.0F) + 0.5) & 15);
   }

   @Override
   public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
      return facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos floorPos = pos.below();
      return canSupportRigidBlock(level, floorPos) || canSupportCenter(level, floorPos, Direction.UP);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(ROTATION, SIZE, INVISIBLE);
   }

   @Override
   public BlockState rotate(BlockState pState, Rotation pRot) {
      return pState.setValue(ROTATION, pRot.rotate(pState.getValue(ROTATION), 16));
   }

   @Override
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(ROTATION, pMirror.mirror(pState.getValue(ROTATION), 16));
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof CoasterBlockEntity be) {
            Containers.dropContents(level, pos, be.getItems());
            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, newState, moved);
      }
   }

   @Override
   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
      return Math.min(blockState.getValue(SIZE) * 4, 15);
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      if (level.getBlockEntity(pos) instanceof CoasterBlockEntity blockEntity && blockEntity.getItems().stream().anyMatch(stack -> !stack.isEmpty())) {
         List<ItemStack> stacks = blockEntity.getItems().stream().filter(stack -> !stack.isEmpty()).toList();
         return stacks.get(stacks.size() - 1).copy();
      }

      return super.getCloneItemStack(level, pos, state);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new CoasterBlockEntity(pPos, pState);
   }
}
