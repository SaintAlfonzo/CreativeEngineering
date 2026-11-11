package com.SaintAlfonzo.creativedelight.hearth.nest;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.crow.CrowConfig;
import com.SaintAlfonzo.creativedelight.hearth.crow.CrowEntity;
import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Crows come from nests (plan_hearth_crow_swap.md 2.2): a nest generates with a small flock, and a generated nest near a player
 * refills it on a random tick, one crow per cooldown, up to a cap. Crows have no biome spawn entries, so no spawn check scans
 * the area around a spawn any more (the 1.2.02 chunk generation crash).
 */
public final class NestCrows {
    public static final ResourceLocation TREE_LOOT = new ResourceLocation(HearthAndHarvestMod.MODID, "chests/crow_nest");
    public static final ResourceLocation VILLAGE_LOOT = new ResourceLocation(HearthAndHarvestMod.MODID, "chests/crow_nest_village");
    private static final int SPOT_RADIUS = 6;
    private static final int SPOT_TRIES = 8;

    private NestCrows() {
    }

    /**
     * Places a generated nest with its stolen items and flock. Worldgen only: every read and write stays at {@code pos}, which
     * the caller keeps inside the chunk being decorated.
     */
    public static void placeGenerated(WorldGenLevel level, BlockPos pos, RandomSource random, ResourceLocation lootTable) {
        BlockState nest = HHNestRegistry.NEST.get().defaultBlockState().setValue(NestBlock.GENERATED, true);
        level.setBlock(pos, nest, 2);
        if (level.getBlockEntity(pos) instanceof NestBlockEntity entity) {
            entity.setLootTable(lootTable, random.nextLong());
        }
        int min = CrowConfig.NEST_FLOCK_MIN.get();
        int max = Math.max(min, CrowConfig.NEST_FLOCK_MAX.get());
        int count = Mth.randomBetweenInclusive(random, min, max);
        for (int i = 0; i < count; i++) {
            spawnCrow(level, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, random, MobSpawnType.CHUNK_GENERATION);
        }
    }

    /** The random tick of a generated nest (or any nest with {@code playerNestsSpawnCrows}). */
    public static void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(NestBlock.GENERATED) && !CrowConfig.PLAYER_NESTS_SPAWN_CROWS.get()) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof NestBlockEntity nest)) {
            return;
        }
        long now = level.getGameTime();
        if (now - nest.getLastCrow() < CrowConfig.NEST_RESPAWN_MINUTES.get() * 1200L) {
            return;
        }
        if (!level.hasNearbyAlivePlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, CrowConfig.NEST_PLAYER_RANGE.get())) {
            return;
        }
        int range = CrowConfig.NEST_CROW_RANGE.get();
        if (level.getEntitiesOfClass(CrowEntity.class, new AABB(pos).inflate(range)).size() >= CrowConfig.NEST_CROW_CAP.get()) {
            return;
        }
        for (int i = 0; i < SPOT_TRIES; i++) {
            BlockPos spot = pos.offset(random.nextInt(SPOT_RADIUS * 2 + 1) - SPOT_RADIUS, random.nextInt(6) - 2, random.nextInt(SPOT_RADIUS * 2 + 1) - SPOT_RADIUS);
            if (!level.isLoaded(spot) || level.getMaxLocalRawBrightness(spot) <= 8) {
                continue;
            }
            double x = spot.getX() + 0.5D;
            double z = spot.getZ() + 0.5D;
            if (!level.noCollision(HHEntities.CROW.get().getAABB(x, spot.getY(), z)) || !level.getFluidState(spot).isEmpty()) {
                continue;
            }
            if (spawnCrow(level, x, spot.getY(), z, random, MobSpawnType.NATURAL)) {
                nest.setLastCrow(now);
            }
            return;
        }
    }

    private static boolean spawnCrow(ServerLevelAccessor level, double x, double y, double z, RandomSource random, MobSpawnType reason) {
        CrowEntity crow = HHEntities.CROW.get().create(level.getLevel());
        if (crow == null) {
            return false;
        }
        crow.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
        crow.finalizeSpawn(level, level.getCurrentDifficultyAt(crow.blockPosition()), reason, null, null);
        return level.addFreshEntity(crow);
    }
}
