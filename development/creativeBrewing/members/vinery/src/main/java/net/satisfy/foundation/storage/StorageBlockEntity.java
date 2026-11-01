package net.satisfy.foundation.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.foundation.util.LibUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Inventory holder for {@link StorageBlock}. Syncs to tracking players on every change. */
public class StorageBlockEntity extends BlockEntity implements Clearable {
    /**
     * Port: what upstream Vinery did with a mixin on {@link #setStack} / {@link #removeStack} (wine aging in storage).
     * Called on the server only.
     */
    public interface Listener {
        void onInsert(ItemStack stack, Level level, BlockPos pos);

        void onRemove(ItemStack stack, Level level);
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    public static void addListener(Listener listener) {
        LISTENERS.add(listener);
    }

    private int size;
    private NonNullList<ItemStack> inventory;
    private long wobbleStart = Long.MIN_VALUE / 2;
    private long[] slotWobbleStart = new long[0];

    public StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    }

    public StorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        this.size = size;
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
    }

    public ItemStack removeStack(int slot) {
        ItemStack stack = this.inventory.set(slot, ItemStack.EMPTY);
        if (level != null && !level.isClientSide()) {
            for (Listener listener : LISTENERS) {
                listener.onRemove(stack, level);
            }
        }
        this.setChanged();
        return stack;
    }

    public void setStack(int slot, ItemStack stack) {
        if (level != null && !level.isClientSide()) {
            for (Listener listener : LISTENERS) {
                listener.onInsert(stack, level, worldPosition);
            }
        }
        this.inventory.set(slot, stack);
        this.setChanged();
    }

    /** Port: ends storage for every slot before the contents drop (the block broke). */
    public void stopAll() {
        if (level != null && !level.isClientSide()) {
            for (ItemStack stack : this.inventory) {
                for (Listener listener : LISTENERS) {
                    listener.onRemove(stack, level);
                }
            }
        }
    }

    @Override
    public void setChanged() {
        if (this.level instanceof ServerLevel serverLevel) {
            Packet<ClientGamePacketListener> updatePacket = this.getUpdatePacket();
            for (ServerPlayer player : LibUtil.tracking(serverLevel, this.getBlockPos())) {
                player.connection.send(updatePacket);
            }
        }
        super.setChanged();
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        super.load(compoundTag);
        NonNullList<ItemStack> previous = this.inventory;
        int previousCount = previous == null ? -1 : countItems();
        this.size = compoundTag.contains("size") ? compoundTag.getInt("size") : this.size;
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.inventory);
        if (this.slotWobbleStart.length != this.size) {
            this.slotWobbleStart = new long[this.size];
            Arrays.fill(this.slotWobbleStart, Long.MIN_VALUE / 2);
        }
        if (previous != null && this.level != null && this.level.isClientSide()) {
            long time = this.level.getGameTime();
            if (previousCount != countItems()) {
                this.wobbleStart = time;
            }
            for (int slot = 0; slot < this.size && slot < previous.size(); slot++) {
                if (previous.get(slot).isEmpty() && !this.inventory.get(slot).isEmpty()) {
                    this.slotWobbleStart[slot] = time;
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, this.inventory);
        nbt.putInt("size", this.size);
        super.saveAdditional(nbt);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setInventory(NonNullList<ItemStack> inventory) {
        for (int i = 0; i < inventory.size() && i < this.inventory.size(); ++i) {
            this.inventory.set(i, inventory.get(i));
        }
    }

    private int countItems() {
        int count = 0;
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public long getWobbleStart() {
        return this.wobbleStart;
    }

    public long getWobbleStart(int slot) {
        return slot >= 0 && slot < this.slotWobbleStart.length ? this.slotWobbleStart[slot] : Long.MIN_VALUE / 2;
    }

    public NonNullList<ItemStack> getInventory() {
        return this.inventory;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }
}
