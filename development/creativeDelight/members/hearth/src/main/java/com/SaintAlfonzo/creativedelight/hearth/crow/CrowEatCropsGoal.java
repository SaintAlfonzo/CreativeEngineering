package com.SaintAlfonzo.creativedelight.hearth.crow;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

/** A wild adult crow pecks a crop back to age 0 (C5). Needs mobGriefing; scarecrows, carved pumpkins and jack o'lanterns guard a 6-block radius. */
public class CrowEatCropsGoal extends Goal {
    private static final int SCAN_RADIUS = 8;
    private static final int PECK_AT = 20;
    private static final int PECK_END = 40;
    private static final double SPEED = 1.2D;
    private final CrowEntity crow;
    @Nullable
    private BlockPos crop;
    private int peckTime;

    public CrowEatCropsGoal(CrowEntity crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean allowed() {
        return CrowConfig.EAT_CROPS.get() && ForgeEventFactory.getMobGriefingEvent(crow.level, crow);
    }

    @Override
    public boolean canUse() {
        if (!CrowLogic.isWild(crow) || crow.isBaby() || !allowed() || crow.getRandom().nextInt(50) != 0) {
            return false;
        }
        crop = findCrop();
        return crop != null;
    }

    @Override
    public boolean canContinueToUse() {
        return crop != null && CrowLogic.isWild(crow) && allowed() && crow.level.getBlockState(crop).is(CrowTags.EDIBLE_CROPS)
                && crop.closerToCenterThan(crow.position(), 10.0D);
    }

    @Override
    public void start() {
        peckTime = 0;
        moveToCrop();
    }

    @Override
    public void stop() {
        crop = null;
        peckTime = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (crop == null) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(crop);
        if (crow.distanceToSqr(center) >= 2.0D) {
            moveToCrop();
            return;
        }
        crow.getNavigation().stop();
        crow.getLookControl().setLookAt(crop.getX() + 0.5D, crop.getY() + 0.5D, crop.getZ() + 0.5D);
        peckTime++;
        if (peckTime == PECK_AT && crow.level instanceof ServerLevel level) {
            peck(level);
        }
        if (peckTime > PECK_END) {
            crop = null;
        }
    }

    private void moveToCrop() {
        CrowLogic.fly(crow, crop.getX() + 0.5D, crop.getY() + 1.0D, crop.getZ() + 0.5D, SPEED);
    }

    private void peck(ServerLevel level) {
        BlockState state = level.getBlockState(crop);
        IntegerProperty age = ageOf(state);
        if (state.is(CrowTags.EDIBLE_CROPS) && age != null && state.getValue(age) > 0) {
            level.levelEvent(2001, crop, Block.getId(state));
            level.setBlock(crop, state.setValue(age, 0), Block.UPDATE_ALL);
        }
    }

    @Nullable
    private static IntegerProperty ageOf(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("age") && property instanceof IntegerProperty integer) {
                return integer;
            }
        }
        return null;
    }

    /** One random candidate, then the guard test on it alone: a guard scan per crop would add up over a whole field. */
    @Nullable
    private BlockPos findCrop() {
        BlockPos origin = crow.blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = crow.level.getBlockState(cursor);
                    if (state.is(CrowTags.EDIBLE_CROPS)) {
                        IntegerProperty age = ageOf(state);
                        if (age != null && state.getValue(age) > 0) {
                            candidates.add(cursor.immutable());
                        }
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        BlockPos pick = candidates.get(crow.getRandom().nextInt(candidates.size()));
        return CrowLogic.isGuarded(crow, pick) ? null : pick;
    }
}
