package com.SaintAlfonzo.creativedelight.oven;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/** FD's CookingPotResultSlot: take-only, and taking awards the used recipes and their experience. */
public class OvenResultSlot extends SlotItemHandler {

    private final OvenBlockEntity oven;
    private final Player player;
    private int removeCount;

    public OvenResultSlot(Player player, OvenBlockEntity oven, IItemHandler inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.oven = oven;
        this.player = player;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        checkTakeAchievements(stack);
        super.onTake(thePlayer, stack);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(player.level, player, removeCount);
        if (!player.level.isClientSide) {
            oven.awardUsedRecipes(player);
        }
        removeCount = 0;
    }
}
