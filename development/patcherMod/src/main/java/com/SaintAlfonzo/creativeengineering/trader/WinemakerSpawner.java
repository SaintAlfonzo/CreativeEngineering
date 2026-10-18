package com.SaintAlfonzo.creativeengineering.trader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.WanderingTrader;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * Spawns Vinery's Wandering Winemaker on its own timer, independent of the Wandering Trader.
 * Vinery normally swaps a share of Wandering Trader spawns for a Winemaker (its
 * {@code WanderingTraderManagerMixin}, {@code wine_trader_chance}); with that set to 0 this is the
 * only source, and the two traders no longer share one roll.
 *
 * <p>A copy of vanilla's {@code WanderingTraderSpawner}: the same gates (overworld, doMobSpawning,
 * doTraderSpawning), the same 1200-tick step and 25/50/75% chance, 1 in 10, a spot within 48 blocks of
 * a random player or a meeting point near them, two leashed mules (Vinery's {@code vinery:mule}, as
 * its mixin does), 48000 ticks before it leaves. The wait and chance live in the overworld's saved
 * data ({@code data/creativeengineering_winemaker_spawner.dat}) like vanilla's live in level.dat.
 */
public final class WinemakerSpawner {

    private static final Logger LOG = LogUtils.getLogger();
    private static final ResourceLocation WINEMAKER = new ResourceLocation("vinery", "wandering_winemaker");
    private static final ResourceLocation MULE = new ResourceLocation("vinery", "mule");
    private static final String DATA_NAME = "creativeengineering_winemaker_spawner";
    private static final int STEP = 1200;

    private static final RandomSource random = RandomSource.create();
    private static int tickDelay = STEP;

    private WinemakerSpawner() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        tickDelay = STEP;
        warnIfVinerySwapsTraders();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD || level.isDebug()) return;
        // vanilla only ticks custom spawners while doMobSpawning is on (ServerChunkCache.tickChunks)
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        CEConfig.WanderingTrader cfg = CEConfig.WANDERING_TRADER;
        if (!cfg.enabled.get() || !cfg.winemakerEnabled.get()) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) return;
        if (--tickDelay > 0) return;
        tickDelay = STEP;

        EntityType<?> type = entityType(WINEMAKER);
        if (type == null) return;
        Data data = data(level);
        data.spawnDelay -= STEP;
        data.setDirty();
        if (data.spawnDelay > 0) return;
        data.spawnDelay = cfg.winemakerIntervalTicks.get();

        int chance = data.spawnChance;
        data.spawnChance = Mth.clamp(data.spawnChance + 25, 25, 75);
        if (random.nextInt(100) > chance) return;
        if (spawn(level, type)) data.spawnChance = 25;
    }

    private static boolean spawn(ServerLevel level, EntityType<?> type) {
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

        Entity entity = type.spawn(level, null, null, null, pos, MobSpawnType.EVENT, false, false);
        if (!(entity instanceof WanderingTrader trader)) {
            if (entity != null) entity.discard();
            return false;
        }
        EntityType<?> mule = entityType(MULE);
        for (int n = 0; n < 2 && mule != null; n++) {
            BlockPos mulePos = findSpawnPositionNear(level, trader.blockPosition(), 4);
            if (mulePos == null) continue;
            Entity m = mule.spawn(level, null, null, null, mulePos, MobSpawnType.EVENT, false, false);
            if (m instanceof Mob mob) mob.setLeashedTo(trader, true);
        }
        trader.setDespawnDelay(48000);
        trader.setWanderTarget(center);
        trader.restrictTo(center, 16);
        LOG.info("[trades] Wandering Winemaker spawned at {}", pos.toShortString());
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

    private static EntityType<?> entityType(ResourceLocation id) {
        // the entity registry is defaulted (pig), so check the key before reading it
        return ForgeRegistries.ENTITY_TYPES.containsKey(id) ? ForgeRegistries.ENTITY_TYPES.getValue(id) : null;
    }

    private static Data data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(Data::load, Data::new, DATA_NAME);
    }

    /** Logs a warning when Vinery would still turn Wandering Trader spawns into Winemakers too. */
    private static void warnIfVinerySwapsTraders() {
        CEConfig.WanderingTrader cfg = CEConfig.WANDERING_TRADER;
        if (!cfg.enabled.get() || !cfg.winemakerEnabled.get()) return;
        // the Vinery backport (vinery-common.toml [trader] spawnChance), else 1.3.10 (config/vinery/config.json5)
        Path toml = FMLPaths.CONFIGDIR.get().resolve("vinery-common.toml");
        try {
            if (Files.exists(toml)) {
                Matcher t = Pattern.compile("(?s)\\[trader\\].*?spawnChance\\s*=\\s*([0-9.]+)").matcher(Files.readString(toml));
                if (t.find() && Double.parseDouble(t.group(1)) > 0) {
                    LOG.warn("[trades] {} has [trader] spawnChance {}: Vinery still swaps that share of Wandering Trader"
                            + " spawns for Winemakers on top of the separate Winemaker spawner. Set it to 0.", toml, t.group(1));
                }
                return;
            }
        } catch (Exception e) {
            LOG.debug("[trades] could not read {}: {}", toml, e.toString());
        }
        Path file = FMLPaths.CONFIGDIR.get().resolve("vinery/config.json5");
        try {
            if (!Files.exists(file)) return;
            Matcher m = Pattern.compile("\"wine_trader_chance\"\\s*:\\s*(\\d+)").matcher(Files.readString(file));
            if (m.find() && Integer.parseInt(m.group(1)) > 0) {
                LOG.warn("[trades] {} has wine_trader_chance {}: Vinery still swaps that share of Wandering Trader"
                        + " spawns for Winemakers on top of the separate Winemaker spawner. Set it to 0.", file, m.group(1));
            }
        } catch (Exception e) {
            LOG.debug("[trades] could not read {}: {}", file, e.toString());
        }
    }

    /** Wait and chance of the Winemaker spawner, saved with the overworld. */
    public static final class Data extends SavedData {
        int spawnDelay;
        int spawnChance;

        Data() {
            spawnDelay = CEConfig.WANDERING_TRADER.winemakerIntervalTicks.get();
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
