package net.satisfy.foundation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class CabinetBlockEntity extends RandomizableContainerBlockEntity {
    private static final int ROWS = 4;
    private static final int SIZE = ROWS * 9;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            setOpen(level, pos, state, true);
        }

        @Override
        protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            setOpen(level, pos, state, false);
        }

        @Override
        protected void openerCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int oldCount, int newCount) {
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == CabinetBlockEntity.this;
        }
    };

    public CabinetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 3);
        }
        if (state.getBlock() instanceof ContainerSoundBlock block) {
            block.playSound(level, pos, open);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, inventory);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        inventory = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, inventory);
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) {
        inventory = items;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int syncId, @NotNull Inventory playerInventory) {
        return new ChestMenu(MenuType.GENERIC_9x4, syncId, playerInventory, this, ROWS);
    }

    @Override
    public void startOpen(@NotNull Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openersCounter.incrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public void stopOpen(@NotNull Player player) {
        if (!remove && !player.isSpectator() && level != null) {
            openersCounter.decrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!remove && level != null) {
            openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }
}
