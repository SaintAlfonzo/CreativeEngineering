package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.HHSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

/** Event side of the crow systems: carry timer, alarm, hand feeding, corn breeding and the rules that keep stolen loot alive. Spawning is the nests' (NestCrows). */
public final class CrowEvents {
    private CrowEvents() {
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof CrowEntity crow) || crow.level.isClientSide) {
            return;
        }
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        wariness.tick();
        if (!wariness.isCarrying()) {
            return;
        }
        if (!wariness.getHeldBack().isEmpty()) {
            crow.setItemSlot(EquipmentSlot.MAINHAND, wariness.getHeldBack());
            wariness.setHeldBack(ItemStack.EMPTY);
        } else if (crow.getMainHandItem().isEmpty()) {
            wariness.stopCarrying();
        } else if (wariness.tickCarry()) {
            CrowLogic.dropLoot(crow, wariness);
        }
    }

    /** Sets off the flock, and lets go of carried loot as a protected drop or, with {@code crow.dropOnHurt} off, into a one-tick hold. */
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof CrowEntity crow) || crow.level.isClientSide) {
            return;
        }
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        if (CrowConfig.FLOCK_ALARM.get() && !crow.isTame() && event.getSource().getEntity() instanceof LivingEntity attacker && attacker != crow) {
            raiseAlarm(crow, wariness, attacker);
        }
        if (wariness.isCarrying() && !crow.getMainHandItem().isEmpty()) {
            if (CrowConfig.DROP_ON_HURT.get()) {
                CrowLogic.dropLoot(crow, wariness);
            } else {
                wariness.setHeldBack(crow.getMainHandItem().copy());
                crow.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }
    }

    private static void raiseAlarm(CrowEntity crow, CrowWariness wariness, LivingEntity attacker) {
        wariness.alarm(attacker);
        if (!crow.isSilent()) {
            crow.playSound(HHSounds.CROW_SQUAWK.get(), 2.0F, 1.25F);
        }
        for (CrowEntity other : crow.level.getEntitiesOfClass(CrowEntity.class, crow.getBoundingBox().inflate(CrowLogic.ALARM_RANGE),
                entity -> entity != crow && !entity.isTame())) {
            CrowWariness state = CrowWariness.of(other);
            if (state != null) {
                state.alarm(attacker);
                if (!other.isSilent() && other.getRandom().nextInt(3) == 0) {
                    other.playSound(HHSounds.CROW_SQUAWK.get(), 1.5F, 1.15F + (other.getRandom().nextFloat() - other.getRandom().nextFloat()) * 0.2F);
                }
            }
        }
    }

    /** Carried or held-back loot leaves as a protected item. Runs before the equipment drop, so the guaranteed drop finds nothing left. */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof CrowEntity crow && !crow.level.isClientSide) {
            CrowWariness wariness = CrowWariness.of(crow);
            if (wariness != null && wariness.isCarrying()) {
                CrowLogic.dropLoot(crow, wariness);
            }
        }
    }

    public static void onAllowDespawn(LivingSpawnEvent.AllowDespawn event) {
        if (event.getEntity() instanceof CrowEntity crow) {
            CrowWariness wariness = CrowWariness.of(crow);
            if (wariness != null && wariness.isCarrying()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof CrowEntity crow)) {
            return;
        }
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = event.getItemStack();
        if (crow.isTame()) {
            if (CrowConfig.CORN_BREEDING.get() && (stack.is(CrowTags.CORN) || stack.is(CrowTags.CORN_SEEDS)) && crow.isOwnedBy(player) && crow.getAge() == 0 && crow.canFallInLove()) {
                if (!crow.level.isClientSide) {
                    consume(player, stack);
                    crow.setInLove(player);
                }
                succeed(event, crow);
            }
            return;
        }
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        if (wariness.isCarrying() && !crow.getMainHandItem().isEmpty()) {
            if (!crow.level.isClientSide) {
                CrowLogic.dropLoot(crow, wariness);
            }
            succeed(event, crow);
        } else if (CrowConfig.HAND_FEEDING.get() && stack.is(CrowTags.CROW_FOOD)) {
            if (!crow.level.isClientSide) {
                consume(player, stack);
                if (!crow.isSilent()) {
                    crow.playSound(HHSounds.CROW_EAT.get(), 1.0F, 1.0F + (crow.getRandom().nextFloat() - crow.getRandom().nextFloat()) * 0.2F);
                }
                CrowLogic.tryTame(crow, player, 5);
            }
            succeed(event, crow);
        }
    }

    private static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void succeed(PlayerInteractEvent.EntityInteract event, CrowEntity crow) {
        event.setCancellationResult(InteractionResult.sidedSuccess(crow.level.isClientSide));
        event.setCanceled(true);
    }
}
