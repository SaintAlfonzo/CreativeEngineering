package com.SaintAlfonzo.creativedelight.hearth.nest;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Four single-item slots, shown on the nest. Not exposed to hoppers (plan_hearth_crow_salt.md section 5). A generated nest also
 * keeps a loot table rolled into the slots the first time anything touches them (the RandomizableContainerBlockEntity pattern,
 * done by hand as the nest has no menu), and the game time of its last crow (plan_hearth_crow_swap.md 2.2 and 5.3).
 */
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
    @Nullable
    private ResourceLocation lootTable;
    private long lootSeed;
    private long lastCrow;

    public NestBlockEntity(BlockPos pos, BlockState state) {
        super(HHNestRegistry.NEST_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        unpackLoot();
        return inventory;
    }

    public void setLootTable(ResourceLocation table, long seed) {
        lootTable = table;
        lootSeed = seed;
        setChanged();
    }

    /** Rolls the stolen items in, once. Only on the server; the table is cleared first so the inserts below cannot recurse. */
    private void unpackLoot() {
        if (lootTable == null || !(level instanceof ServerLevel server)) {
            return;
        }
        ResourceLocation table = lootTable;
        lootTable = null;
        LootContext context = new LootContext.Builder(server)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withOptionalRandomSeed(lootSeed)
                .create(LootContextParamSets.CHEST);
        for (ItemStack stack : server.getServer().getLootTables().get(table).getRandomItems(context)) {
            for (int slot = 0; slot < SLOTS && !stack.isEmpty(); slot++) {
                stack = inventory.insertItem(slot, stack, false);
            }
        }
        setChanged();
    }

    public long getLastCrow() {
        return lastCrow;
    }

    public void setLastCrow(long gameTime) {
        lastCrow = gameTime;
        setChanged();
    }

    public ItemStack insert(ItemStack stack) {
        unpackLoot();
        ItemStack remaining = stack;
        for (int slot = 0; slot < SLOTS && !remaining.isEmpty(); slot++) {
            remaining = inventory.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    public ItemStack takeLast() {
        unpackLoot();
        for (int slot = SLOTS - 1; slot >= 0; slot--) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return inventory.extractItem(slot, stack.getCount(), false);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        unpackLoot();
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
        unpackLoot();
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
        if (lootTable != null) {
            tag.putString("LootTable", lootTable.toString());
            tag.putLong("LootTableSeed", lootSeed);
        }
        tag.putLong("LastCrow", lastCrow);
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
        lootTable = tag.contains("LootTable") ? ResourceLocation.tryParse(tag.getString("LootTable")) : null;
        lootSeed = tag.getLong("LootTableSeed");
        lastCrow = tag.getLong("LastCrow");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.remove("LootTable");
        tag.remove("LootTableSeed");
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
