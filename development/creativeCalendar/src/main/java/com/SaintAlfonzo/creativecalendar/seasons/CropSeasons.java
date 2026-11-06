package com.SaintAlfonzo.creativecalendar.seasons;

import com.SaintAlfonzo.creativecalendar.CalendarConfig;
import com.SaintAlfonzo.creativecalendar.CreativeCalendar;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.FertilityConfig;
import sereneseasons.config.ServerConfig;
import sereneseasons.init.ModTags;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Crop growing seasons by crop type (owner spec, feature_writeup/crops.md 2.12.1). Replaces Serene Seasons'
 * "in season / out of season at 1/6 speed" rule (its fertility.toml {@code seasonal_crops} is off).
 *
 * <p>Each crop block is in at most one block tag {@code creativeengineering:crop_seasons/<type>} (filled from
 * kubejs/generation_scripts/worldgen/crop_seasons.json by server_scripts/tags/cropseasontags.js). Where it stands,
 * each type is {@link Fertility#FERTILE} (growth +25 %), {@link Fertility#NEUTRAL} or {@link Fertility#OFF}
 * (growth -25 %), read from the biome's own calendar after the pack's season types ({@link SeasonTypes}):
 * <ul>
 *   <li>four seasons in temperate and cold biomes (and untagged ones Serene Seasons calls temperate); frozen and
 *       permanent winter biomes and any spot too cold to rain count as winter, a permanent season as that season;
 *   <li>wet / dry in tropical, dry and arid biomes (and untagged ones Serene Seasons calls tropical), from
 *       Serene Seasons' tropical season (wet = Early, Mid, Late Wet).
 * </ul>
 * Seasons only reach the overworld (Serene Seasons' whitelisted dimensions) and the surface: elsewhere, in a
 * blacklisted biome or below Serene Seasons' underground_fertility_level without sky, seasonal types are neutral.
 * An infertile biome (Serene Seasons' config) is off. Greenhouse glass above makes any season neutral.
 * Nether and End types go by dimension, cave crops by height, mushroom colonies by mushroom biome, grapes by
 * biome family ({@link #familyAt}).
 *
 * <p>Growth: every growth check fires Forge's CropGrowEvent.Pre. Off refuses {@code offPenalty} of them. Fertile
 * queues {@code fertileBonus} of them for a second random tick of the same block at the end of the level tick
 * (never nested inside the crop's own tick, and the second tick never queues a third), so the crop's own growth
 * odds, light and soil rules apply to the extra check too. Bone meal is not limited.
 */
@Mod.EventBusSubscriber(modid = CreativeCalendar.MOD_ID)
public final class CropSeasons {

    public enum Fertility { FERTILE, NEUTRAL, OFF }

    private static final Fertility F = Fertility.FERTILE, N = Fertility.NEUTRAL, O = Fertility.OFF;

    /** Biome families, for the grape types (by biome, not by season). */
    public enum Family { TEMPERATE, COLD, WARM_DRY, WARM_WET }

    /**
     * Crop types. {@code fourSeasons} = spring, summer, autumn, winter; {@code wetDry} = wet, dry. Special types
     * (nether, end, cave, mushroom, none) do not read the calendar, except mushroom outside mushroom biomes,
     * which is temperate. Grape types read the biome family instead: fertile in their own family, off in the
     * opposite temperature (cold against warm), neutral otherwise; {@code grape} resolves from the block's
     * {@code grape} property (Vinery's stem and lattice hold any grape kind).
     */
    public enum Type {
        TEMPERATE("temperate", new Fertility[]{F, N, N, O}, new Fertility[]{N, O}),
        COLD("cold", new Fertility[]{N, O, N, F}, new Fertility[]{O, O}),
        WARM_DRY("warm_dry", new Fertility[]{N, F, N, O}, new Fertility[]{N, F}),
        WARM_WET("warm_wet", new Fertility[]{N, F, N, O}, new Fertility[]{F, N}),
        /** Vinery apple trees: fertile autumn, neutral spring and summer, off winter; wet/dry as temperate. */
        AUTUMN_FRUIT("autumn_fruit", new Fertility[]{N, N, F, O}, new Fertility[]{N, O}),
        /** Grasses (farming/crops #28): off in winter only, neutral in every other season and in wet/dry biomes; their spread, see {@link #refuseSpread}. */
        GRASS("grass", new Fertility[]{N, N, N, O}, new Fertility[]{N, N}),
        NETHER("nether", null, null),
        END("end", null, null),
        CAVE("cave", null, null),
        MUSHROOM("mushroom", null, null),
        GRAPE_TEMPERATE("grape_temperate", Family.TEMPERATE),
        GRAPE_COLD("grape_cold", Family.COLD),
        GRAPE_WARM_DRY("grape_warm_dry", Family.WARM_DRY),
        GRAPE_WARM_WET("grape_warm_wet", Family.WARM_WET),
        /** The grape type of the grape the block holds (Vinery stem and lattice); none while it holds no grape. */
        GRAPE("grape", null, null),
        /** Listed so the json documents it: seasons never apply (kelp, sea grapes). */
        NONE("none", null, null);

        public final String id;
        @Nullable public final Fertility[] fourSeasons;
        @Nullable public final Fertility[] wetDry;
        @Nullable public final Family family;
        public final TagKey<Block> blockTag;
        public final TagKey<Item> itemTag;

        Type(String id, Family family) {
            this(id, null, null, family);
        }

        Type(String id, @Nullable Fertility[] fourSeasons, @Nullable Fertility[] wetDry) {
            this(id, fourSeasons, wetDry, null);
        }

        Type(String id, @Nullable Fertility[] fourSeasons, @Nullable Fertility[] wetDry, @Nullable Family family) {
            this.id = id;
            this.fourSeasons = fourSeasons;
            this.wetDry = wetDry;
            this.family = family;
            ResourceLocation rl = new ResourceLocation(CreativeCalendar.NAMESPACE, "crop_seasons/" + id);
            this.blockTag = TagKey.create(Registry.BLOCK_REGISTRY, rl);
            this.itemTag = TagKey.create(Registry.ITEM_REGISTRY, rl);
        }

        public boolean seasonal() {
            return fourSeasons != null;
        }
    }

    private static final Type[] TYPES = Type.values();
    private static final Object UNTYPED = new Object();
    /** Type per block (holders compare by identity); replaced on tag reload. */
    private static volatile Map<Block, Object> BLOCK_CACHE = new ConcurrentHashMap<>();
    private static final int GLASS_REACH = 16;

    /** Positions queued for a second random tick, per level, in queue order. Server thread only. */
    private static final Map<ServerLevel, LongLinkedOpenHashSet> EXTRA = new WeakHashMap<>();
    private static boolean inExtraTick;

    private CropSeasons() {}

    public static boolean enabled() {
        return CalendarConfig.CROP_SEASONS.enabled.get();
    }

    @Nullable
    public static Type typeOf(BlockState state) {
        Block block = state.getBlock();
        Map<Block, Object> cache = BLOCK_CACHE;
        Object hit = cache.get(block);
        if (hit == null) {
            hit = UNTYPED;
            for (Type t : TYPES) {
                if (state.is(t.blockTag)) {
                    hit = t;
                    break;
                }
            }
            cache.put(block, hit);
        }
        if (hit == UNTYPED) return null;
        Type type = (Type) hit;
        return type == Type.GRAPE ? heldGrape(state) : type;
    }

    /** Vinery grape kinds by id: taiga_* cold, savanna_* warm dry, jungle_* warm wet, the rest temperate. */
    @Nullable
    private static Type heldGrape(BlockState state) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty("grape");
        if (prop == null) return null;
        String kind = valueName(state, prop);
        if (kind.isEmpty() || kind.equals("none")) return null;
        if (kind.startsWith("taiga")) return Type.GRAPE_COLD;
        if (kind.startsWith("savanna")) return Type.GRAPE_WARM_DRY;
        if (kind.startsWith("jungle")) return Type.GRAPE_WARM_WET;
        return Type.GRAPE_TEMPERATE;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> prop) {
        return prop.getName(state.getValue(prop));
    }

    /** The type shown on a planting item: its own item tag first, else the block it places. */
    @Nullable
    public static Type typeOf(ItemStack stack) {
        for (Type t : TYPES) {
            if (stack.is(t.itemTag)) return t;
        }
        if (stack.getItem() instanceof BlockItem bi) return typeOf(bi.getBlock().defaultBlockState());
        return null;
    }

    /**
     * The type to show players (tooltip, WTHIT): like {@link #typeOf(BlockState)}, but null for plants that do not grow
     * (grass, tall grass, ferns: bush-shaped blocks of the grass type). They stay typed for the spread hooks.
     */
    @Nullable
    public static Type displayTypeOf(BlockState state) {
        Type type = typeOf(state);
        return type == Type.GRASS && state.getBlock() instanceof net.minecraft.world.level.block.BushBlock ? null : type;
    }

    /** Item form of {@link #displayTypeOf(BlockState)}; the planting-item tag types always show. */
    @Nullable
    public static Type displayTypeOf(ItemStack stack) {
        Type type = typeOf(stack);
        if (type == Type.GRASS && stack.getItem() instanceof BlockItem bi) return displayTypeOf(bi.getBlock().defaultBlockState());
        return type;
    }

    /** What the crop's type makes of where it stands; NEUTRAL for an untyped block or with the feature off. */
    public static Fertility fertility(Level level, BlockPos pos, BlockState state) {
        if (!enabled()) return N;
        Type type = typeOf(state);
        return type == null ? N : fertility(level, pos, type);
    }

    public static Fertility fertility(Level level, BlockPos pos, Type type) {
        return fertility(level, pos, type, true);
    }

    /** {@code glass} false: as if no greenhouse glass were above (WTHIT uses it to say the glass is working). */
    public static Fertility fertility(Level level, BlockPos pos, Type type, boolean glass) {
        switch (type) {
            case NONE, GRAPE:
                return N;
            case GRAPE_TEMPERATE, GRAPE_COLD, GRAPE_WARM_DRY, GRAPE_WARM_WET: {
                Family here = familyAt(level, pos);
                if (here == null) return N;
                if (here == type.family) return greenhouse(level, pos, F, glass);
                boolean opposite = (type.family == Family.COLD && (here == Family.WARM_DRY || here == Family.WARM_WET))
                        || (here == Family.COLD && (type.family == Family.WARM_DRY || type.family == Family.WARM_WET));
                return opposite ? greenhouse(level, pos, O, glass) : N;
            }
            case NETHER:
                return level.dimension() == Level.NETHER ? F : O;
            case END:
                return level.dimension() == Level.END ? F : O;
            case CAVE: {
                int y = pos.getY();
                if (y < CalendarConfig.CROP_SEASONS.caveFertileBelowY.get()) return F;
                return y > CalendarConfig.CROP_SEASONS.caveOffAboveY.get() ? O : N;
            }
            case MUSHROOM: {
                Holder<Biome> biome = level.getBiome(pos);
                if (biome.is(Biomes.MUSHROOM_FIELDS) || biome.is(Tags.Biomes.IS_MUSHROOM)) return F;
                return greenhouse(level, pos, seasonal(level, pos, Type.TEMPERATE), glass);
            }
            default:
                return greenhouse(level, pos, seasonal(level, pos, type), glass);
        }
    }

    /**
     * Greenhouse rule (owner 2026-10-30): a greenhouse removes season fertility. Any block in
     * {@code #sereneseasons:greenhouse_glass} (the pack fills it from crop_seasons.json greenhouse_glass) in the 16
     * blocks straight above the crop makes a fertile or off result neutral. Only that column counts, other blocks
     * between the crop and the glass do not matter, and light is not checked. Nether, End and cave crops ignore it
     * (they are not seasonal).
     */
    private static Fertility greenhouse(Level level, BlockPos pos, Fertility f, boolean glass) {
        if (glass && f != N && CalendarConfig.CROP_SEASONS.greenhouse.get() && glassAbove(level, pos)) return N;
        return f;
    }

    /** The calendar rule for a seasonal type, before greenhouse glass. */
    public static Fertility seasonal(Level level, BlockPos pos, Type type) {
        if (!ServerConfig.isDimensionWhitelisted(level.dimension())) return N;
        int underground = FertilityConfig.undergroundFertilityLevel.get();
        if (underground > -1 && pos.getY() < underground && !level.canSeeSky(pos)) return N;
        Holder<Biome> biome = SeasonTypes.resolve(level, level.getBiome(pos), pos);
        if (BiomeConfig.infertileBiome(biome)) return O;
        if (!BiomeConfig.enablesSeasonalEffects(biome)) return N; // answers from the season tags (SeasonBiomeConfigMixin)
        SeasonTypes.Type t = SeasonTypes.tagged(biome);
        if (SeasonTypes.frozenLike(t)) return type.fourSeasons[3];
        Season permanent = SeasonTypes.permanentSeason(t);
        if (permanent != null) return type.fourSeasons[permanent.ordinal()];
        if (t == SeasonTypes.Type.ARID || (t != null && SeasonTypes.wetDryCycle(t))
                || (t == null && BiomeConfig.usesTropicalSeasons(biome))) {
            return type.wetDry[wet(SeasonHelper.getSeasonState(level).getTropicalSeason()) ? 0 : 1];
        }
        if (!biome.value().warmEnoughToRain(pos)) return type.fourSeasons[3];
        return type.fourSeasons[SeasonHelper.getSeasonState(level).getSeason().ordinal()];
    }

    /**
     * The biome family at {@code pos} (after rivers and beaches take the land's), null outside Serene Seasons'
     * dimensions. Tagged biomes by their season type: cold, frozen and permanent winter cold; dry and arid warm dry;
     * tropical warm wet; temperate and the other permanent seasons temperate. Untagged: Serene Seasons' tropical ones
     * warm dry when they never rain or their downfall is below 0.4, else warm wet; others cold below base
     * temperature 0.3, else temperate.
     */
    @Nullable
    public static Family familyAt(Level level, BlockPos pos) {
        if (!ServerConfig.isDimensionWhitelisted(level.dimension())) return null;
        Holder<Biome> biome = SeasonTypes.resolve(level, level.getBiome(pos), pos);
        SeasonTypes.Type t = SeasonTypes.tagged(biome);
        if (t != null) {
            return switch (t) {
                case COLD, FROZEN, PERMANENT_WINTER -> Family.COLD;
                case DRY, ARID -> Family.WARM_DRY;
                case TROPICAL -> Family.WARM_WET;
                default -> Family.TEMPERATE;
            };
        }
        Biome b = biome.value();
        if (BiomeConfig.usesTropicalSeasons(biome)) {
            return b.getPrecipitation() == Biome.Precipitation.NONE || b.getDownfall() < 0.4F ? Family.WARM_DRY : Family.WARM_WET;
        }
        return b.getBaseTemperature() < 0.3F ? Family.COLD : Family.TEMPERATE;
    }

    /** Whether the crop at {@code pos} is under the wet/dry calendar (for the WTHIT line's wording). */
    public static boolean wetDryAt(Level level, BlockPos pos) {
        if (!ServerConfig.isDimensionWhitelisted(level.dimension())) return false;
        Holder<Biome> biome = SeasonTypes.resolve(level, level.getBiome(pos), pos);
        SeasonTypes.Type t = SeasonTypes.tagged(biome);
        return t == SeasonTypes.Type.ARID || (t != null && SeasonTypes.wetDryCycle(t))
                || (t == null && BiomeConfig.usesTropicalSeasons(biome));
    }

    public static boolean wet(Season.TropicalSeason s) {
        return s == Season.TropicalSeason.EARLY_WET || s == Season.TropicalSeason.MID_WET || s == Season.TropicalSeason.LATE_WET;
    }

    public static boolean glassAbove(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        for (int i = 1; i <= GLASS_REACH; i++) {
            p.setY(pos.getY() + i);
            if (level.getBlockState(p).is(ModTags.Blocks.GREENHOUSE_GLASS)) return true;
        }
        return false;
    }

    // ── growth ──────────────────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (!enabled() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getResult() == Event.Result.DENY) return;
        Type type = typeOf(event.getState());
        if (type == null || type == Type.NONE) return;
        BlockPos pos = event.getPos();
        Fertility f = fertility(level, pos, type);
        if (f == O) {
            if (level.random.nextDouble() < CalendarConfig.CROP_SEASONS.offPenalty.get()) event.setResult(Event.Result.DENY);
        } else if (f == F && !inExtraTick) {
            if (level.random.nextDouble() < CalendarConfig.CROP_SEASONS.fertileBonus.get()) {
                EXTRA.computeIfAbsent(level, l -> new LongLinkedOpenHashSet()).add(pos.asLong());
            }
        }
    }

    /**
     * Spreading that fires no crop-grow event (farming/crops #28): vanilla grass-block spread
     * ({@code GrassSpreadSeasonMixin}) and Immersive Weathering's block growths ({@code IWGrowthSeasonMixin}) ask here
     * before each try. True = skip it: a seasonal type that is off where the block stands refuses {@code offPenalty}
     * ({@code grassOffPenalty} for the grass type) of its spreads, like the crop-grow rule. Fertile seasons add nothing here (the grass type has none).
     */
    public static boolean refuseSpread(ServerLevel level, BlockPos pos, @Nullable Type type) {
        if (type == null || !type.seasonal() || !enabled()) return false;
        double penalty = type == Type.GRASS ? CalendarConfig.CROP_SEASONS.grassOffPenalty.get() : CalendarConfig.CROP_SEASONS.offPenalty.get();
        return level.random.nextDouble() < penalty && fertility(level, pos, type) == O;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        LongLinkedOpenHashSet queue = EXTRA.get(level);
        if (queue == null || queue.isEmpty()) return;
        long[] positions = queue.toLongArray();
        queue.clear();
        inExtraTick = true;
        try {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (long packed : positions) {
                pos.set(packed);
                if (!level.isLoaded(pos)) continue;
                BlockState state = level.getBlockState(pos);
                if (state.isRandomlyTicking()) state.randomTick(level, pos.immutable(), level.random);
            }
        } finally {
            inExtraTick = false;
        }
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        BLOCK_CACHE = new ConcurrentHashMap<>();
    }
}
