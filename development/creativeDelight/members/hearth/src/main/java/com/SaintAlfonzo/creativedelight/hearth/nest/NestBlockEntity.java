package com.SaintAlfonzo.creativedelight.hearth.nest;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/** Four single-item slots, shown on the nest. Not exposed to hoppers (plan_hearth_crow_salt.md section 5). */
public class NestBlockEntity extends BlockEntity {
    public static final int SLOTS = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (isEmpty()) {
                crowStash = false;
            }
            markUpdated();
        }
    };
    private boolean crowStash;

    public NestBlockEntity(BlockPos pos, BlockState state) {
        super(HHNestRegistry.NEST_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack insert(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < SLOTS && !remaining.isEmpty(); slot++) {
            remaining = inventory.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    public ItemStack takeLast() {
        for (int slot = SLOTS - 1; slot >= 0; slot--) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return inventory.extractItem(slot, stack.getCount(), false);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasCrowStash() {
        return crowStash;
    }

    public void markCrowStash() {
        crowStash = true;
        markUpdated();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D, stack.copy());
            }
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putBoolean("CrowStash", crowStash);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            if (inventory.getSlots() != SLOTS) {
                inventory.setSize(SLOTS);
            }
        }
        crowStash = tag.getBoolean("CrowStash");
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
}
