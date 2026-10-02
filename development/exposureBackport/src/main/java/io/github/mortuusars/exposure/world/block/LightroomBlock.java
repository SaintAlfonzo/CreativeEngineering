package io.github.mortuusars.exposure.world.block;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.InteractionHand;

public class LightroomBlock extends Block implements EntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty PRINTING = BooleanProperty.create("printing");
   public static final BooleanProperty REFRACTED = BooleanProperty.create("refracted");

   public LightroomBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(PRINTING, false))
            .setValue(REFRACTED, false)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{FACING, PRINTING, REFRACTED});
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
            if (level instanceof ServerLevel serverLevel) {
               Containers.dropContents(serverLevel, pos, lightroomBlockEntity);
               lightroomBlockEntity.dropStoredExperience(null);
            }

            level.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, level, pos, newState, isMoving);
      }
   }

   public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(@NotNull BlockState blockState, Level level, @NotNull BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
         ItemStack resultStack = lightroomBlockEntity.getItem(6);
         if (resultStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem) {
            int photographsCount = stackedPhotographsItem.getPhotographs(resultStack).size();
            return (int)((float)photographsCount / (float)stackedPhotographsItem.getStackLimit() * 14.0F + 1.0F);
         }

         if (!resultStack.isEmpty()) {
            return 1;
         }
      }

      return 0;
   }

   @NotNull
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return (BlockState)pState.setValue(FACING, pRotation.rotate((Direction)pState.getValue(FACING)));
   }

   @NotNull
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation((Direction)pState.getValue(FACING)));
   }

   @NotNull
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
      if (level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
         player.awardStat(Exposure.Stats.INTERACT_WITH_LIGHTROOM);
         if (player instanceof ServerPlayer serverPlayer) {
            lightroomBlockEntity.setLastPlayer(serverPlayer);
            lightroomBlockEntity.setChanged();
            PlatformHelper.openMenu(serverPlayer, lightroomBlockEntity, buffer -> buffer.writeBlockPos(pos));
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return super.use(state, level, pos, player, hand, hitResult);
      }
   }

   public void neighborChanged(
      @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean pIsMoving
   ) {
      if (!level.isClientSide) {
         if (!(Boolean)state.getValue(PRINTING)) {
            for (Direction direction : Direction.values()) {
               BlockPos relative = pos.relative(direction);
               if (relative.equals(fromPos)
                  && level.getSignal(relative, direction) > 0
                  && level.getBlockEntity(pos) instanceof LightroomBlockEntity lightroomBlockEntity) {
                  lightroomBlockEntity.startPrintingProcess(true);
                  break;
               }
            }
         }

         level.setBlock(
            pos, (BlockState)level.getBlockState(pos).setValue(REFRACTED, level.getBlockState(pos.above()).is(Exposure.Tags.Blocks.CHROMATIC_REFRACTORS)), 2
         );
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return createBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return getBlockTicker(level, state, blockEntityType);
   }

   public static BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new LightroomBlockEntity(pos, state);
   }

   public static <T extends BlockEntity> BlockEntityTicker<T> getBlockTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return !level.isClientSide && blockEntityType.equals(Exposure.BlockEntityTypes.LIGHTROOM.get()) ? LightroomBlockEntity::serverTick : null;
   }
}
