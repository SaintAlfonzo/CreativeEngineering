package com.SaintAlfonzo.creativecalendar.seasons;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;
import com.SaintAlfonzo.creativecalendar.CreativeCalendar;
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
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.ServerConfig;
import sereneseasons.season.SeasonHooks;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Season types on top of Serene Seasons: arid, frozen, dry, tropical, temperate and cold biomes by tag,
 * permanent seasons (one season all year: the Terralith skylands), and rivers/beaches (adaptive) that take on
 * the land around them.
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
 *   season but Mid Dry. Temperate takes the seasonal cooling even above 0.8 (temperate_highlands). Cold is
 *   temperate that snows exactly Late Autumn to Early Spring and rains the rest of the year.
 *   Permanent seasons (see PERMANENT SEASONS below) keep one season all year.
 *   The mixins that apply it:
 *   - {@code SeasonBiomeConfigMixin}: usesTropicalSeasons and enablesSeasonalEffects answer from the tags.
 *   - {@code SeasonHooksTypesMixin}: rivers/beaches resolved to their land in every temperature and
 *     gameplay-rain read; arid never rains; dry rains in Mid Wet only; arid/frozen temperature never shifts
 *     with the season, frozen is held at 0.1 or colder; temperate always takes the seasonal offset; oceans
 *     only freeze over when frozen-tagged (the rest keep vanilla ice, i.e. none).
 *   - {@code SeasonFertilityMixin}: arid grows summer crops all year, frozen winter crops only, a permanent
 *     season grows that season's crops.
 *   - client: {@code SeasonHooksClientMixin} + {@code SeasonRainColumnMixin} (what the renderer draws per
 *     column), {@code SeasonColorUtilMixin} (no seasonal tint for arid/frozen, a permanent season's own tint),
 *     {@code SeasonHud*Mixin} (HUD shows Arid / Frozen / a permanent season and its day),
 *     {@code PrettyRainSeasonMixin} (Pretty Rain's particles and sounds, which replace the vanilla renderer).
 *   - {@link #seasonTemperature} (getBiomeTemperatureInSeason): what each type's season does to temperature.
 *   - {@link #applySnowRules} (every Level temperature read): the snow line at CalendarConfig seasons.snowLineY
 *     (build height): below it nothing snows from height alone; above it temperate and cold biomes always
 *     snow (a height gauge for flyers). Plus per-biome snow seasons ({@code creativeengineering:snow/no_<sub_season>}
 *     tags, season_types.json snow_seasons).
 *
 * PERMANENT SEASONS (permanent_spring|summer|autumn|winter):
 *   No seasonal temperature offset; HUD, grass tint and crops show the biome's own season. Winter behaves
 *   exactly like frozen (snow all year, nothing melts, no tint, winter crops) but the HUD says Winter. Summer
 *   never snows. Spring and autumn split the year into Early (1/4), Mid (1/2) and Late (1/4), shown on the HUD
 *   with their own day count; the snowy quarter (Early Spring, Late Autumn) falls in the world's winter, so it
 *   lines up with Serene Seasons' winter sub-seasons, and they rain the rest of the year.
 *
 * RESOLVING AN ADAPTIVE COLUMN:
 *   Rings of 8 directions at 8, 16, 24, 32, 48, 64, ... blocks (up to CalendarConfig seasons.adaptiveRadius). At each
 *   point the biome at the surface (MOTION_BLOCKING heightmap) counts if it is not adaptive, not ocean and has
 *   seasonal effects (not blacklisted: caves, oceans). The first ring with any such biome decides by
 *   majority, ties to the first found. Points in unloaded chunks are skipped (never loads or generates a
 *   chunk); a result with skipped points is used but not cached. Cache per level and 4x4 column, cleared on
 *   tag reload. Only for real levels in Serene Seasons' whitelisted dimensions: worldgen (WorldGenRegion)
 *   keeps the vanilla rules, as Serene Seasons does.
 */
@Mod.EventBusSubscriber(modid = CreativeCalendar.MOD_ID)
public final class SeasonTypes {

    public enum Type {
        TEMPERATE, COLD, TROPICAL, DRY, ARID, FROZEN,
        PERMANENT_SPRING, PERMANENT_SUMMER, PERMANENT_AUTUMN, PERMANENT_WINTER
    }

    /** Where a permanent spring or autumn stands in its year: Early 1/4, Mid 1/2, Late 1/4. */
    public enum Part { EARLY, MID, LATE }

    public static final TagKey<Biome> ARID = tag("arid");
    public static final TagKey<Biome> FROZEN = tag("frozen");
    public static final TagKey<Biome> DRY = tag("dry");
    public static final TagKey<Biome> TROPICAL = tag("tropical");
    public static final TagKey<Biome> TEMPERATE = tag("temperate");
    public static final TagKey<Biome> COLD = tag("cold");
    public static final TagKey<Biome> PERMANENT_SPRING = tag("permanent_spring");
    public static final TagKey<Biome> PERMANENT_SUMMER = tag("permanent_summer");
    public static final TagKey<Biome> PERMANENT_AUTUMN = tag("permanent_autumn");
    public static final TagKey<Biome> PERMANENT_WINTER = tag("permanent_winter");
    public static final TagKey<Biome> ADAPTIVE = tag("adaptive");

    /** Sub-seasons a cold biome snows in: Late Autumn to Early Spring. */
    private static final EnumSet<Season.SubSeason> COLD_SNOW = EnumSet.of(Season.SubSeason.LATE_AUTUMN,
            Season.SubSeason.EARLY_WINTER, Season.SubSeason.MID_WINTER, Season.SubSeason.LATE_WINTER,
            Season.SubSeason.EARLY_SPRING);

    /** Frozen biomes never read warmer than this (snow below 0.15). */
    public static final float FROZEN_MAX_TEMPERATURE = 0.1F;

    private static final int[] RING_STEPS = {8, 16, 24, 32, 48, 64, 96, 128, 192, 256};
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
    private static final int CACHE_SIZE = 32768;
    private static final Map<Level, Map<Long, Holder<Biome>>> CACHES = new WeakHashMap<>();
    /**
     * tagged() per biome holder (registry references only; holders compare by identity). Chunk meshing reads it
     * for every tinted block on several threads, ten tag lookups each. Replaced, not cleared, on tag reload so a
     * lookup racing the reload lands in the old map.
     */
    private static volatile Map<Holder<Biome>, Object> TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Object UNTAGGED = new Object();

    private SeasonTypes() {}

    private static TagKey<Biome> tag(String type) {
        return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(CreativeCalendar.NAMESPACE, "seasons/" + type));
    }

    public static boolean enabled() {
        return CalendarConfig.SEASONS.enabled.get();
    }

    /** The pack's type for this biome, or null when no seasons tag lists it (Serene Seasons' own rule applies). */
    @Nullable
    public static Type tagged(Holder<Biome> biome) {
        if (!enabled()) return null;
        if (biome.kind() != Holder.Kind.REFERENCE) return lookupType(biome);
        Map<Holder<Biome>, Object> cache = TYPE_CACHE;
        Object hit = cache.get(biome);
        if (hit == null) {
            Type type = lookupType(biome);
            hit = type != null ? type : UNTAGGED;
            cache.put(biome, hit);
        }
        return hit == UNTAGGED ? null : (Type) hit;
    }

    @Nullable
    private static Type lookupType(Holder<Biome> biome) {
        if (biome.is(ARID)) return Type.ARID;
        if (biome.is(FROZEN)) return Type.FROZEN;
        if (biome.is(PERMANENT_WINTER)) return Type.PERMANENT_WINTER;
        if (biome.is(PERMANENT_SPRING)) return Type.PERMANENT_SPRING;
        if (biome.is(PERMANENT_SUMMER)) return Type.PERMANENT_SUMMER;
        if (biome.is(PERMANENT_AUTUMN)) return Type.PERMANENT_AUTUMN;
        if (biome.is(DRY)) return Type.DRY;
        if (biome.is(TROPICAL)) return Type.TROPICAL;
        if (biome.is(COLD)) return Type.COLD;
        if (biome.is(TEMPERATE)) return Type.TEMPERATE;
        return null;
    }

    /** Frozen, or permanent winter (frozen that the HUD calls Winter). */
    public static boolean frozenLike(@Nullable Type type) {
        return type == Type.FROZEN || type == Type.PERMANENT_WINTER;
    }

    public static boolean permanent(@Nullable Type type) {
        return type == Type.PERMANENT_SPRING || type == Type.PERMANENT_SUMMER
                || type == Type.PERMANENT_AUTUMN || type == Type.PERMANENT_WINTER;
    }

    /** Temperate and cold: Serene Seasons' temperate offset, also above 0.8. */
    public static boolean temperateLike(@Nullable Type type) {
        return type == Type.TEMPERATE || type == Type.COLD;
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
        return enabled() && biome.is(BiomeTags.IS_OCEAN) && !frozenLike(tagged(biome));
    }

    /** Dry or tropical: Serene Seasons' wet/dry cycle (temperature fixed, tropical tint, crops, HUD). */
    public static boolean wetDryCycle(Type type) {
        return type == Type.TROPICAL || type == Type.DRY;
    }

    /** Arid, frozen or permanent winter: no seasonal tint. */
    public static boolean yearRound(Holder<Biome> biome) {
        Type t = tagged(biome);
        return t == Type.ARID || frozenLike(t);
    }

    /** Arid, frozen and the permanent seasons: Serene Seasons' seasonal temperature offset is skipped. */
    public static boolean noSeasonalOffset(Holder<Biome> biome) {
        Type t = tagged(biome);
        return t == Type.ARID || t == Type.FROZEN || permanent(t);
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

    /** Like {@link #yearRoundTypeAt}, also returning the permanent seasons: the HUD shows these its own way. */
    @Nullable
    public static Type hudTypeAt(Level level, BlockPos pos) {
        if (!enabled() || !ServerConfig.isDimensionWhitelisted(level.dimension())) return null;
        Holder<Biome> biome = resolve(level, level.getBiome(pos), pos);
        if (!BiomeConfig.enablesSeasonalEffects(biome)) return null;
        Type t = tagged(biome);
        return t == Type.ARID || t == Type.FROZEN || permanent(t) ? t : null;
    }

    // ── seasonal temperature by type ────────────────────────────────────

    /**
     * Serene Seasons' temperature for {@code sub} after each type's rule: frozen and permanent winter at most
     * 0.1 (snow all year); cold at most 0.1 from Late Autumn to Early Spring and at least 0.15 otherwise;
     * permanent spring and autumn at most 0.1 in the world's winter (their Early Spring / Late Autumn) and at
     * least 0.15 otherwise; permanent summer at least 0.15. 0.15 is just warm enough to rain and melt.
     */
    public static float seasonTemperature(Season.SubSeason sub, Holder<Biome> biome, float t) {
        Type type = tagged(biome);
        if (type == null) return t;
        boolean snow;
        switch (type) {
            case FROZEN, PERMANENT_WINTER -> snow = true;
            case COLD -> snow = COLD_SNOW.contains(sub);
            case PERMANENT_SPRING, PERMANENT_AUTUMN -> snow = sub.getSeason() == Season.WINTER;
            case PERMANENT_SUMMER -> snow = false;
            default -> {
                return t;
            }
        }
        return snow ? Math.min(t, FROZEN_MAX_TEMPERATURE) : Math.max(t, RAIN_MIN_TEMPERATURE);
    }

    // ── permanent season calendar ───────────────────────────────────────

    /** The season a permanent type shows all year (HUD, crops). */
    @Nullable
    public static Season permanentSeason(@Nullable Type type) {
        if (type == null) return null;
        return switch (type) {
            case PERMANENT_SPRING -> Season.SPRING;
            case PERMANENT_SUMMER -> Season.SUMMER;
            case PERMANENT_AUTUMN -> Season.AUTUMN;
            case PERMANENT_WINTER -> Season.WINTER;
            default -> null;
        };
    }

    /** Days in one Serene Seasons sub-season. */
    private static int subSeasonDays(Level level) {
        ISeasonState state = SeasonHelper.getSeasonState(level);
        return Math.max(1, state.getSubSeasonDuration() / Math.max(1, state.getDayDuration()));
    }

    /**
     * 0-based day of a permanent spring/autumn year. Spring's year starts at the world's Early Winter (its Early
     * Spring is the world's winter), autumn's at the world's Early Spring (its Late Autumn is the world's winter).
     */
    private static int permanentDay(Type type, Level level) {
        int offset = type == Type.PERMANENT_SPRING ? 3 * subSeasonDays(level) : 0;
        return (SeasonHelper.getSeasonState(level).getDay() + offset) % yearDays(level);
    }

    /** Early (first 1/4 of the year), Mid (next 1/2) or Late (last 1/4) of a permanent spring/autumn. */
    public static Part permanentPart(Type type, Level level) {
        int quarter = 3 * subSeasonDays(level);
        int day = permanentDay(type, level);
        return day < quarter ? Part.EARLY : day < 3 * quarter ? Part.MID : Part.LATE;
    }

    /**
     * HUD date for a permanent spring/autumn: {day (1-based), length}, within its Early/Mid/Late part when
     * {@code byPart}, else within its whole year.
     */
    public static int[] permanentDate(Type type, Level level, boolean byPart) {
        int quarter = 3 * subSeasonDays(level);
        int day = permanentDay(type, level);
        if (!byPart) return new int[]{day + 1, yearDays(level)};
        return switch (permanentPart(type, level)) {
            case EARLY -> new int[]{day + 1, quarter};
            case MID -> new int[]{day - quarter + 1, 2 * quarter};
            case LATE -> new int[]{day - 3 * quarter + 1, quarter};
        };
    }

    /** The Serene Seasons sub-season whose grass tint a permanent season shows. */
    @Nullable
    public static Season.SubSeason permanentTint(@Nullable Type type, Level level) {
        if (type == null) return null;
        return switch (type) {
            case PERMANENT_SUMMER -> Season.SubSeason.MID_SUMMER;
            case PERMANENT_SPRING -> switch (permanentPart(type, level)) {
                case EARLY -> Season.SubSeason.EARLY_SPRING;
                case MID -> Season.SubSeason.MID_SPRING;
                case LATE -> Season.SubSeason.LATE_SPRING;
            };
            case PERMANENT_AUTUMN -> switch (permanentPart(type, level)) {
                case EARLY -> Season.SubSeason.EARLY_AUTUMN;
                case MID -> Season.SubSeason.MID_AUTUMN;
                case LATE -> Season.SubSeason.LATE_AUTUMN;
            };
            default -> null;
        };
    }

    // ── snow line and snow seasons ──────────────────────────────────────

    /** Rain stays rain at this temperature (Biome.warmEnoughToRain: >= 0.15). */
    private static final float RAIN_MIN_TEMPERATURE = 0.15F;

    /** Per sub-season: biomes that may not snow then ({@code creativeengineering:snow/no_<sub_season>}). */
    private static final TagKey<Biome>[] SNOW_BLOCKED = snowBlockedTags();

    @SuppressWarnings("unchecked")
    private static TagKey<Biome>[] snowBlockedTags() {
        Season.SubSeason[] subs = Season.SubSeason.values();
        TagKey<Biome>[] tags = new TagKey[subs.length];
        for (Season.SubSeason sub : subs) {
            tags[sub.ordinal()] = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(CreativeCalendar.NAMESPACE,
                    "snow/no_" + sub.name().toLowerCase(Locale.ROOT)));
        }
        return tags;
    }

    /**
     * The snow line and the snow seasons, applied to a temperature Serene Seasons has already worked out for
     * {@code pos} (seasonal in its dimensions, vanilla elsewhere). The line is CalendarConfig seasons.snowLineY, the
     * overworld build height. Below it:
     * - a column whose biome is warm enough to rain at y80 right now rains all the way up (vanilla cools it by
     *   0.05 per 40 blocks above y80, so mountains and the floating skylands snowed in spring and autumn);
     * - a biome tagged snow/no_&lt;sub_season&gt; (season_types.json snow_seasons) rains instead of snowing then.
     * Either way the temperature is lifted to 0.15, just warm enough to rain and melt, and never lowered.
     * At or above it, temperate and cold biomes (tagged, or temperate by Serene Seasons' rule) always snow: no
     * block can sit there, so snowfall only tells a flyer they are above build height.
     * Frozen biomes and the permanent seasons keep their own rules ({@link #seasonTemperature}). Only called
     * for real levels (worldgen keeps vanilla snow caps).
     */
    public static float applySnowRules(Level level, Holder<Biome> biome, BlockPos pos, float temperature) {
        if (!enabled()) return temperature;
        Type type = tagged(biome);
        if (frozenLike(type) || permanent(type)) return temperature;
        boolean seasons = ServerConfig.isDimensionWhitelisted(level.dimension());
        if (pos.getY() >= CalendarConfig.SEASONS.snowLineY.get()) {
            boolean temperate = temperateLike(type) || type == null && BiomeConfig.enablesSeasonalEffects(biome)
                    && !BiomeConfig.usesTropicalSeasons(biome);
            return seasons && temperate ? Math.min(temperature, FROZEN_MAX_TEMPERATURE) : temperature;
        }
        if (temperature >= RAIN_MIN_TEMPERATURE) return temperature;
        if (seasons && biome.is(SNOW_BLOCKED[SeasonHelper.getSeasonState(level).getSubSeason().ordinal()])) {
            return RAIN_MIN_TEMPERATURE;
        }
        // same column at y80, where vanilla's height cooling starts (this call skips this branch: y is not above 80)
        if (pos.getY() > 80 && SeasonHooks.getBiomeTemperature(level, biome, pos.atY(80)) >= RAIN_MIN_TEMPERATURE) {
            return RAIN_MIN_TEMPERATURE;
        }
        return temperature;
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
        int radius = CalendarConfig.SEASONS.adaptiveRadius.get();
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
        TYPE_CACHE = new ConcurrentHashMap<>();
        SeasonAnswers.clear();
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
