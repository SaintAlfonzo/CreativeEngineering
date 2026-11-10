package com.SaintAlfonzo.creativedelight.hearth.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;

/** Hold to charge and release to throw, the trident's flow; a tool one point from breaking cannot be thrown. */
final class ThrowableToolItem {
    private ThrowableToolItem() {
    }

    static InteractionResultHolder<ItemStack> startCharge(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    interface Factory {
        AbstractArrow create(Level level, Player player, ItemStack stack);
    }

    static void release(ItemStack stack, Level level, LivingEntity entity, int charged, int minCharge, float speed, SoundEvent sound, float pitch, Factory factory) {
        if (!(entity instanceof Player player) || charged < minCharge || level.isClientSide) return;
        InteractionHand hand = player.getUsedItemHand();
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        AbstractArrow thrown = factory.create(level, player, stack);
        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed, 1.0F);
        level.addFreshEntity(thrown);
        level.playSound(null, thrown, sound, SoundSource.PLAYERS, 1.0F, pitch);
        if (!player.getAbilities().instabuild) player.getInventory().removeItem(stack);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    }
}
