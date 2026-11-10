package com.SaintAlfonzo.creativedelight.hearth.event;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.registry.ModItems;

/** Goat and bottle milking, chicken plucking, pig and rabbit litters (hearth_and_harvest.md 2.2, 3.2). */
public final class AnimalEvents {
    private static final String PLUCK_READY = "hearthandharvestPluckReady";
    private static final int PLUCK_COOLDOWN = 600;
    /** Fungi Delight's flag on a pig fed a truffle; read at HIGH priority, before its handler may clear it. */
    private static final String TRUFFLE_BRED = "FD_TruffleBred";

    private AnimalEvents() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (event.getTarget() instanceof Goat goat && !goat.isBaby()) {
            if (held.is(Items.BUCKET)) {
                milk(event, HHItems.GOAT_MILK_BUCKET.get(), SoundEvents.GOAT_MILK);
            } else if (held.is(Items.GLASS_BOTTLE) && HHConfig.BOTTLE_MILKING.get()) {
                milk(event, HHItems.GOAT_MILK_BOTTLE.get(), SoundEvents.GOAT_MILK);
            }
        } else if (event.getTarget() instanceof Cow cow && !cow.isBaby()) {
            if (held.is(Items.GLASS_BOTTLE) && HHConfig.BOTTLE_MILKING.get()) {
                milk(event, ModItems.MILK_BOTTLE.get(), SoundEvents.COW_MILK);
            }
        } else if (event.getTarget() instanceof Chicken chicken) {
            pluck(event, chicken, held);
        }
    }

    private static void milk(PlayerInteractEvent.EntityInteract event, Item result, SoundEvent sound) {
        Player player = event.getEntity();
        if (!player.level.isClientSide) {
            event.getTarget().playSound(sound, 1.0F, 1.0F);
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(event.getItemStack(), player, new ItemStack(result)));
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level.isClientSide));
        event.setCanceled(true);
    }

    /** Feeding a sneaking player's seeds still breeds: food in hand never plucks. */
    private static void pluck(PlayerInteractEvent.EntityInteract event, Chicken chicken, ItemStack held) {
        Player player = event.getEntity();
        if (!HHConfig.CHICKEN_PLUCKING.get() || !player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND || chicken.isFood(held)) return;
        if (!player.level.isClientSide) {
            long now = player.level.getGameTime();
            if (chicken.getPersistentData().getLong(PLUCK_READY) > now) {
                event.setCancellationResult(InteractionResult.FAIL);
                event.setCanceled(true);
                return;
            }
            chicken.getPersistentData().putLong(PLUCK_READY, now + PLUCK_COOLDOWN);
            chicken.spawnAtLocation(Items.FEATHER);
            if (player.level instanceof ServerLevel level) {
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.FEATHER)),
                        chicken.getX(), chicken.getY(0.5D), chicken.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
            }
            float damage = 0.0F;
            if (held.is(Items.SHEARS)) {
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            } else if (player.getRandom().nextDouble() < 0.25D) {
                damage = 1.0F;
            }
            chicken.hurt(DamageSource.playerAttack(player), damage);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level.isClientSide));
        event.setCanceled(true);
    }

    /** Priority HIGH: see TRUFFLE_BRED. */
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        Mob a = event.getParentA();
        Mob b = event.getParentB();
        boolean pigs = a instanceof Pig && b instanceof Pig;
        boolean rabbits = a instanceof Rabbit && b instanceof Rabbit;
        if ((!pigs && !rabbits) || !HHConfig.LITTERS.get() || !(a.level instanceof ServerLevel level)) return;
        boolean truffle = pigs && (a.getPersistentData().getBoolean(TRUFFLE_BRED) || b.getPersistentData().getBoolean(TRUFFLE_BRED));
        double chance = truffle ? HHConfig.TRUFFLE_LITTER_CHANCE.get() : HHConfig.LITTER_CHANCE.get();
        if (a.getRandom().nextDouble() >= chance) return;
        AgeableMob baby = ((Animal) a).getBreedOffspring(level, (AgeableMob) b);
        if (baby == null) return;
        baby.setBaby(true);
        baby.moveTo(a.getX(), a.getY(), a.getZ(), 0.0F, 0.0F);
        level.addFreshEntityWithPassengers(baby);
    }
}
