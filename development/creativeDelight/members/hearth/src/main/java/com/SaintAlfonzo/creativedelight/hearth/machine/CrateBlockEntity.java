package com.SaintAlfonzo.creativedelight.hearth.machine;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/** Bottle Crate: 9 slots per half, one item per slot; hoppers in from the top and sides, out from below (3.1). */
public class CrateBlockEntity extends SyncedBlockEntity implements Clearable {
    public static final int PER_HALF = 9;
    public static final int TOTAL = 18;
    public static final TagKey<Item> BOTTLES = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("hearthandharvest", "bottles"));
    public static final TagKey<Item> CRATEABLE = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("hearthandharvest", "crateable_items"));
    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL, ItemStack.EMPTY);
    private final IItemHandler insertHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return activeSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < getSlots() ? items.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot >= getSlots() || !isItemValid(slot, stack) || !items.get(slot).isEmpty()) return stack;
            if (!simulate) {
                items.set(slot, ItemHandlerHelper.copyStackWithSize(stack, 1));
                sync();
            }
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return accepts(stack);
        }
    };
    private final IItemHandler extractHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return PER_HALF;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < PER_HALF ? items.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot >= PER_HALF || amount <= 0 || items.get(slot).isEmpty()) return ItemStack.EMPTY;
            ItemStack current = items.get(slot);
            int taken = Math.min(amount, current.getCount());
            ItemStack result = ItemHandlerHelper.copyStackWithSize(current, taken);
            if (!simulate) {
                items.set(slot, ItemStack.EMPTY);
                sync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };
    private final LazyOptional<IItemHandler> insertCap = LazyOptional.of(() -> insertHandler);
    private final LazyOptional<IItemHandler> extractCap = LazyOptional.of(() -> extractHandler);

    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(HHMachines.CRATE_BE.get(), pos, state);
    }

    public static boolean accepts(ItemStack stack) {
        return stack.is(BOTTLES) || stack.is(CRATEABLE);
    }

    private int activeSlots() {
        BlockState state = getBlockState();
        return state.hasProperty(CrateBlock.TYPE) && state.getValue(CrateBlock.TYPE) == SlabType.DOUBLE ? TOTAL : PER_HALF;
    }

    public NonNullList<ItemStack> items() {
        return items;
    }

    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    /** Sets a slot from a click (one item) and sounds it. */
    public void setItem(int slot, ItemStack stack) {
        boolean wasEmpty = items.get(slot).isEmpty();
        items.set(slot, stack.isEmpty() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stack, 1));
        sync();
        if (level != null && !level.isClientSide && wasEmpty != stack.isEmpty()) {
            level.playSound(null, worldPosition, wasEmpty ? SoundEvents.ITEM_FRAME_ADD_ITEM : SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    SoundSource.BLOCKS, 0.6F, 0.9F + level.random.nextFloat() * 0.2F);
        }
    }

    public void loadItems(CompoundTag tag) {
        clearContent();
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItems(tag);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return (side == Direction.DOWN ? extractCap : insertCap).cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        insertCap.invalidate();
        extractCap.invalidate();
    }
}
