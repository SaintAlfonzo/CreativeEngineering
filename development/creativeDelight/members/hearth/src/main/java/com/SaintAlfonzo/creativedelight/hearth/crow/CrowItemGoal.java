package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.Comparator;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.item.ItemEntity;

/** H&H 1.4.1 base of the item goals: throttled scans and re-pathing. */
public abstract class CrowItemGoal extends Goal {
    protected final CrowEntity crow;
    protected final double speed;
    private final int scanInterval;
    private final double scanRadius;
    private final int repathInterval;
    @Nullable
    protected ItemEntity targetItem;
    private int scanCooldown;
    private int repathTimer;

    protected CrowItemGoal(CrowEntity crow, double speed, int scanInterval, double scanRadius, int repathInterval, EnumSet<Flag> flags) {
        this.crow = crow;
        this.speed = speed;
        this.scanInterval = scanInterval;
        this.scanRadius = scanRadius;
        this.repathInterval = repathInterval;
        this.setFlags(flags);
    }

    protected abstract boolean isValidTarget(@Nullable ItemEntity var1);

    protected boolean isWorthApproaching(ItemEntity item) {
        return true;
    }

    protected boolean scanReady() {
        if (--this.scanCooldown > 0) {
            return false;
        } else {
            this.scanCooldown = this.scanInterval;
            return true;
        }
    }

    protected void setScanCooldown(int ticks) {
        this.scanCooldown = ticks;
    }

    @Nullable
    protected ItemEntity findNearestTarget() {
        return this.crow
            .level
            .getEntitiesOfClass(ItemEntity.class, this.crow.getBoundingBox().inflate(this.scanRadius), this::isValidTarget)
            .stream()
            .sorted(Comparator.comparingDouble(this.crow::distanceToSqr))
            .filter(this::isWorthApproaching)
            .findFirst()
            .orElse(null);
    }

    protected boolean approach(Entity target, double arriveDistanceSqr) {
        if (this.crow.distanceToSqr(target) <= arriveDistanceSqr) {
            this.crow.getNavigation().stop();
            this.repathTimer = 0;
            return true;
        } else {
            this.repath(target, this.speed);
            return false;
        }
    }

    protected void repath(Entity target, double moveSpeed) {
        if (--this.repathTimer <= 0 || this.crow.getNavigation().isDone()) {
            this.repathTimer = this.repathInterval;
            this.crow.getNavigation().moveTo(target, moveSpeed);
        }
    }

    protected void repath(double x, double y, double z, double moveSpeed, int interval) {
        if (--this.repathTimer <= 0 || this.crow.getNavigation().isDone()) {
            this.repathTimer = interval;
            this.crow.getNavigation().moveTo(x, y, z, moveSpeed);
        }
    }

    protected void resetRepath() {
        this.repathTimer = 0;
    }

    protected void lookAtTarget(double yOffset) {
        if (this.targetItem != null) {
            this.crow.getLookControl().setLookAt(this.targetItem.getX(), this.targetItem.getY() + yOffset, this.targetItem.getZ());
        }
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void stop() {
        this.targetItem = null;
        this.crow.getNavigation().stop();
    }
}
