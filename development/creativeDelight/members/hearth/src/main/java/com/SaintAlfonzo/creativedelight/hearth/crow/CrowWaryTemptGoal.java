package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** A player holding crow food draws a wild crow, which comes closer as trust fills (C3, plan_hearth_crow_salt.md 8.5). */
public class CrowWaryTemptGoal extends Goal {
    private static final double TEMPT_RANGE = 10.0D;
    private static final double GIVE_UP_RANGE_SQR = 196.0D;
    private static final double SPEED = 1.0D;
    private final TamableAnimal crow;
    private final TargetingConditions conditions = TargetingConditions.forNonCombat().range(TEMPT_RANGE).ignoreLineOfSight()
            .selector(entity -> entity instanceof Player player && !player.isSpectator() && CrowLogic.isHoldingFood(player));
    @Nullable
    private Player player;
    private int repath;
    private int cooldown;

    public CrowWaryTemptGoal(TamableAnimal crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean unavailable(CrowWariness wariness) {
        return !CrowLogic.isWild(crow) || crow.level instanceof ServerLevel level && wariness.isAlarmed(level) || !crow.getMainHandItem().isEmpty();
    }

    @Override
    public boolean canUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || !CrowConfig.TEMPTING.get()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (unavailable(wariness)) {
            return false;
        }
        player = crow.level.getNearestPlayer(conditions, crow);
        return player != null;
    }

    @Override
    public boolean canContinueToUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        return wariness != null && !unavailable(wariness) && player != null && player.isAlive() && !player.isSpectator()
                && CrowLogic.isHoldingFood(player) && crow.distanceToSqr(player) < GIVE_UP_RANGE_SQR;
    }

    @Override
    public void start() {
        repath = 0;
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness != null) {
            wariness.setBeingTempted(true);
        }
    }

    @Override
    public void stop() {
        player = null;
        cooldown = 40;
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness != null) {
            wariness.setBeingTempted(false);
        }
        crow.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || player == null) {
            return;
        }
        crow.getLookControl().setLookAt(player, crow.getMaxHeadYRot() + 20.0F, crow.getMaxHeadXRot());
        wariness.addTrust(player.isSprinting() ? -5 : player.isShiftKeyDown() ? 2 : 1);

        double comfort = wariness.comfortDistance();
        double distance = crow.distanceTo(player);
        if (distance > comfort - 0.75D && distance < comfort + 1.0D) {
            crow.getNavigation().stop();
            CrowLogic.land(crow);
        } else if (--repath <= 0 || crow.getNavigation().isDone()) {
            repath = 10;
            Vec3 away = new Vec3(crow.getX() - player.getX(), 0.0D, crow.getZ() - player.getZ());
            if (away.lengthSqr() < 1.0E-4D) {
                away = new Vec3(crow.getRandom().nextDouble() - 0.5D, 0.0D, crow.getRandom().nextDouble() - 0.5D);
            }
            double standOff = distance > comfort ? comfort : comfort + 1.0D;
            Vec3 spot = player.position().add(away.normalize().scale(standOff));
            CrowLogic.moveTo(crow, spot.x, player.getY(), spot.z, SPEED);
        }
    }
}
