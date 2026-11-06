package net.mehdinoui.fungidelight.common.block.mushrooms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

// Port change (fungi_delight.md 5.1.5): the chain stops growing at MAX_LENGTH segments, for random ticks and bone meal
// alike (upstream had no cap). 1.19.2's isValidBonemealTarget takes a BlockGetter.
public class InkyGooVeilBlock extends Block implements BonemealableBlock {
   public static final int MAX_LENGTH = 16;
   public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
   protected static final VoxelShape SHAPE_NORMAL = Block.box(0.0, 9.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SHAPE_CONNECTED = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public InkyGooVeilBlock(Properties properties) {
      super(properties.noOcclusion().instabreak().lightLevel(state -> 5).sound(SoundType.HONEY_BLOCK));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(CONNECTED, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{CONNECTED});
   }

   @NotNull
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(CONNECTED) ? SHAPE_CONNECTED : SHAPE_NORMAL;
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      boolean connectedBelow = context.getLevel().getBlockState(pos.below()).is(this);
      return (BlockState)this.defaultBlockState().setValue(CONNECTED, connectedBelow);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, false);
      }

      return direction == Direction.DOWN
         ? (BlockState)state.setValue(CONNECTED, neighborState.is(this))
         : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState above = level.getBlockState(pos.above());
      return above.isFaceSturdy(level, pos.above(), Direction.DOWN) || above.is(this);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (level.isEmptyBlock(pos.below()) && !level.isClientSide()) {
         if (random.nextInt(16) == 0 && this.chainLength(level, pos) < MAX_LENGTH) {
            this.performBonemeal(level, random, pos, state);
         } else {
            super.randomTick(state, level, pos, random);
         }
      }
   }

   public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean pIsClient) {
      return this.chainLength(level, pos) < MAX_LENGTH;
   }

   private BlockPos bottom(BlockGetter level, BlockPos pos) {
      BlockPos target = pos;
      while (level.getBlockState(target.below()).is(this)) {
         target = target.below();
      }
      return target;
   }

   // Segments in the chain through pos: from its bottom segment upward to the first block that is not a veil.
   private int chainLength(BlockGetter level, BlockPos pos) {
      BlockPos cursor = this.bottom(level, pos);
      int length = 0;
      while (length < MAX_LENGTH && level.getBlockState(cursor).is(this)) {
         length++;
         cursor = cursor.above();
      }
      return length;
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      if (this.chainLength(level, pos) >= MAX_LENGTH) {
         return;
      }

      BlockPos below = this.bottom(level, pos).below();
      if (level.isEmptyBlock(below) && this.defaultBlockState().canSurvive(level, below)) {
         level.setBlock(below, (BlockState)this.defaultBlockState().setValue(CONNECTED, false), 3);
      }
   }
}
