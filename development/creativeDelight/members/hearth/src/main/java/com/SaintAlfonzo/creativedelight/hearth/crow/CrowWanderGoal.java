package com.SaintAlfonzo.creativedelight.hearth.crow;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

/** H&H 1.4.1: wander flights that prefer perches (leaves, logs, fences, walls) and avoid repelling blocks. */
public class CrowWanderGoal extends WaterAvoidingRandomFlyingGoal {
    private static final int PERCH_SAMPLES = 16;
    private static final int PERCH_HORIZONTAL_RANGE = 12;
    private static final int PERCH_VERTICAL_RANGE = 8;
    private static final int PERCHED_INTERVAL = 400;
    private static final int GROUND_INTERVAL = 120;

    public CrowWanderGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);
    }

    public boolean canUse() {
        if (this.mob instanceof TamableAnimal tamable && tamable.isOrderedToSit()) {
            return false;
        }

        this.setInterval(this.isPerched() ? 400 : 120);
        return super.canUse();
    }

    private boolean isPerched() {
        return !this.mob.isOnGround() ? false : isPerchBlock(this.mob.level.getBlockState(this.mob.blockPosition().below()));
    }

    private static boolean isPerchBlock(BlockState state) {
        return state.getBlock() instanceof LeavesBlock || state.is(BlockTags.LOGS) || state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS);
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInWater()) {
            Vec3 land = LandRandomPos.getPos(this.mob, 15, 15);
            if (land != null) {
                return land;
            }
        }

        if (this.mob.isOnGround() && !this.isPerched() && this.mob.getRandom().nextFloat() < 0.4F) {
            Vec3 stroll = LandRandomPos.getPos(this.mob, 4, 2);
            if (stroll != null) {
                return stroll;
            }
        }

        Vec3 perch = this.findPerch();
        return perch != null ? perch : super.getPosition();
    }

    @Nullable
    private Vec3 findPerch() {
        Level level = this.mob.level;
        RandomSource random = this.mob.getRandom();
        BlockPos origin = this.mob.blockPosition();
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 16; i++) {
            BlockPos column = origin.offset(random.nextInt(25) - 12, 0, random.nextInt(25) - 12);
            if (level.hasChunkAt(column)) {
                BlockPos top = level.getHeightmapPos(Types.MOTION_BLOCKING, column);
                int rise = top.getY() - origin.getY();
                if (Math.abs(rise) <= 8 && !top.closerThan(origin, 3.0) && level.getFluidState(top.below()).isEmpty()) {
                    BlockState below = level.getBlockState(top.below());
                    if (!below.is(CrowTags.REPELS_CROWS)) {
                        double score = (double)rise + (isPerchBlock(below) ? 6.0 : 0.0) + random.nextDouble() * 4.0;
                        if (score > bestScore) {
                            bestScore = score;
                            best = top;
                        }
                    }
                }
            }
        }

        return best != null ? Vec3.atBottomCenterOf(best) : null;
    }
}
