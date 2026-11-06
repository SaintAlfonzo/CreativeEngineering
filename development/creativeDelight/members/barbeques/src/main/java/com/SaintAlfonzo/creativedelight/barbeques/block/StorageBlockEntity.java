package com.SaintAlfonzo.creativedelight.barbeques.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Ingredients Basin and Tray storage (1.3.0 StorageTileBlockEntity + StorageTile): click slots, item handler, synced. */
public abstract class StorageBlockEntity extends BlockEntity implements BlockSlot {
    public final ItemStackHandler items;
    private final LazyOptional<IItemHandler> handler;

    protected StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        items = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                notifyTile();
            }
        };
        handler = LazyOptional.of(() -> items);
    }

    @Override
    public int size() {
        return items.getSlots();
    }

    public ItemStack getStack(int i) {
        return items.getStackInSlot(i);
    }

    public void setStack(int i, ItemStack stack) {
        items.setStackInSlot(i, stack);
    }

    /** The basin's skewering click; the tray has none. */
    public abstract boolean specialClick(Player player, int slot, InteractionHand hand);

    /** 1.3.0 StorageTile#insert: same item only, all of the hand or (sneaking) one. */
    public boolean insert(Level level, int i, ItemStack hand, boolean all) {
        if (hand.isEmpty()) {
            return false;
        }
        ItemStack stored = getStack(i);
        if (!stored.isEmpty() && !ItemStack.isSameItemSameTags(stored, hand)) {
            return false;
        }
        int old = stored.getCount();
        int split = Math.min(hand.getMaxStackSize() - old, all ? hand.getCount() : 1);
        if (split <= 0) {
            return false;
        }
        if (!level.isClientSide) {
            ItemStack copy = hand.split(split);
            copy.grow(old);
            setStack(i, copy);
        }
        return true;
    }

    public void notifyTile() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? handler.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }
}
