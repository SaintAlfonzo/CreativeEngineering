package com.SaintAlfonzo.creativeengineering.seasons;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.ServerConfig;
import sereneseasons.season.SeasonHooks;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Season types on top of Serene Seasons: arid, frozen, dry, tropical and temperate biomes by tag, and
 * rivers/beaches (adaptive) that take on the land around them.
 *
 * THE PROBLEM:
 *   Serene Seasons knows two kinds of biome. "Tropical" is its config list plus every biome warmer than 0.8,
 *   so deserts and badlands get the wet season, and its Mid Wet rains even where the biome declares no
 *   precipitation. Everything else is temperate and cools by up to 0.8 in winter, so a river (0.5, also
 *   Terralith's warm_river in the hot band) snows next to jungles and deserts that never do.
 *
 * THE FIX:
 *   Biome tags {@code creativeengineering:seasons/arid|frozen|dry|tropical|temperate|adaptive} (filled from
 *   kubejs/generation_scripts/worldgen/season_types.json) pick the type; untagged biomes keep Serene Seasons'
 *   rule. A tagged biome always has seasons, even when Serene Seasons' blacklist names it (oceans). Dry and
 *   tropical both run the wet/dry cycle: dry (savannas) rains in Mid Wet only, tropical (jungles) in every
 *   season but Mid Dry. Temperate takes the seasonal cooling even above 0.8 (temperate_highlands).
 *   The mixins that apply it:
 *   - {@code SeasonBiomeConfigMixin}: usesTropicalSeasons and enablesSeasonalEffects answer from the tags.
 *   - {@code SeasonHooksTypesMixin}: rivers/beaches resolved to their land in every temperature and
 *     gameplay-rain read; arid never rains; dry rains in Mid Wet only; arid/frozen temperature never shifts
 *     with the season, frozen is held at 0.1 or colder; temperate always takes the seasonal offset; oceans
 *     only freeze over when frozen-tagged (the rest keep vanilla ice, i.e. none).
 *   - {@code SeasonFertilityMixin}: arid grows summer crops all year, frozen winter crops only.
 *   - client: {@code SeasonHooksClientMixin} + {@code SeasonRainColumnMixin} (what the renderer draws per
 *     column), {@code SeasonColorUtilMixin} (no seasonal tint for arid/frozen), {@code SeasonHud*Mixin}
 *     (HUD shows Arid / Frozen and the day of the year), {@code PrettyRainSeasonMixin} (Pretty Rain's
 *     particles and sounds, which replace the vanilla renderer).
 *
 * RESOLVING AN ADAPTIVE COLUMN:
 *   Rings of 8 directions at 8, 16, 24, 32, 48, 64, ... blocks (up to CEConfig seasons.adaptiveRadius). At each
 *   point the biome at the surface (MOTION_BLOCKING heightmap) counts if it is not adaptive, not ocean and has
 *   seasonal effects (not blacklisted: caves, oceans). The first ring with any such biome decides by
 *   majority, ties to the first found. Points in unloaded chunks are skipped (never loads or generates a
 *   chunk); a result with skipped points is used but not cached. Cache per level and 4x4 column, cleared on
 *   tag reload. Only for real levels in Serene Seasons' whitelisted dimensions: worldgen (WorldGenRegion)
 *   keeps the vanilla rules, as Serene Seasons does.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID)
public final class SeasonTypes {

    public enum Type { TEMPERATE, TROPICAL, DRY, ARID, FROZEN }

    public static final TagKey<Biome> ARID = tag("arid");
    public static final TagKey<Biome> FROZEN = tag("frozen");
    public static final TagKey<Biome> DRY = tag("dry");
    public static final TagKey<Biome> TROPICAL = tag("tropical");
    public static final TagKey<Biome> TEMPERATE = tag("temperate");
    public static final TagKey<Biome> ADAPTIVE = tag("adaptive");

    /** Frozen biomes never read warmer than this (snow below 0.15). */
    public static final float FROZEN_MAX_TEMPERATURE = 0.1F;

