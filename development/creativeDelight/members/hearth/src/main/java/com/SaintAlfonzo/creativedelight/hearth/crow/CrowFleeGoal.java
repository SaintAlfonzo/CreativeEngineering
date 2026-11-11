package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

/** Wild crows flee players, villagers and the alarm source (C2). */
public class CrowFleeGoal extends Goal {
    private static final double FLEE_SPEED = 1.4D;
    private final CrowEntity crow;
    @Nullable
    private LivingEntity threat;
    @Nullable
    private Vec3 target;
    private int scanCooldown;

    public CrowFleeGoal(CrowEntity crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || !CrowConfig.FLEEING.get() || !CrowLogic.isWild(crow) || !(crow.level instanceof ServerLevel level)) {
            return false;
        }
        boolean fresh = wariness.consumeFreshAlarm();
        if (--scanCooldown > 0 && !fresh) {
            return false;
        }
        scanCooldown = 10 + crow.getRandom().nextInt(10);
        LivingEntity nearest = nearestThreat(level, wariness);
        if (nearest == null) {
            return false;
        }
        Vec3 away = CrowLogic.fleeTarget(crow, nearest.getX(), nearest.getZ());
        if (away == null) {
            return false;
        }
        threat = nearest;
        target = away;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || threat == null || !CrowLogic.isWild(crow) || !(crow.level instanceof ServerLevel level)) {
            return false;
        }
        double radius = CrowLogic.threatRadius(crow, wariness, threat, CrowConfig.SCARE_RADIUS.get());
        if (radius <= 0.0D) {
            return false;
        }
        double stop = threat == wariness.getAlarmSource(level) ? radius * 1.25D : radius * 1.6D;
        return crow.distanceToSqr(threat) < stop * stop;
    }

    @Override
    public void start() {
        crow.setTarget(null);
        crow.getNavigation().stop();
        moveOn();
    }

    @Override
    public void stop() {
        threat = null;
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        crow.setTarget(null);
        if (target == null || crow.distanceToSqr(target) < 4.0D) {
            Vec3 next = threat == null ? null : CrowLogic.fleeTarget(crow, threat.getX(), threat.getZ());
            if (next != null) {
                target = next;
            }
        }
        moveOn();
    }

    private void moveOn() {
        if (target != null) {
            CrowLogic.fly(crow, target.x, target.y, target.z, FLEE_SPEED);
        }
    }

    @Nullable
    private LivingEntity nearestThreat(ServerLevel level, CrowWariness wariness) {
        double base = CrowConfig.SCARE_RADIUS.get();
        LivingEntity alarm = wariness.getAlarmSource(level);
        double scan = alarm != null ? Math.max(base, CrowLogic.ALARM_RANGE) : base;
        if (scan <= 0.0D) {
            return null;
        }
        List<LivingEntity> candidates = crow.level.getEntitiesOfClass(LivingEntity.class, crow.getBoundingBox().inflate(scan), entity -> {
            double radius = CrowLogic.threatRadius(crow, wariness, entity, base);
            return radius > 0.0D && crow.distanceToSqr(entity) <= radius * radius;
        });
        if (candidates.isEmpty()) {
            return null;
        }
        if (alarm != null && candidates.contains(alarm)) {
            return alarm;
        }
        return crow.level.getNearestEntity(candidates, TargetingConditions.forNonCombat(), crow, crow.getX(), crow.getY(), crow.getZ());
    }
}
