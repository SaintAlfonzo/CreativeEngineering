package com.SaintAlfonzo.creativedelight.hearth.salt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

/**
 * Keeps a licker animal near a salt lick and sends it to lick (plan_hearth_crow_salt.md S4). The search is the vanilla POI
 * index, so a world with no salt costs nothing. There is no home area: the goal holds the animal only while a block exists.
 */
public class StayNearSaltGoal extends Goal {
    private static final int CHECK_INTERVAL = 20;
    private static final int RETRY_TICKS = 125;
    private static final int TRIP_TICKS = 300;
    private static final int PATH_CANDIDATES = 3;
    private static final double LICK_DISTANCE_SQR = 4.0D;

    private final PathfinderMob mob;
    @Nullable
    private BlockPos salt;
    private long nextCheck;
    private long nextSearch;
    private long nextLick = -1L;
    private boolean licking;
    private boolean licked;
    private int tripTicks;
    @Nullable
    private Path trip;

    public StayNearSaltGoal(PathfinderMob mob) {
        this.mob = mob;
        this.nextCheck = mob.getRandom().nextInt(CHECK_INTERVAL);
        this.nextSearch = mob.getRandom().nextInt(200);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean available() {
        if (!SaltConfig.ANIMAL_LICKING.get() || mob.isLeashed() || mob.isPassenger() || mob.isVehicle()) {
            return false;
        }
        if (mob instanceof TamableAnimal tamable && (tamable.isTame() || tamable.isOrderedToSit())) {
            return false;
        }
        return !(mob instanceof AbstractHorse horse && horse.isTamed());
    }

    private boolean saltStillThere() {
        return salt != null && SaltLick.isLickable(mob.level.getBlockState(salt).getBlock());
    }

    @Override
    public boolean canUse() {
        long now = mob.level.getGameTime();
        if (now < nextCheck || !(mob.level instanceof ServerLevel level)) {
            return false;
        }
        nextCheck = now + CHECK_INTERVAL;
        if (!available()) {
            return false;
        }
        if (nextLick < 0L) {
            int interval = SaltConfig.LICK_INTERVAL.get();
            nextLick = now + interval / 2 + mob.getRandom().nextInt(interval + 1);
        }
        if (!saltStillThere()) {
            salt = null;
            if (now < nextSearch) {
                return false;
            }
            nextSearch = now + SaltConfig.SEARCH_INTERVAL.get();
            salt = findSalt(level);
            if (salt == null) {
                return false;
            }
        }
        if (now >= nextLick) {
            trip = findTrip();
            if (trip == null) {
                nextLick = now + RETRY_TICKS;
                return false;
            }
            licking = true;
            return true;
        }
        licking = false;
        return beyondRadius();
    }

    private boolean beyondRadius() {
        int radius = SaltConfig.ANIMAL_RADIUS.get();
        return mob.blockPosition().distSqr(salt) > (double) (radius * radius);
    }

    @Override
    public boolean canContinueToUse() {
        if (salt == null || licked || !available() || !saltStillThere()) {
            return false;
        }
        if (licking) {
            return tripTicks < TRIP_TICKS && !mob.getNavigation().isDone() || closeEnough();
        }
        return beyondRadius() && !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        licked = false;
        tripTicks = 0;
        if (licking) {
            mob.getNavigation().moveTo(trip, 1.0D);
        } else {
            mob.getNavigation().moveTo(salt.getX() + 0.5D, salt.getY(), salt.getZ() + 0.5D, 1.0D);
        }
    }

    @Override
    public void tick() {
        if (!licking || licked) {
            return;
        }
        tripTicks++;
        if (closeEnough()) {
            mob.getLookControl().setLookAt(salt.getX() + 0.5D, salt.getY() + 0.5D, salt.getZ() + 0.5D);
            lick();
        }
    }

    private boolean closeEnough() {
        return mob.blockPosition().distSqr(salt) <= LICK_DISTANCE_SQR;
    }

    private void lick() {
        licked = true;
        BlockState state = mob.level.getBlockState(salt);
        mob.level.playSound(null, salt, HHSaltRegistry.LICK.get(), SoundSource.NEUTRAL, 0.8F, 0.6F + mob.getRandom().nextFloat() * 0.6F);
        if (mob.getRandom().nextDouble() < SaltConfig.ANIMAL_STAGE_CHANCE.get()) {
            SaltLick.wear(mob.level, salt, state);
        }
    }

    @Override
    public void stop() {
        long now = mob.level.getGameTime();
        if (licking) {
            if (licked) {
                int interval = SaltConfig.LICK_INTERVAL.get();
                nextLick = now + interval * 3 / 4 + mob.getRandom().nextInt(interval / 2 + 1);
            } else {
                nextLick = now + RETRY_TICKS;
            }
        }
        licking = false;
        licked = false;
        trip = null;
        mob.getNavigation().stop();
    }

    @Nullable
    private BlockPos findSalt(ServerLevel level) {
        Optional<BlockPos> found = level.getPoiManager().findClosest(type -> type.is(HHSaltRegistry.SALT_LICK_POI.getKey()), mob.blockPosition(),
                SaltConfig.ANIMAL_RADIUS.get() + 2, PoiManager.Occupancy.ANY);
        return found.map(BlockPos::immutable).orElse(null);
    }

    /** A complete path to a walkable spot beside the block, never into it; only the nearest few spots are path-checked. */
    @Nullable
    private Path findTrip() {
        Level level = mob.level;
        List<BlockPos> spots = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos spot = salt.offset(dx, dy, dz);
                    if ((dx != 0 || dz != 0) && walkable(level, spot)) {
                        spots.add(spot);
                    }
                }
            }
        }
        spots.sort(Comparator.comparingDouble(spot -> mob.distanceToSqr(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D)));
        for (int i = 0; i < Math.min(PATH_CANDIDATES, spots.size()); i++) {
            Path path = mob.getNavigation().createPath(spots.get(i), 0);
            if (path != null && path.canReach()) {
                return path;
            }
        }
        return null;
    }

    private static boolean walkable(Level level, BlockPos spot) {
        BlockPos below = spot.below();
        return level.getBlockState(spot).getCollisionShape(level, spot).isEmpty() && level.getBlockState(spot).getFluidState().isEmpty()
                && level.getBlockState(spot.above()).getCollisionShape(level, spot.above()).isEmpty()
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
