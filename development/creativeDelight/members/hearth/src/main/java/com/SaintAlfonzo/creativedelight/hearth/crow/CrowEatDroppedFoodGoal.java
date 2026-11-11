package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.Comparator;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Crow food on the ground: peck for 24 ticks, heal 1, and 1 in 8 tames whoever threw it (C4). */
public class CrowEatDroppedFoodGoal extends Goal {
    private static final double SCAN_RADIUS = 10.0D;
    private static final double GIVE_UP_SQR = 400.0D;
    private static final double PECK_SQR = 1.0D;
    private static final int PECK_TICKS = 24;
    private static final int SCAN_TICKS = 20;
    private static final double SPEED = 1.1D;

    private final CrowEntity crow;
    @Nullable
    private ItemEntity target;
    private int scanCooldown;
    private int peckTimer;
    private int threatCheck;
    private boolean ate;

    public CrowEatDroppedFoodGoal(CrowEntity crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean eatsFood(ItemStack stack) {
        return CrowConfig.EAT_DROPPED_FOOD.get() && stack.is(CrowTags.CROW_FOOD);
    }

    private boolean unavailable(CrowWariness wariness) {
        return !CrowLogic.isWild(crow) || crow.level instanceof ServerLevel level && wariness.isAlarmed(level) || !crow.getMainHandItem().isEmpty();
    }

    @Override
    public boolean canUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || !CrowConfig.EAT_DROPPED_FOOD.get() || --scanCooldown > 0) {
            return false;
        }
        scanCooldown = SCAN_TICKS;
        if (unavailable(wariness)) {
            return false;
        }
        target = crow.level.getEntitiesOfClass(ItemEntity.class, crow.getBoundingBox().inflate(SCAN_RADIUS), this::valid).stream()
                .sorted(Comparator.comparingDouble(crow::distanceToSqr))
                .filter(item -> !CrowLogic.isSpotThreatened(crow, wariness, item.position()) && !CrowLogic.isGuarded(crow, item.blockPosition()))
                .findFirst().orElse(null);
        return target != null;
    }

    private boolean valid(@Nullable ItemEntity item) {
        return item != null && item.isAlive() && !item.getItem().isEmpty() && eatsFood(item.getItem());
    }

    @Override
    public boolean canContinueToUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        return wariness != null && !unavailable(wariness) && valid(target) && crow.distanceToSqr(target) < GIVE_UP_SQR;
    }

    @Override
    public void start() {
        peckTimer = -1;
        threatCheck = 10;
        ate = false;
    }

    @Override
    public void stop() {
        target = null;
        crow.getNavigation().stop();
        scanCooldown = ate ? 60 + crow.getRandom().nextInt(60) : SCAN_TICKS;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || !valid(target)) {
            return;
        }
        if (--threatCheck <= 0) {
            threatCheck = 10;
            if (CrowLogic.isSpotThreatened(crow, wariness, target.position())) {
                target = null;
                return;
            }
        }
        crow.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
        if (crow.distanceToSqr(target) > PECK_SQR) {
            peckTimer = -1;
            CrowLogic.moveTo(crow, target.getX(), target.getY(), target.getZ(), SPEED);
            return;
        }
        crow.getNavigation().stop();
        if (peckTimer < 0) {
            peckTimer = PECK_TICKS;
        }
        if (peckTimer % 8 == 0) {
            crumbs(target.getItem(), 3);
        }
        if (--peckTimer <= 0) {
            eat();
        }
    }

    private void eat() {
        Entity owner = target.getThrower() == null || !(crow.level instanceof ServerLevel level) ? null : level.getEntity(target.getThrower());
        ItemStack remaining = target.getItem().copy();
        ItemStack eaten = remaining.split(1);
        if (remaining.isEmpty()) {
            target.discard();
        } else {
            target.setItem(remaining);
        }
        crow.heal(1.0F);
        crumbs(eaten, 8);
        if (!crow.isSilent()) {
            crow.playSound(SoundEvents.PARROT_EAT, 1.0F, 1.0F + (crow.getRandom().nextFloat() - crow.getRandom().nextFloat()) * 0.2F);
        }
        if (owner instanceof Player player) {
            CrowLogic.tryTame(crow, player, 8);
        }
        ate = true;
        target = null;
    }

    private void crumbs(ItemStack stack, int count) {
        if (crow.level instanceof ServerLevel server && target != null) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), target.getX(), target.getY() + 0.1D, target.getZ(), count, 0.1D, 0.05D, 0.1D, 0.05D);
        }
    }
}
