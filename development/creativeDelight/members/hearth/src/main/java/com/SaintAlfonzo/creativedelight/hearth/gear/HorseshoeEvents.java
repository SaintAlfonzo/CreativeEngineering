package com.SaintAlfonzo.creativedelight.hearth.gear;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShearsItem;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Shod horses (3.1, N22): Fire Resistance, cactus and berry bush immunity, slowing blocks canceled, and +25 % speed, which
 * upstream does not have. The shoe lives in the horse's persistent data. Speed modifiers are transient and re-applied, so
 * they are never written to a save.
 */
public final class HorseshoeEvents {
    private static final String KEY = "hearthandharvest:horseshoe";
    private static final UUID GROUND = UUID.fromString("6a1c7f42-5b1e-4f7e-9c11-1f3c0e2a7a01");
    private static final UUID SHOD = UUID.fromString("6a1c7f42-5b1e-4f7e-9c11-1f3c0e2a7a02");
    private static final double SHOD_SPEED = 0.25D;

    private HorseshoeEvents() {
    }

    public static boolean isShod(AbstractHorse horse) {
        return horse.getPersistentData().contains(KEY);
    }

    public static void setShoe(AbstractHorse horse, ItemStack shoe) {
        horse.getPersistentData().put(KEY, shoe.save(new CompoundTag()));
    }

    public static ItemStack takeShoe(AbstractHorse horse) {
        ItemStack shoe = ItemStack.of(horse.getPersistentData().getCompound(KEY));
        horse.getPersistentData().remove(KEY);
        AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(GROUND);
            speed.removeModifier(SHOD);
        }
        return shoe;
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse horse) || horse.level.isClientSide || !isShod(horse)) {
            return;
        }
        if (!horse.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            horse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
        }
        AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (speed.getModifier(SHOD) == null) {
            speed.addTransientModifier(new AttributeModifier(SHOD, "Horseshoe", SHOD_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        BlockPos below = horse.blockPosition().below();
        float factor = horse.level.getBlockState(below).getBlock().getSpeedFactor();
        speed.removeModifier(GROUND);
        if (factor < 1.0F && factor > 0.0F && horse.isOnGround()) {
            speed.addTransientModifier(new AttributeModifier(GROUND, "Horseshoe grip", 1.0F / factor - 1.0F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse && isShod(horse)) {
            DamageSource source = event.getSource();
            // By id, not identity: the source reaching here need not be the vanilla constant (SQ-HH-1505).
            String id = source.getMsgId();
            if (DamageSource.CACTUS.getMsgId().equals(id) || DamageSource.SWEET_BERRY_BUSH.getMsgId().equals(id)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Sneak-click on a shod tamed horse with an empty hand, shears or a horseshoe takes the shoe back. Runs as an
     * EntityInteract handler so it comes before the horse opens its inventory (plan_bugfix_1114 3.6, TODO #69).
     */
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractHorse horse) || !horse.isTamed() || !isShod(horse)) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        boolean tool = held.getItem() instanceof HorseshoeItem || held.getItem() instanceof ShearsItem;
        if (!player.isShiftKeyDown() || !(tool || held.isEmpty() && event.getHand() == InteractionHand.MAIN_HAND)) {
            return;
        }
        if (!horse.level.isClientSide) {
            ItemStack shoe = takeShoe(horse);
            if (!player.getInventory().add(shoe)) {
                player.drop(shoe, false);
            }
            horse.level.playSound(null, horse, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(horse.level.isClientSide));
        event.setCanceled(true);
    }

    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse && !horse.level.isClientSide && isShod(horse)) {
            horse.spawnAtLocation(takeShoe(horse));
        }
    }
}
