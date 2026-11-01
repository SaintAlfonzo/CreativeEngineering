package net.satisfy.vinery.core.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;

/** Port: 1.20.5+ item helpers that 1.19.2 doesn't have. */
public final class PortUtil {
    private PortUtil() {
    }

    /** 1.20.5's {@code ItemTags.SHOVELS}: anything that can dig like a shovel. */
    public static boolean isShovel(ItemStack stack) {
        return stack.canPerformAction(ToolActions.SHOVEL_DIG);
    }

    /** 1.20.5's {@code ItemStack#hurtAndBreak(int, LivingEntity, EquipmentSlot)}. */
    public static void hurt(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
        stack.hurtAndBreak(amount, entity, e -> e.broadcastBreakEvent(slot));
    }

    public static void hurt(ItemStack stack, int amount, LivingEntity entity, InteractionHand hand) {
        stack.hurtAndBreak(amount, entity, e -> e.broadcastBreakEvent(hand));
    }

    /** 1.20.5's {@code ItemStack#consume}: shrinks unless the player is in creative. */
    public static void consume(ItemStack stack, int amount, Player player) {
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(amount);
        }
    }
}