    private static final int[] RING_STEPS = {8, 16, 24, 32, 48, 64, 96, 128, 192, 256};
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
    private static final int CACHE_SIZE = 32768;
    private static final Map<Level, Map<Long, Holder<Biome>>> CACHES = new WeakHashMap<>();

    private SeasonTypes() {}

    private static TagKey<Biome> tag(String type) {
        return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(CreativeEngineering.MODID, "seasons/" + type));
    }

    public static boolean enabled() {
        return CEConfig.SEASONS.enabled.get();
    }

    /** The pack's type for this biome, or null when no seasons tag lists it (Serene Seasons' own rule applies). */
    @Nullable
    public static Type tagged(Holder<Biome> biome) {
        if (!enabled()) return null;
        if (biome.is(ARID)) return Type.ARID;
        if (biome.is(FROZEN)) return Type.FROZEN;
        if (biome.is(DRY)) return Type.DRY;
        if (biome.is(TROPICAL)) return Type.TROPICAL;
        if (biome.is(TEMPERATE)) return Type.TEMPERATE;
        return null;
    }

    /** True when any seasons tag lists the biome, adaptive included: it gets seasons even if blacklisted. */
    public static boolean listed(Holder<Biome> biome) {
        return enabled() && (tagged(biome) != null || biome.is(ADAPTIVE));
    }

    /**
     * Oceans have seasonal weather but only frozen-tagged ones get seasonal ice: an ocean that is not frozen keeps
     * vanilla's freezing (by its base temperature, so a 0.5 ocean never freezes), however cold its season reads.
     */
    public static boolean vanillaIce(Holder<Biome> biome) {
        return enabled() && biome.is(BiomeTags.IS_OCEAN) && tagged(biome) != Type.FROZEN;
    }

    /** Dry or tropical: Serene Seasons' wet/dry cycle (temperature fixed, tropical tint, crops, HUD). */
    public static boolean wetDryCycle(Type type) {
        return type == Type.TROPICAL || type == Type.DRY;
    }

    /** Arid or frozen: no seasonal cycle at all (temperature, tint, HUD). */
    public static boolean yearRound(Holder<Biome> biome) {
        Type t = tagged(biome);
        return t == Type.ARID || t == Type.FROZEN;
    }

    /**
     * The arid/frozen type that applies at {@code pos}, after resolving rivers and beaches, or null when it is
     * an ordinary seasonal biome, outside Serene Seasons' dimensions, or blacklisted. Used by the HUD.
     */
    @Nullable
    public static Type yearRoundTypeAt(Level level, BlockPos pos) {
        if (!enabled() || !ServerConfig.isDimensionWhitelisted(level.dimension())) return null;
        Holder<Biome> biome = resolve(level, level.getBiome(pos), pos);
        if (!BiomeConfig.enablesSeasonalEffects(biome)) return null;
        Type t = tagged(biome);
        return t == Type.ARID || t == Type.FROZEN ? t : null;
    }

    /** Days in a Serene Seasons year (12 sub-seasons). */
    public static int yearDays(Level level) {
        ISeasonState state = SeasonHelper.getSeasonState(level);
        return Math.max(1, state.getCycleDuration() / Math.max(1, state.getDayDuration()));
    }

    /** Day of the Serene Seasons year, 1-based, counting up from Early Spring. */
    public static int dayOfYear(Level level) {
        return SeasonHelper.getSeasonState(level).getDay() % yearDays(level) + 1;
    }

    // ── adaptive biomes ─────────────────────────────────────────────────

    /**
     * For a river or beach column: the land biome it should behave like. Every other biome (and anything
     * outside a real, whitelisted level) comes back unchanged.
     */
    public static Holder<Biome> resolve(LevelReader reader, Holder<Biome> biome, BlockPos pos) {
        if (!(reader instanceof Level level) || !enabled() || !biome.is(ADAPTIVE)) return biome;
        if (!ServerConfig.isDimensionWhitelisted(level.dimension())) return biome;

        int cx = pos.getX() >> 2, cz = pos.getZ() >> 2;
        long key = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
        Map<Long, Holder<Biome>> cache;
        synchronized (CACHES) {
            cache = CACHES.computeIfAbsent(level, l -> new LinkedHashMap<>(256, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Holder<Biome>> eldest) {
                    return size() > CACHE_SIZE;
                }
            });
        }
        synchronized (cache) {
            Holder<Biome> hit = cache.get(key);
            if (hit != null) return hit;
        }

        int x = (cx << 2) + 2, z = (cz << 2) + 2;
        int radius = CEConfig.SEASONS.adaptiveRadius.get();
        boolean complete = true;
        Holder<Biome> found = null;
        for (int step : RING_STEPS) {
            if (step > radius) break;
            Map<Holder<Biome>, Integer> votes = new LinkedHashMap<>();
            for (int[] d : DIRECTIONS) {
                int dist = d[0] != 0 && d[1] != 0 ? Math.round(step * 0.7071F) : step;
                int sx = x + d[0] * dist, sz = z + d[1] * dist;
                if (!level.hasChunk(sx >> 4, sz >> 4)) {
                    complete = false;
                    continue;
                }
                int sy = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sx, sz);
                Holder<Biome> h = level.getNoiseBiome(sx >> 2, sy >> 2, sz >> 2);
                if (h.is(ADAPTIVE) || h.is(BiomeTags.IS_OCEAN) || !BiomeConfig.enablesSeasonalEffects(h)) continue;
                votes.merge(h, 1, Integer::sum);
            }
            if (!votes.isEmpty()) {
                int best = 0;
                for (Map.Entry<Holder<Biome>, Integer> e : votes.entrySet()) {
                    if (e.getValue() > best) {
                        best = e.getValue();
                        found = e.getKey();
                    }
                }
                break;
            }
        }

        Holder<Biome> result = found != null ? found : biome;
        if (complete) {
            synchronized (cache) {
                cache.put(key, result);
            }
        }
        return result;
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        synchronized (CACHES) {
            CACHES.clear();
        }
    }

    /**
     * What falls in this column right now, as the vanilla renderer with Serene Seasons would draw it: river or
     * beach resolved to its land, the season's rain rule (arid none, dry Mid Wet only, tropical not in Mid Dry)
     * for rain/none biomes, then rain or snow by the seasonal temperature. For weather mods that replace the
     * renderer (PrettyRainSeasonMixin).
     */
    public static Biome.Precipitation precipitationAt(Level level, Holder<Biome> biome, BlockPos pos) {
        Holder<Biome> land = resolve(level, biome, pos);
        Biome.Precipitation precipitation = land.value().getPrecipitation();
        if (ServerConfig.isDimensionWhitelisted(level.dimension()) && BiomeConfig.enablesSeasonalEffects(land)
                && precipitation != Biome.Precipitation.SNOW) {
            precipitation = SeasonHooks.shouldRainInBiomeInSeason(level, land) ? Biome.Precipitation.RAIN : Biome.Precipitation.NONE;
        }
        if (precipitation == Biome.Precipitation.NONE) return precipitation;
        return SeasonHooks.warmEnoughToRainHook(land, pos, level) ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW;
    }

    // ── client renderer column ──────────────────────────────────────────
    // LevelRenderer.renderSnowAndRain / tickRain ask Serene Seasons for a column's precipitation with only the
    // biome in hand; SeasonRainColumnMixin records the column just before, on the render thread.

    private static final BlockPos.MutableBlockPos RENDER_COLUMN = new BlockPos.MutableBlockPos();
    private static boolean renderColumnSet;

    public static void setRenderColumn(int x, int z) {
        RENDER_COLUMN.set(x, 64, z);
        renderColumnSet = true;
    }

    @Nullable
    public static BlockPos renderColumn() {
        return renderColumnSet ? RENDER_COLUMN : null;
    }
}
