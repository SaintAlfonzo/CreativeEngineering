package com.sammy.minersdelight.content.block.sticky_basket;

import com.sammy.minersdelight.registry.MDBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import vectorwing.farmersdelight.common.block.entity.Basket;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;

/**
 * The Sticky Basket's 27-slot store: like Farmer's Delight's basket it pulls item entities from the block space it
 * faces, every 8 ticks, while not powered.
 * <p>
 * Create Central Kitchen hooks {@link #setTransferCooldown(int)} (head) and invokes {@code isOnTransferCooldown()}: keep
 * both names and signatures.
 */
public class StickyBasketBlockEntity extends RandomizableContainerBlockEntity implements Basket {
    private static final int COOLDOWN = 8;
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int transferCooldown = -1;

    public StickyBasketBlockEntity(BlockPos pos, BlockState state) {
        super(MDBlockEntities.STICKY_BASKET.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(compound)) {
            ContainerHelper.loadAllItems(compound, items);
        }
        transferCooldown = compound.getInt("TransferCooldown");
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, items);
        }
        compound.putInt("TransferCooldown", transferCooldown);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return TextUtils.getTranslation("container.basket");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return ChestMenu.threeRows(id, playerInventory, this);
    }

    // ---- collecting ----

    public static void pushItemsTick(Level level, BlockPos pos, BlockState state, StickyBasketBlockEntity basket) {
        basket.transferCooldown--;
        if (!basket.isOnTransferCooldown()) {
            basket.setTransferCooldown(0);
            int facing = state.getValue(BlockStateProperties.FACING).get3DDataValue();
            basket.tryCollect(() -> pullItems(level, basket, facing));
        }
    }

    private void tryCollect(java.util.function.BooleanSupplier collect) {
        if (level == null || level.isClientSide || isOnTransferCooldown() || !getBlockState().getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        if (!isFull() && collect.getAsBoolean()) {
            setTransferCooldown(COOLDOWN);
            setChanged();
        }
    }

    private boolean isFull() {
        for (ItemStack stack : items) {
            if (stack.isEmpty() || stack.getCount() != stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    /** True when one item entity in the collection area was taken whole. */
    public static boolean pullItems(Level level, Basket basket, int facing) {
        for (ItemEntity item : itemsInArea(level, basket, facing)) {
            if (captureItem(basket, item)) {
                return true;
            }
        }
        return false;
    }

    private static List<ItemEntity> itemsInArea(Level level, Basket basket, int facing) {
        return basket.getFacingCollectionArea(facing).toAabbs().stream()
                .map(box -> box.move(basket.getLevelX() - 0.5, basket.getLevelY() - 0.5, basket.getLevelZ() - 0.5))
                .flatMap((AABB box) -> level.getEntitiesOfClass(ItemEntity.class, box, EntitySelector.ENTITY_STILL_ALIVE).stream())
                .toList();
    }

    public static boolean captureItem(Container container, ItemEntity item) {
        ItemStack rest = insertAll(container, item.getItem().copy());
        if (rest.isEmpty()) {
            item.discard();
            return true;
        }
        item.setItem(rest);
        return false;
    }

    private static ItemStack insertAll(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize() && !stack.isEmpty(); slot++) {
            stack = insert(container, stack, slot);
        }
        return stack;
    }

    private static ItemStack insert(Container container, ItemStack stack, int slot) {
        if (!container.canPlaceItem(slot, stack)
                || container instanceof WorldlyContainer worldly && !worldly.canPlaceItemThroughFace(slot, stack, (Direction) null)) {
            return stack;
        }
        ItemStack present = container.getItem(slot);
        boolean wasEmpty = container.isEmpty();
        boolean moved = false;
        if (present.isEmpty()) {
            container.setItem(slot, stack);
            stack = ItemStack.EMPTY;
            moved = true;
        } else if (canMerge(present, stack)) {
            int count = Math.min(stack.getCount(), stack.getMaxStackSize() - present.getCount());
            stack.shrink(count);
            present.grow(count);
            moved = count > 0;
        }
        if (moved) {
            if (wasEmpty && container instanceof StickyBasketBlockEntity basket && !basket.mayTransfer()) {
                basket.setTransferCooldown(COOLDOWN);
            }
            container.setChanged();
        }
        return stack;
    }

    private static boolean canMerge(ItemStack into, ItemStack stack) {
        return into.getItem() == stack.getItem() && into.getDamageValue() == stack.getDamageValue()
                && into.getCount() <= into.getMaxStackSize() && ItemStack.tagMatches(into, stack);
    }

    public void setTransferCooldown(int ticks) {
        transferCooldown = ticks;
    }

    private boolean isOnTransferCooldown() {
        return transferCooldown > 0;
    }

    public boolean mayTransfer() {
        return transferCooldown > COOLDOWN;
    }

    @Override
    public double getLevelX() {
        return worldPosition.getX() + 0.5;
    }

    @Override
    public double getLevelY() {
        return worldPosition.getY() + 0.5;
    }

    @Override
    public double getLevelZ() {
        return worldPosition.getZ() + 0.5;
    }
}
