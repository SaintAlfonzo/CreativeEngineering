package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Fits a tamed horse (sneak-use takes the shoe back), or charges and throws (hearth_and_harvest.md 3.1). */
public class HorseshoeItem extends Item {
    private static final int MIN_THROW_TICKS = 5;

    public HorseshoeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AbstractHorse horse) || !horse.isTamed()) {
            return InteractionResult.PASS;
        }
        if (player.level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean shod = HorseshoeEvents.isShod(horse);
        if (shod && player.isCrouching()) {
            ItemStack shoe = HorseshoeEvents.takeShoe(horse);
            if (!player.getInventory().add(shoe)) {
                player.drop(shoe, false);
            }
            horse.level.playSound(null, horse, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0F, 0.8F);
            return InteractionResult.SUCCESS;
        }
        if (shod) {
            return InteractionResult.PASS;
        }
        ItemStack one = stack.copy();
        one.setCount(1);
        HorseshoeEvents.setShoe(horse, one);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        horse.level.playSound(null, horse, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0F, 1.2F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int charged = getUseDuration(stack) - timeLeft;
        if (charged < MIN_THROW_TICKS || level.isClientSide) {
            return;
        }
        float power = Mth.clamp(charged / 20.0F, 0.25F, 1.0F) * 2.0F;
        ThrownHorseshoe thrown = new ThrownHorseshoe(level, player, stack);
        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power, 1.0F);
        level.addFreshEntity(thrown);
        level.playSound(null, thrown, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 0.9F + power * 0.15F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
