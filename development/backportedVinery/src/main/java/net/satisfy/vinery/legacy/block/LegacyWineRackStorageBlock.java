package net.satisfy.vinery.legacy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.satisfy.vinery.legacy.entity.LegacyWineRackStorageBlockEntity;

public class LegacyWineRackStorageBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   private final java.util.function.Supplier<SoundEvent> openSound;
   private final java.util.function.Supplier<SoundEvent> closeSound;

   public LegacyWineRackStorageBlock(Properties settings, java.util.function.Supplier<SoundEvent> openSound, java.util.function.Supplier<SoundEvent> closeSound) {
      super(settings);
      this.openSound = openSound;
      this.closeSound = closeSound;
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(OPEN, false));
   }

   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (world.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         if (world.getBlockEntity(pos) instanceof LegacyWineRackStorageBlockEntity blockEntity1) {
            player.openMenu(blockEntity1);
         }

         return InteractionResult.CONSUME;
      }
   }

   public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.is(newState.getBlock())) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof Container) {
            Containers.dropContents(world, pos, (Container)blockEntity);
            world.updateNeighbourForOutputSignal(pos, this);
         }

         super.onRemove(state, world, pos, newState, moved);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new LegacyWineRackStorageBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomHoverName() && world.getBlockEntity(pos) instanceof LegacyWineRackStorageBlockEntity blockEntity1) {
         blockEntity1.setCustomName(itemStack.getHoverName());
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN});
   }

   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return (BlockState)this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   public void playSound(Level world, BlockPos pos, boolean open) {
      world.playSound(null, pos, (open ? this.openSound : this.closeSound).get(), SoundSource.BLOCKS, 1.0F, 1.0F);
   }
}
