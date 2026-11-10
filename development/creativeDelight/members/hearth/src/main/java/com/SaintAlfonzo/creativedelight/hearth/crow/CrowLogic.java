package com.SaintAlfonzo.creativedelight.hearth.crow;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/** Shared movement, threat and taming rules of the crow goals (H&H 1.4.1 behaviour, re-based on AM's flight and navigation). */
public final class CrowLogic {
    public static final double ALARM_RANGE = 16.0D;
    private static final double SNATCHING_RADIUS = 2.5D;

    private CrowLogic() {
    }

    /** The goals act on wild, free crows only. */
    public static boolean isWild(TamableAnimal crow) {
        return !crow.isTame() && !crow.isOrderedToSit() && !CrowAccess.isSitting(crow) && !crow.isPassenger();
    }

    /** Far away or already airborne: fly, as AM's own item goal does. Otherwise walk. */
    public static void moveTo(TamableAnimal crow, double x, double y, double z, double speed) {
        if (CrowAccess.isFlying(crow) || crow.distanceToSqr(x, y, z) > 64.0D) {
            fly(crow, x, y, z, speed);
        } else {
            crow.getNavigation().moveTo(x, y, z, speed);
        }
    }

    public static void fly(TamableAnimal crow, double x, double y, double z, double speed) {
        CrowAccess.setFlying(crow, true);
        crow.getMoveControl().setWantedPosition(x, y, z, speed);
    }

    /** Settles a crow that has come down at its goal, the same check AM's idle goal makes. */
    public static void land(TamableAnimal crow) {
        if (CrowAccess.isFlying(crow) && crow.isOnGround()) {
            CrowAccess.setFlying(crow, false);
        }
    }

    /** A spot 8 blocks directly away from the threat and 3 to 5 up; AM's flight goes straight, so no ground search is needed. */
    @Nullable
    public static Vec3 fleeTarget(TamableAnimal crow, double fromX, double fromZ) {
        Vec3 away = new Vec3(crow.getX() - fromX, 0.0D, crow.getZ() - fromZ);
        if (away.lengthSqr() < 1.0E-8D) {
            return null;
        }
        away = away.normalize().scale(8.0D);
        return new Vec3(crow.getX() + away.x, crow.getY() + 3.0D + crow.getRandom().nextInt(3), crow.getZ() + away.z);
    }

    public static boolean isHoldingFood(LivingEntity entity) {
        return entity.getMainHandItem().is(CrowTags.CROW_FOOD) || entity.getOffhandItem().is(CrowTags.CROW_FOOD);
    }

    /** The radius at which {@code entity} scares this crow, or -1 if it does not. */
    public static double threatRadius(TamableAnimal crow, CrowWariness wariness, LivingEntity entity, double baseRadius) {
        if (entity == crow || !entity.isAlive() || wariness.isSnatchTarget(entity)) {
            return -1.0D;
        }
        if (crow.level instanceof ServerLevel level && entity == wariness.getAlarmSource(level)) {
            return ALARM_RANGE;
        }
        if (!entity.getType().is(CrowTags.SCARY_FOR_CROW)) {
            return -1.0D;
        }
        double radius = wariness.isSnatching() ? Math.min(baseRadius, SNATCHING_RADIUS) : baseRadius;
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return -1.0D;
            }
            if (CrowConfig.TEMPTING.get() && isHoldingFood(player)) {
                return Math.min(radius, wariness.waryDistance());
            }
        }
        return radius;
    }

    public static boolean isSpotThreatened(TamableAnimal crow, CrowWariness wariness, Vec3 pos) {
        double base = CrowConfig.SCARE_RADIUS.get();
        double scan = crow.level instanceof ServerLevel level && wariness.isAlarmed(level) ? Math.max(base, ALARM_RANGE) : base;
        return !crow.level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos, scan * 2.0D, scan * 2.0D, scan * 2.0D), entity -> {
            double radius = threatRadius(crow, wariness, entity, base);
            return radius > 0.0D && entity.distanceToSqr(pos) <= radius * radius;
        }).isEmpty();
    }

    /** Whether a repelling block (scarecrow, carved pumpkin, jack o'lantern) stands within the scare radius of a position. */
    public static boolean isGuarded(TamableAnimal crow, BlockPos center) {
        int radius = CrowConfig.SCARE_RADIUS.get();
        if (radius <= 0) {
            return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (crow.level.getBlockState(pos).is(CrowTags.REPELS_CROWS)) {
                return true;
            }
        }
        return false;
    }

    /** Tames on success, with the hearts; smoke otherwise. */
    public static boolean tryTame(TamableAnimal crow, Player player, int odds) {
        if (crow.getRandom().nextInt(odds) == 0 && !ForgeEventFactory.onAnimalTame(crow, player)) {
            crow.tame(player);
            CrowAccess.setFollowing(crow);
            crow.level.broadcastEntityEvent(crow, (byte) 7);
            return true;
        }
        crow.level.broadcastEntityEvent(crow, (byte) 6);
        return false;
    }

    public static void holdLoot(TamableAnimal crow, CrowWariness wariness, ItemStack stack) {
        crow.setItemSlot(EquipmentSlot.MAINHAND, stack);
        crow.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        wariness.startCarrying(CrowConfig.CARRY_TICKS.get());
    }

    /** Lets go of the loot as an item that never despawns and resists fire, lava and explosions (N24-8). */
    public static void dropLoot(TamableAnimal crow, CrowWariness wariness) {
        ItemStack stack = !wariness.getHeldBack().isEmpty() ? wariness.getHeldBack() : crow.getMainHandItem();
        spawnProtected(crow, stack);
        crow.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        wariness.stopCarrying();
    }

    public static void spawnProtected(TamableAnimal crow, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity item = new ItemEntity(crow.level, crow.getX(), crow.getY(), crow.getZ(), stack.copy());
        item.setUnlimitedLifetime();
        item.setInvulnerable(true);
        item.setDefaultPickUpDelay();
        crow.level.addFreshEntity(item);
    }
}
