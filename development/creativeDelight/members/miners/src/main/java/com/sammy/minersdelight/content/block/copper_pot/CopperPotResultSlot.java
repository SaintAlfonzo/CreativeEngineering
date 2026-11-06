package com.sammy.minersdelight.content.block.copper_pot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/** The output slot: take-only; taking meals awards the recipes and the stored experience. */
public class CopperPotResultSlot extends SlotItemHandler {
    private final CopperPotBlockEntity pot;
    private final Player player;
    private int removeCount;

    public CopperPotResultSlot(Player player, CopperPotBlockEntity pot, IItemHandler inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.pot = pot;
        this.player = player;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    @NotNull
    public ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(@NotNull Player taker, @NotNull ItemStack stack) {
        checkTakeAchievements(stack);
        super.onTake(taker, stack);
    }

    @Override
    protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(player.level, player, removeCount);
        if (!player.level.isClientSide) {
            pot.awardUsedRecipes(player);
        }
        removeCount = 0;
    }
}
