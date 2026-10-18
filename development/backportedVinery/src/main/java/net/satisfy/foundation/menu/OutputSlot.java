package net.satisfy.foundation.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OutputSlot extends Slot {
    private final Player player;
    private int removeCount;

    public OutputSlot(Player player, Container container, int index, int x, int y) {
        super(container, index, x, y);
        this.player = player;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        checkTakeAchievements(stack);
        super.onTake(player, stack);
    }

    @Override
    protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(player.level, player, removeCount);
        if (player.level instanceof ServerLevel serverLevel && container instanceof ExperienceSource source) {
            source.dropExperience(serverLevel, player.position());
        }
        removeCount = 0;
    }
}
