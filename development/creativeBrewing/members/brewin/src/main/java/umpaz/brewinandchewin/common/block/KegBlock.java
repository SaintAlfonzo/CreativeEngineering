package umpaz.brewinandchewin.common.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.utility.BnCMathUtils;

public class KegBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE_X = Block.box(1.0, 0.0, 0.0, 15.0, 16.0, 16.0);
   protected static final VoxelShape SHAPE_Z = Block.box(0.0, 0.0, 1.0, 16.0, 16.0, 15.0);
   protected static final VoxelShape SHAPE_VERTICAL = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

   public KegBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VERTICAL, false).setValue(WATERLOGGED, false));
   }

   @Override
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      ItemStack stack = player.getItemInHand(hand);
      BlockEntity tileEntity = level.getBlockEntity(pos);
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         if (tileEntity instanceof KegBlockEntity kegBE) {
            List<ItemStack> itms = stack.isEmpty() ? List.of() : kegBE.extractInWorld(stack, 1, player.getAbilities().instabuild);
            if (!itms.isEmpty()) {
               itms.forEach(itm -> {
                  if (!ItemStack.isSameItemSameTags(itm, stack)) {
                     if (stack.isEmpty()) {
                        player.setItemInHand(hand, itm);
                     } else if (!player.getInventory().add(itm)) {
                        player.drop(itm, false);
                     }
                  }
               });
               kegBE.awardUsedRecipes(player);
               level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            }

            kegBE.updateTemperature();
            BrewinAndChewin.getHelper().openKegMenu(player, kegBE, pos);
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (state.getValue(VERTICAL)) {
         return SHAPE_VERTICAL;
      } else {
         return state.getValue(FACING) != Direction.NORTH && state.getValue(FACING) != Direction.SOUTH ? SHAPE_Z : SHAPE_X;
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      FluidState fluid = level.getFluidState(context.getClickedPos());
      return this.defaultBlockState()
         .setValue(FACING, context.getHorizontalDirection().getOpposite())
         .setValue(VERTICAL, context.getNearestLookingVerticalDirection() == Direction.UP || context.getNearestLookingDirection() == Direction.DOWN)
         .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
   }

   @Override
   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      if (state.getValue(WATERLOGGED)) {
         level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return state;
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
      ItemStack stack = super.getCloneItemStack(level, pos, state);
      Optional<KegBlockEntity> kegBE = level.getBlockEntity(pos, BnCBlockEntityTypes.KEG);
      kegBE.ifPresent(blockEntity -> {
         var tag = blockEntity.writeDrink(new net.minecraft.nbt.CompoundTag());
         tag.remove("id");
         if (!tag.isEmpty()) {
            BlockItem.setBlockEntityData(stack, BnCBlockEntityTypes.KEG, tag);
         }
      });
      return stack;
   }

   @Override
   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         if (level.getBlockEntity(pos) instanceof KegBlockEntity kegEntity) {
            Containers.dropContents(level, pos, kegEntity.getDroppableInventory());
            kegEntity.getUsedRecipesAndPopExperience(level, Vec3.atCenterOf(pos));
            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(FACING, VERTICAL, WATERLOGGED);
   }

   @Override
   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
      BlockEntity tileEntity = level.getBlockEntity(pos);
      if (tileEntity instanceof KegBlockEntity) {
         AbstractedItemHandler inventory = ((KegBlockEntity)tileEntity).getInventory();
         return BnCMathUtils.redstoneFromItemHandler(inventory);
      } else {
         return 0;
      }
   }

   @Override
   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return BnCBlockEntityTypes.KEG.create(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
      return level.isClientSide() ? null : createTickerHelper(blockEntity, BnCBlockEntityTypes.KEG, KegBlockEntity::fermentingTick);
   }

   @Override
   public BlockState rotate(BlockState state, Rotation rotation) {
      return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation(state.getValue(FACING)));
   }
}
