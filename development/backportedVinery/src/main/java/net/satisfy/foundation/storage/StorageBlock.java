package net.satisfy.foundation.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.foundation.block.FacingBlock;
import net.satisfy.foundation.util.ItemInteractionResult;
import net.satisfy.foundation.util.ShapeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Block with a small display inventory (shelves, racks...). Where you click
 * on the front face decides the slot, see {@link #getSection(Float, Float)}.
 * Rendering goes through {@link StorageTypeRenderer}s registered under {@link #type()}.
 */
public abstract class StorageBlock extends FacingBlock implements EntityBlock {

    public StorageBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        return ItemInteractionResult.dispatch(this::useItemOn, this::useWithoutItem, state, level, pos, player, hand, hit);
    }

    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StorageBlockEntity storageEntity) {
            Optional<Tuple<Float, Float>> hitCoordinates = ShapeUtil.getRelativeHitCoordinatesForBlockFace(hit, state.getValue(FACING), this.unAllowedDirections());
            if (hitCoordinates.isEmpty()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            Tuple<Float, Float> coordinates = hitCoordinates.get();
            int section = this.getSection(coordinates.getA(), coordinates.getB());
            if (section == Integer.MIN_VALUE) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (!storageEntity.getInventory().get(section).isEmpty()) {
                this.remove(world, pos, player, storageEntity, section);
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }

            if (!stack.isEmpty() && this.canInsertStack(stack)) {
                this.add(world, pos, player, storageEntity, stack, section);
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }

            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    public void add(Level level, BlockPos pos, Player player, StorageBlockEntity storageEntity, ItemStack itemStack, int index) {
        if (!level.isClientSide) {
            SoundEvent sound = this.getAddSound(level, pos, player, index);
            storageEntity.setStack(index, itemStack.split(1));
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                itemStack.grow(1);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    public void remove(Level level, BlockPos pos, Player player, StorageBlockEntity storageEntity, int index) {
        if (!level.isClientSide) {
            ItemStack removedStack = storageEntity.removeStack(index);
            SoundEvent sound = this.getRemoveSound(level, pos, player, index);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(removedStack)) {
                player.drop(removedStack, false);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level world, @NotNull BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StorageBlockEntity storageEntity) {
                if (world instanceof ServerLevel) {
                    storageEntity.stopAll();
                    Containers.dropContents(world, pos, storageEntity.getInventory());
                }

                world.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    public SoundEvent getAddSound(Level level, BlockPos pos, Player player, int index) {
        return SoundEvents.WOOD_PLACE;
    }

    public SoundEvent getRemoveSound(Level level, BlockPos pos, Player player, int index) {
        return SoundEvents.WOOD_BREAK;
    }

    public abstract BlockEntityType<?> blockEntityType();

    /** Amount of slots. */
    public abstract int size();

    /** Id of the {@link StorageTypeRenderer} to use. */
    public abstract ResourceLocation type();

    /** Faces that can not be clicked. */
    public abstract Direction[] unAllowedDirections();

    public abstract boolean canInsertStack(ItemStack stack);

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new StorageBlockEntity(this.blockEntityType(), pos, state, this.size());
    }

    /**
     * Maps relative hit coords (0-1) to a slot.
     * @return slot index or {@link Integer#MIN_VALUE} for none
     */
    public abstract int getSection(Float x, Float y);
}
