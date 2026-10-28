package net.satisfy.vinery.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.entity.TraderMuleEntity;
import net.satisfy.vinery.core.entity.WanderingWinemakerEntity;
import net.satisfy.vinery.core.registry.EntityTypeRegistry;

import java.util.Optional;

/**
 * Port ({@code [trader] ownSpawner}): the Wandering Winemaker on its own timer, independent of the Wandering Trader.
 * 1.5.5 only swaps a share of Wandering Trader spawns ({@code WanderingTraderSpawnerMixin}, {@code spawnChance}); with
 * that at 0 this is the only source, and the two traders no longer share one roll.
 * <p>
 * A copy of vanilla's {@code WanderingTraderSpawner}: the same gates (overworld, doMobSpawning, doTraderSpawning), the
 * same 1200-tick step and 25/50/75% chance, 1 in 10, a spot within 48 blocks of a random player or a meeting point
 * near them, two leashed mules ({@code spawnWithMules}), {@code spawnDelay} ticks before it leaves. The wait and chance
 * live in the overworld's saved data ({@code data/vinery_winemaker_spawner.dat}) like vanilla's live in level.dat.
 * Moved from patcherMod (it was {@code trader.WinemakerSpawner}, feature_writeup/wandering_trader.md).
 */
public final class WinemakerSpawner {
    private static final String DATA_NAME = "vinery_winemaker_spawner";
    private static final int STEP = 1200;

    private static final RandomSource random = RandomSource.create();
    private static int tickDelay = STEP;

    private WinemakerSpawner() {
    }

    static void onServerStarted(ServerStartedEvent event) {
        tickDelay = STEP;
    }

    static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD || level.isDebug()) return;
        // vanilla only ticks custom spawners while doMobSpawning is on (ServerChunkCache.tickChunks)
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        if (!VineryConfig.TRADER_OWN_SPAWNER.get()) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) return;
        if (--tickDelay > 0) return;
        tickDelay = STEP;

        Data data = data(level);
        data.spawnDelay -= STEP;
        data.setDirty();
        if (data.spawnDelay > 0) return;
        data.spawnDelay = VineryConfig.TRADER_OWN_SPAWNER_INTERVAL.get();

        int chance = data.spawnChance;
        data.spawnChance = Mth.clamp(data.spawnChance + 25, 25, 75);
        if (random.nextInt(100) > chance) return;
        if (spawn(level)) data.spawnChance = 25;
    }

    private static boolean spawn(ServerLevel level) {
        ServerPlayer player = level.getRandomPlayer();
        if (player == null) return true;
        if (random.nextInt(10) != 0) return false;

        BlockPos playerPos = player.blockPosition();
        PoiManager poi = level.getPoiManager();
        Optional<BlockPos> meeting = poi.find(h -> h.is(PoiTypes.MEETING), p -> true, playerPos, 48, PoiManager.Occupancy.ANY);
        BlockPos center = meeting.orElse(playerPos);
        BlockPos pos = findSpawnPositionNear(level, center, 48);
        if (pos == null || !hasEnoughSpace(level, pos)) return false;
        if (level.getBiome(pos).is(Biomes.THE_VOID)) return false;

        WanderingWinemakerEntity trader = EntityTypeRegistry.WANDERING_WINEMAKER.get()
                .spawn(level, null, null, null, pos, MobSpawnType.EVENT, false, false);
        if (trader == null) return false;
        if (VineryConfig.TRADER_SPAWN_WITH_MULES.get()) {
            for (int n = 0; n < 2; n++) {
                BlockPos mulePos = findSpawnPositionNear(level, trader.blockPosition(), 4);
                if (mulePos == null) continue;
                TraderMuleEntity mule = EntityTypeRegistry.MULE.get().spawn(level, null, null, null, mulePos, MobSpawnType.EVENT, false, false);
                if (mule != null) mule.setLeashedTo(trader, true);
            }
        }
        trader.setDespawnDelay(VineryConfig.TRADER_SPAWN_DELAY.get());
        trader.setWanderTarget(center);
        trader.restrictTo(center, 16);
        Vinery.LOGGER.info("[trader] Wandering Winemaker spawned at {}", pos.toShortString());
        return true;
    }

    private static BlockPos findSpawnPositionNear(LevelReader level, BlockPos pos, int radius) {
        for (int n = 0; n < 10; n++) {
            int x = pos.getX() + random.nextInt(radius * 2) - radius;
            int z = pos.getZ() + random.nextInt(radius * 2) - radius;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos p = new BlockPos(x, y, z);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, level, p, EntityType.WANDERING_TRADER)) {
                return p;
            }
        }
        return null;
    }

    private static boolean hasEnoughSpace(BlockGetter level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos, pos.offset(1, 2, 1))) {
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) return false;
        }
        return true;
    }

    private static Data data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(Data::load, Data::new, DATA_NAME);
    }

    /** Wait and chance of the spawner, saved with the overworld. */
    public static final class Data extends SavedData {
        int spawnDelay;
        int spawnChance;

        Data() {
            spawnDelay = VineryConfig.TRADER_OWN_SPAWNER_INTERVAL.get();
            spawnChance = 25;
        }

        static Data load(CompoundTag tag) {
            Data d = new Data();
            d.spawnDelay = tag.getInt("SpawnDelay");
            d.spawnChance = tag.contains("SpawnChance") ? tag.getInt("SpawnChance") : 25;
            return d;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt("SpawnDelay", spawnDelay);
            tag.putInt("SpawnChance", spawnChance);
            return tag;
        }
    }
}
