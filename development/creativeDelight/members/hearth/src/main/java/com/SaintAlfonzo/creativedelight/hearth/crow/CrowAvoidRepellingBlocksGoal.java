package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Wild crows keep clear of {@code #creativedelight:repels_crows} (R2): flee inside the scare radius, stop at 1.6 times it. */
public class CrowAvoidRepellingBlocksGoal extends Goal {
    private static final double SPEED = 1.3D;
    private final TamableAnimal crow;
    @Nullable
    private BlockPos source;
    @Nullable
    private Vec3 target;
    private int scanCooldown;

    public CrowAvoidRepellingBlocksGoal(TamableAnimal crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!CrowLogic.isWild(crow) || CrowConfig.SCARE_RADIUS.get() <= 0) {
            return false;
        }
        if (--scanCooldown > 0) {
            return false;
        }
        scanCooldown = 10 + crow.getRandom().nextInt(10);
        BlockPos found = findNearest();
        if (found == null) {
            return false;
        }
        source = found;
        target = CrowLogic.fleeTarget(crow, found.getX() + 0.5D, found.getZ() + 0.5D);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (source == null || !CrowLogic.isWild(crow) || !crow.level.getBlockState(source).is(CrowTags.REPELS_CROWS)) {
            return false;
        }
        double stop = CrowConfig.SCARE_RADIUS.get() * 1.6D;
        return crow.distanceToSqr(Vec3.atCenterOf(source)) < stop * stop;
    }

    @Override
    public void start() {
        crow.getNavigation().stop();
        moveOn();
    }

    @Override
    public void stop() {
        source = null;
        target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (source != null && (target == null || crow.distanceToSqr(target) < 4.0D)) {
            Vec3 next = CrowLogic.fleeTarget(crow, source.getX() + 0.5D, source.getZ() + 0.5D);
            if (next != null) {
                target = next;
            }
        }
        moveOn();
    }

    private void moveOn() {
        if (target != null) {
            CrowLogic.fly(crow, target.x, target.y, target.z, SPEED);
        }
    }

    @Nullable
    private BlockPos findNearest() {
        int radius = CrowConfig.SCARE_RADIUS.get();
        int scan = (int) (radius * 1.3D);
        BlockPos center = crow.blockPosition();
        double best = Double.MAX_VALUE;
        BlockPos closest = null;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-scan, -scan, -scan), center.offset(scan, scan, scan))) {
            if (crow.level.getBlockState(pos).is(CrowTags.REPELS_CROWS)) {
                double dist = crow.distanceToSqr(Vec3.atCenterOf(pos));
                if (dist < best) {
                    best = dist;
                    closest = pos.immutable();
                }
            }
        }
        return closest != null && best <= (double) (radius * radius) ? closest : null;
    }
}
