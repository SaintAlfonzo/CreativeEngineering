package com.SaintAlfonzo.creativecommon.melting;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Melting (feature_writeup/melting.md, plan_bugfix_1114.md 3.8, plan_post_scratch_1115.md G7, G8): salt, fire and lava
 * melt ice and snow. Sources and targets are the {@link MeltTags} block tags, rules and chances {@code [melting]} in
 * creativecommon-common.toml.
 *
 * <p>Sources find, targets schedule (G8). A pair is found from either side: a target's random tick
 * ({@code mixin.MeltingBlockStateMixin}, BlockStateBase.randomTick) rolls its chance, then looks at its 6 neighbors,
 * then, only when block light reaches it, scans a sphere for fire and lava; a source that random ticks (magma, lava,
 * salt blocks) scans its own sphere, the chunk sections in reach checked by palette first. Each target found gets a
 * vanilla scheduled tick ({@code meltTickInterval}, saved with the chunk by the game); on that tick
 * ({@link #onScheduledTick}) it looks for a source again, rolls, melts one stage and schedules the next, until it is gone
 * or no source is in reach. No registry and no save data of our own.
 *
 * <p>Snow melts one layer per melt ({@code snowLayerMeltChance}); magma melts snow within {@code magmaInstantRange} at
 * once and stops snow forming and water freezing within {@code fireRange} ({@code mixin.BiomeMeltMixin}). Salt melts
 * only {@link MeltTags#SALTABLE} blocks, into {@code saltProducts}. Packed ice, blue ice and Immersive Weathering's
 * cryosol and permafrost (meltable_slow) take no heat while {@code slowHeatMelting} is off (G7.15). Snow resting on a
 * {@link MeltTags#KEEPS_SNOW} block ignores heat and vanilla light, not salt (G6 4b).
 *
 * <p>Ultra-warm dimensions: melted ice leaves air, and blocks in {@link MeltTags#ULTRAWARM_INSTANT} become air the
 * moment they are placed ({@link #onPlaced}). Vanilla light melting is cut by {@code mixin.VanillaLightMeltMixin}
 * unless {@code vanillaLightMelting} is on.
 */
public final class Melting {

	private static final Logger LOGGER = LogUtils.getLogger();

	/** Fire sources giving less light than this are found only by their own tick (magma, 3). Light 4 reaches 3 blocks. */
	public static final int DIM_LIGHT = 4;
	/** Largest range the config allows (sphere offsets are precomputed to it). */
	private static final int MAX_RANGE = 8;

	private static final int MELTABLE = 1, SLOW = 2, SALT = 4, FIRE = 8, LAVA = 16, DRIVEN = 32, INSTANT = 64,
			SALTABLE = 128, KEEPS_SNOW = 256, MAGMA = 512, SRC_TICK = 1024;
	private static final int TARGET = MELTABLE | SLOW;
	/** What {@link #findSource} found. */
	private static final int NONE = 0, BY_SALT = 1, BY_HEAT = 2, BY_MAGMA_INSTANT = 3;
	private static final Direction[] DIRECTIONS = Direction.values();
	/** {dx, dy, dz, d2} for every offset within MAX_RANGE except the center, nearest first. */
	private static final int[][] OFFSETS = buildOffsets();

	private static volatile Reference2IntOpenHashMap<Block> flags = new Reference2IntOpenHashMap<>();
	private static List<? extends String> stepsSource, saltSource;
	private static Map<Block, BlockState> steps = Map.of(), saltProducts = Map.of();

	/**
	 * The [melting] config values the tick paths read, copied from the spec on config load and reload so a tick reads
	 * plain fields instead of ForgeConfigSpec values (SQ-MELT-P1). Before the first load: melting and the ultra-warm
	 * melt off, vanilla light melting as vanilla, the same as the former isLoaded() checks.
	 */
	record Values(boolean enabled, int saltRange, int fireRange, int lavaRange, int slowFireRange,
			int slowLavaRange, double meltChance, double slowMeltChance, double snowLayerMeltChance, boolean placeWater,
			boolean vanillaLightMelting, boolean ultraWarmInstantMelt, boolean slowHeatMelting, int meltTickInterval,
			int magmaInstantRange, boolean magmaStopsFreezing, int saltMeltSnowLayers) {
		static final Values UNLOADED = new Values(false, 0, 0, 0, 0, 0, 0, 0, 0, true, true, false, false, 0, 0, false, 1);

		static Values read() {
			CommonConfig.MeltingConfig c = CommonConfig.MELTING;
			return new Values(c.enabled.get(), c.saltRange.get(), c.fireRange.get(), c.lavaRange.get(),
					c.slowFireRange.get(), c.slowLavaRange.get(), c.meltChance.get(), c.slowMeltChance.get(),
					c.snowLayerMeltChance.get(), c.placeWater.get(), c.vanillaLightMelting.get(),
					c.ultraWarmInstantMelt.get(), c.slowHeatMelting.get(), c.meltTickInterval.get(),
					c.magmaInstantRange.get(), c.magmaStopsFreezing.get(), c.saltMeltSnowLayers.get());
		}
	}

	private static volatile Values cfg = Values.UNLOADED;

	/**
	 * Told after a salt source melted a target by a tick (plan_post_scratch_1115 B3): Creative Delight removes a salt
	 * layer by chance. Not called for {@link #meltStep} (the caller spends its own salt).
	 */
	@FunctionalInterface
	public interface SaltMeltListener {
		void onSaltMelt(ServerLevel level, BlockPos source, BlockState sourceState, RandomSource random);
	}

	private static volatile SaltMeltListener saltListener;

	/** One listener (the last set wins); null removes it. */
	public static void setSaltMeltListener(SaltMeltListener listener) {
		saltListener = listener;
	}

	private Melting() {}

	/** Mod bus: ModConfigEvent.Loading and Reloading of Common's main config refresh {@link #cfg}. */
	public static void onConfig(ModConfigEvent event) {
		if (event.getConfig().getSpec() == CommonConfig.COMMON_SPEC && CommonConfig.COMMON_SPEC.isLoaded())
			reloadConfig();
	}

	/** Copies the [melting] values (config events; the headless test). */
	static void reloadConfig() {
		cfg = Values.read();
	}

	// ------------------------------------------------------------------ setup

	/** Common setup, before any chunk loads (a section counts its random tickers when it loads). */
	public static void initRandomTicks() {
		Field ticking;
		try {
			ticking = ObfuscationReflectionHelper.findField(BlockBehaviour.class, "f_60445_");
		} catch (RuntimeException e) {
			LOGGER.error("[melting] could not reach BlockBehaviour.isRandomlyTicking; extraRandomTickBlocks ignored", e);
			return;
		}
		int done = 0;
		for (String id : CommonConfig.MELTING.extraRandomTickBlocks.get()) {
			ResourceLocation rl = ResourceLocation.tryParse(id);
			Block block = rl == null ? null : ForgeRegistries.BLOCKS.getValue(rl);
			if (block == null || block == Blocks.AIR) continue; // absent mod
			try {
				ticking.setBoolean(block, true);
				done++;
			} catch (IllegalAccessException e) {
				LOGGER.error("[melting] could not turn on random ticks for {}", id, e);
			}
		}
		LOGGER.info("[melting] random ticks on for {} extra blocks", done);
	}

	/** Rebuilds the per-block flags whenever tags (re)load: server data load and the client's copy. */
	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
		if (tags == null) return;
		Reference2IntOpenHashMap<Block> map = new Reference2IntOpenHashMap<>();
		mark(map, tags, MeltTags.MELTABLE, MELTABLE);
		mark(map, tags, MeltTags.MELTABLE_SLOW, SLOW);
		mark(map, tags, MeltTags.SALT, SALT);
		mark(map, tags, MeltTags.FIRE, FIRE);
		mark(map, tags, MeltTags.LAVA, LAVA);
		mark(map, tags, MeltTags.ULTRAWARM_INSTANT, INSTANT);
		mark(map, tags, MeltTags.SALTABLE, SALTABLE);
		mark(map, tags, MeltTags.KEEPS_SNOW, KEEPS_SNOW);
		mark(map, tags, MeltTags.MAGMA, MAGMA);
		map.put(Blocks.LAVA, map.getInt(Blocks.LAVA) | LAVA); // the lava block itself (its fluid counts anyway)

		// Fire blocks that give little light are found only by their own tick; any source that random ticks scans
		TreeSet<String> driven = new TreeSet<>(), dimStill = new TreeSet<>();
		int fire = 0, ticking = 0;
		for (Block block : tags.getTag(MeltTags.FIRE)) {
			fire++;
			int light = 0;
			boolean ticks = false;
			for (BlockState s : block.getStateDefinition().getPossibleStates()) {
				if (!fireActive(s)) continue;
				light = Math.max(light, s.getLightEmission());
				ticks |= s.isRandomlyTicking();
			}
			if (ticks) map.put(block, map.getInt(block) | SRC_TICK);
			if (light >= DIM_LIGHT) continue;
			String id = String.valueOf(ForgeRegistries.BLOCKS.getKey(block));
			if (ticks) {
				map.put(block, map.getInt(block) | DRIVEN);
				driven.add(id + " (light " + light + ")");
			} else {
				dimStill.add(id + " (light " + light + ")");
			}
		}
		for (Block block : tags.getTag(MeltTags.SALT)) markTicking(map, block);
		for (Block block : tags.getTag(MeltTags.LAVA)) markTicking(map, block);
		markTicking(map, Blocks.LAVA);
		for (var e : map.reference2IntEntrySet()) if ((e.getIntValue() & SRC_TICK) != 0) ticking++;
		map.trim();
		flags = map;
		if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
			LOGGER.info("[melting] tags: {} meltable, {} slow, {} salt, {} fire, {} lava, {} ultra-warm instant, {} saltable,"
							+ " {} keeps snow, {} magma; {} sources scan on their own tick",
					count(tags, MeltTags.MELTABLE), count(tags, MeltTags.MELTABLE_SLOW), count(tags, MeltTags.SALT),
					fire, count(tags, MeltTags.LAVA), count(tags, MeltTags.ULTRAWARM_INSTANT),
					count(tags, MeltTags.SALTABLE), count(tags, MeltTags.KEEPS_SNOW), count(tags, MeltTags.MAGMA), ticking);
			LOGGER.info("[melting] source-driven fire (light below {}, random ticks): {}", DIM_LIGHT, driven);
			if (!dimStill.isEmpty())
				LOGGER.warn("[melting] fire tag blocks with light below {} that do not random tick (they melt only touching"
						+ " targets): {}", DIM_LIGHT, dimStill);
		}
	}

	private static void markTicking(Reference2IntOpenHashMap<Block> map, Block block) {
		for (BlockState s : block.getStateDefinition().getPossibleStates())
			if (s.isRandomlyTicking()) {
				map.put(block, map.getInt(block) | SRC_TICK);
				return;
			}
	}

	private static void mark(Reference2IntOpenHashMap<Block> map, ITagManager<Block> tags, TagKey<Block> key, int bit) {
		for (Block block : tags.getTag(key)) map.put(block, map.getInt(block) | bit);
	}

	private static int count(ITagManager<Block> tags, TagKey<Block> key) {
		return tags.getTag(key).size();
	}

	// ------------------------------------------------------------------ hooks

	/**
	 * BlockStateBase.randomTick, before the block's own. True when the block melted (the caller skips the block's own
	 * random tick, which would act on a state that is gone).
	 */
	public static boolean onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int f = flags.getInt(state.getBlock());
		if ((f & (TARGET | SRC_TICK)) == 0) return false;
		if (!cfg.enabled()) return false;
		if ((f & TARGET) != 0) return targetTick(state, f, level, pos, random);
		sourceTick(state, f, level, pos, random);
		return false;
	}

	/**
	 * BlockStateBase.tick (scheduled ticks), before the block's own: a target near a source melts one stage by chance
	 * and schedules its next tick. True when it melted (the caller skips the block's own tick).
	 */
	public static boolean onScheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int f = flags.getInt(state.getBlock());
		if ((f & TARGET) == 0) return false;
		Values c = cfg;
		if (!c.enabled() || state.getBlock() instanceof FrostedIceBlock) return false; // frosted ice runs its own ticks
		Hit hit = new Hit();
		if (findSource(state, f, level, pos, true, hit) == NONE) return false; // gone out of reach: no next tick
		boolean melted = false;
		if (hit.kind == BY_MAGMA_INSTANT) {
			meltTo(level, pos, state, Blocks.AIR.defaultBlockState());
			return true;
		}
		if (random.nextDouble() < chance(state, f, c)) {
			meltBy(level, pos, state, f, hit, random);
			melted = true;
		}
		BlockState now = melted ? level.getBlockState(pos) : state;
		if (isTarget(now)) schedule(level, pos, now.getBlock(), random, c);
		return melted;
	}

	/** BlockStateBase.onPlace, after the block's own: snow placed in an ultra-warm dimension or beside magma melts. */
	public static void onPlaced(BlockState state, Level level, BlockPos pos) {
		if (level.isClientSide) return;
		int f = flags.getInt(state.getBlock());
		if ((f & (INSTANT | TARGET)) == 0) return;
		Values c = cfg;
		if (level.dimensionType().ultraWarm()) {
			if ((f & INSTANT) == 0 || !c.ultraWarmInstantMelt()) return;
			if (level.getBlockState(pos) != state) return; // already replaced by the block's own onPlace
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			level.levelEvent(1501, pos, 0); // lava fizz: hiss and smoke
			return;
		}
		if (!c.enabled() || c.magmaInstantRange() < 1 || !(level instanceof ServerLevel sl)) return;
		if (!state.is(BlockTags.SNOW) || keepsSnow(sl, pos)) return;
		if (level.getBlockState(pos) != state) return;
		if (!magmaWithin(sl, pos, c.magmaInstantRange())) return;
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		level.levelEvent(1501, pos, 0);
	}

	/**
	 * Biome.shouldSnow / shouldFreeze on the server (mixin.BiomeMeltMixin): true when magma within {@code fireRange} of
	 * the position stops snow forming or water freezing there (G7.13, G6 A).
	 */
	public static boolean magmaBlocksFreezing(LevelReader reader, BlockPos pos) {
		if (!(reader instanceof ServerLevel level)) return false; // worldgen regions: no check
		Values c = cfg;
		if (!c.enabled() || !c.magmaStopsFreezing() || c.fireRange() < 1) return false;
		return magmaWithin(level, pos, c.fireRange());
	}

	/** The light vanilla IceBlock / SnowLayerBlock (and Rare Ice) melt by: 0 unless vanillaLightMelting is on. */
	public static int vanillaLight(ServerLevel level, LightLayer layer, BlockPos pos) {
		if (!cfg.vanillaLightMelting()) return 0;
		if (keepsSnow(level, pos) && level.getBlockState(pos).is(BlockTags.SNOW)) return 0; // snow on blue ice (G6 4b)
		return level.getBrightness(layer, pos);
	}

	// ------------------------------------------------------------------ public API (salt dispenser, Salt Melt, other jars)

	/** Whether the state is a melting target (meltable or meltable_slow): heat, salt or both may melt it. */
	public static boolean isTarget(BlockState state) {
		return (flags.getInt(state.getBlock()) & TARGET) != 0;
	}

	/** Whether salt can melt the state (tag creativecommon:melting/saltable, G7.11). Drives the "Saltable" tooltip. */
	public static boolean isSaltable(BlockState state) {
		return (flags.getInt(state.getBlock()) & SALTABLE) != 0;
	}

	/**
	 * One salt melt at once, no roll and no source needed: what a salt dispenser and a Salt Melt use do
	 * (plan_post_scratch_1115 B2, B6, G7.11, G7.14). Removes {@code saltMeltSnowLayers} snow layers (a snow block counts
	 * as 8 layers), else melts into the block's {@code saltProducts} row, water for ice, or air. False when salt cannot
	 * melt the block (not saltable: blue ice, permafrost, non-targets).
	 */
	public static boolean meltStep(ServerLevel level, BlockPos pos) {
		return saltMelt(level, pos, cfg.saltMeltSnowLayers());
	}

	/** {@link #meltStep} with an explicit number of snow layers to remove (at least 1). */
	public static boolean saltMelt(ServerLevel level, BlockPos pos, int snowLayers) {
		BlockState state = level.getBlockState(pos);
		if (!isSaltable(state)) return false;
		meltTo(level, pos, state, saltedState(state, level.dimensionType().ultraWarm(), Math.max(1, snowLayers)));
		return true;
	}

	/** What the state becomes when heat melts it once: one snow layer less, a slow step, water for ice, else air. */
	public static BlockState meltedState(BlockState state, Level level, boolean slow) {
		return meltedState(state, level.dimensionType().ultraWarm(), slow);
	}

	/** What the state becomes when salt melts it, {@code snowLayers} snow layers at a time. */
	public static BlockState saltedState(BlockState state, Level level, int snowLayers) {
		return saltedState(state, level.dimensionType().ultraWarm(), snowLayers);
	}

	static BlockState meltedState(BlockState state, boolean ultraWarm, boolean slow) {
		if (state.getBlock() instanceof SnowLayerBlock && state.hasProperty(SnowLayerBlock.LAYERS)) {
			int layers = state.getValue(SnowLayerBlock.LAYERS);
			if (layers > 1) return state.setValue(SnowLayerBlock.LAYERS, layers - 1);
		}
		if (slow) {
			BlockState step = steps().get(state.getBlock());
			if (step != null) return withShared(state, step);
		}
		return waterOrAir(state, ultraWarm);
	}

	static BlockState saltedState(BlockState state, boolean ultraWarm, int snowLayers) {
		int layers = -1;
		if (state.getBlock() instanceof SnowLayerBlock && state.hasProperty(SnowLayerBlock.LAYERS))
			layers = state.getValue(SnowLayerBlock.LAYERS);
		else if (state.is(Blocks.SNOW_BLOCK))
			layers = SnowLayerBlock.MAX_HEIGHT;
		if (layers > 0) {
			int left = layers - snowLayers;
			if (left <= 0) return Blocks.AIR.defaultBlockState();
			BlockState base = state.getBlock() instanceof SnowLayerBlock ? state : Blocks.SNOW.defaultBlockState();
			return base.setValue(SnowLayerBlock.LAYERS, left);
		}
		BlockState product = saltProducts().get(state.getBlock());
		if (product != null) return withShared(state, product);
		return waterOrAir(state, ultraWarm);
	}

	private static BlockState waterOrAir(BlockState state, boolean ultraWarm) {
		if (!ultraWarm && cfg.placeWater() && state.is(BlockTags.ICE)) return Blocks.WATER.defaultBlockState();
		return Blocks.AIR.defaultBlockState();
	}

	/** {@code to} with every property it shares with {@code from} copied over (facing, half, slab type, waterlogged). */
	static BlockState withShared(BlockState from, BlockState to) {
		for (Property<?> p : from.getProperties())
			if (to.hasProperty(p)) to = copy(from, to, p);
		return to;
	}

	private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> p) {
		return to.setValue(p, from.getValue(p));
	}

	// ------------------------------------------------------------------ rules

	/** A target's random tick: roll (cheap gate for dark glaciers), find a source, melt, schedule the follow-up. */
	private static boolean targetTick(BlockState state, int f, ServerLevel level, BlockPos pos, RandomSource random) {
		Values c = cfg;
		if (random.nextDouble() >= chance(state, f, c)) return false;
		Hit hit = new Hit();
		int kind = findSource(state, f, level, pos, false, hit);
		if (kind == NONE) return false;
		meltBy(level, pos, state, f, hit, random);
		BlockState now = level.getBlockState(pos);
		if (isTarget(now)) schedule(level, pos, now.getBlock(), random, c);
		return true;
	}

	/**
	 * A random-ticking source (magma, lava, salt blocks): every target in its reach gets a scheduled melt tick; snow
	 * within magma's instant range melts at once. Lava skips this in ultra-warm dimensions (lava seas; ice there is
	 * found by its own random tick).
	 */
	private static void sourceTick(BlockState state, int f, ServerLevel level, BlockPos pos, RandomSource random) {
		Values c = cfg;
		boolean salt = (f & SALT) != 0;
		boolean fire = (f & FIRE) != 0 && fireActive(state);
		boolean lava = (f & LAVA) != 0 || isLava(state);
		boolean magma = fire && (f & MAGMA) != 0;
		if (lava && !fire && !salt && level.dimensionType().ultraWarm()) return;
		int saltR = salt ? c.saltRange() : 0;
		int heatR = fire ? c.fireRange() : lava ? c.lavaRange() : 0;
		int slowR = !c.slowHeatMelting() ? 0 : fire ? c.slowFireRange() : lava ? c.slowLavaRange() : 0;
		int r = Math.max(saltR, Math.max(heatR, slowR));
		if (r < 1) return;
		ChunkReader reader = new ChunkReader(level);
		if (!reader.mayHave(pos, r, ChunkReader.TARGET)) return;
		int s2 = saltR * saltR, h2 = heatR * heatR, sl2 = slowR * slowR, max2 = r * r;
		int instant = magma ? c.magmaInstantRange() : 0, i2 = instant * instant;
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int[] o : OFFSETS) {
			int d2 = o[3];
			if (d2 > max2) break;
			m.set(pos.getX() + o[0], pos.getY() + o[1], pos.getZ() + o[2]);
			BlockState s = reader.get(m);
			if (s == null) continue;
			int tf = flags.getInt(s.getBlock());
			if ((tf & TARGET) == 0) continue;
			boolean slowT = (tf & MELTABLE) == 0;
			boolean saltOk = (tf & SALTABLE) != 0 && d2 <= s2;
			boolean heatOk = d2 <= (slowT ? sl2 : h2) && !(s.is(BlockTags.SNOW) && keepsSnow(reader, m));
			if (heatOk && d2 <= i2 && s.is(BlockTags.SNOW)) {
				meltTo(level, m.immutable(), s, Blocks.AIR.defaultBlockState());
				continue;
			}
			if (saltOk || heatOk) {
				if (c.meltTickInterval() > 0) {
					schedule(level, m, s.getBlock(), random, c);
				} else if (random.nextDouble() < chance(s, tf, c)) { // scheduling off: one chance roll per random tick
					BlockPos at = m.immutable();
					meltTo(level, at, s, heatOk ? meltedState(s, level, slowT) : saltedState(s, level, 1));
					if (!heatOk) afterMelt(level, pos, state, random);
				}
			}
		}
	}

	/** The found source: kind, its position and state. */
	private static final class Hit {
		int kind = NONE;
		BlockState state;
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		int set(int kind, BlockState state, BlockPos pos) {
			this.kind = kind;
			this.state = state;
			this.pos.set(pos);
			return kind;
		}
	}

	/**
	 * Looks for a source in reach of the target, nearest first. {@code scheduled}: a scheduled tick, which also counts
	 * sources found only by their own tick (magma) and so keeps a magma-found target going.
	 */
	private static int findSource(BlockState state, int f, ServerLevel level, BlockPos pos, boolean scheduled, Hit hit) {
		Values c = cfg;
		boolean slow = (f & MELTABLE) == 0; // in both tags: the faster rule wins
		boolean snow = state.is(BlockTags.SNOW);
		boolean heat = (!slow || c.slowHeatMelting()) && !(snow && keepsSnow(level, pos));
		int saltR = (f & SALTABLE) != 0 ? c.saltRange() : 0;
		int fireR = !heat ? 0 : slow ? c.slowFireRange() : c.fireRange();
		int lavaR = !heat ? 0 : slow ? c.slowLavaRange() : c.lavaRange();
		if (saltR < 1 && fireR < 1 && lavaR < 1) return NONE;
		int instant = heat && snow ? c.magmaInstantRange() : 0, i2 = instant * instant;

		// 1. the 6 neighbors (loaded: they border a ticking chunk at most)
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (Direction d : DIRECTIONS) {
			m.setWithOffset(pos, d);
			BlockState s = level.getBlockState(m);
			int kind = sourceKind(s, saltR >= 1, fireR >= 1, lavaR >= 1, scheduled);
			if (kind == BY_HEAT && i2 >= 1 && isMagma(s)) return hit.set(BY_MAGMA_INSTANT, s, m);
			if (kind != NONE) return hit.set(kind, s, m);
		}

		// 2. the sphere: salt only past 1 (it gives no light), fire and lava only where block light reaches;
		// on a scheduled tick also the dim sources (magma) without the light gate
		int saltScan = saltR >= 2 ? saltR : 0;
		boolean lit = (fireR >= 2 || lavaR >= 2) && blockLit(level, pos, m);
		int dimR = scheduled ? fireR : 0;
		int r = Math.max(saltScan, Math.max(lit ? Math.max(fireR, lavaR) : 0, dimR));
		if (r < 2) return NONE;
		int s2 = saltScan * saltScan, f2 = lit ? fireR * fireR : 0, l2 = lit ? lavaR * lavaR : 0, dim2 = dimR * dimR;
		int max2 = r * r;
		ChunkReader reader = new ChunkReader(level);
		for (int[] o : OFFSETS) {
			int d2 = o[3];
			if (d2 == 1) continue; // done above
			if (d2 > max2) break;
			m.set(pos.getX() + o[0], pos.getY() + o[1], pos.getZ() + o[2]);
			BlockState s = reader.get(m);
			if (s == null) continue;
			int sf = flags.getInt(s.getBlock());
			if (d2 <= s2 && (sf & SALT) != 0) return hit.set(BY_SALT, s, m);
			if ((sf & FIRE) != 0 && fireActive(s)) {
				boolean dim = (sf & DRIVEN) != 0;
				if (dim ? d2 <= dim2 : d2 <= f2) {
					if (d2 <= i2 && (sf & MAGMA) != 0) return hit.set(BY_MAGMA_INSTANT, s, m);
					return hit.set(BY_HEAT, s, m);
				}
			}
			if (d2 <= l2 && ((sf & LAVA) != 0 || isLava(s))) return hit.set(BY_HEAT, s, m);
		}
		return NONE;
	}

	private static int sourceKind(BlockState s, boolean salt, boolean fire, boolean lava, boolean scheduled) {
		int f = flags.getInt(s.getBlock());
		if (salt && (f & SALT) != 0) return BY_SALT;
		if (fire && (f & FIRE) != 0 && (scheduled || (f & DRIVEN) == 0) && fireActive(s)) return BY_HEAT;
		if (lava && ((f & LAVA) != 0 || isLava(s))) return BY_HEAT;
		return NONE;
	}

	private static void meltBy(ServerLevel level, BlockPos pos, BlockState state, int f, Hit hit, RandomSource random) {
		if (hit.kind == BY_SALT) {
			meltTo(level, pos, state, saltedState(state, level, 1));
			afterMelt(level, hit.pos, hit.state, random);
		} else if (hit.kind == BY_MAGMA_INSTANT) {
			meltTo(level, pos, state, Blocks.AIR.defaultBlockState());
		} else {
			meltTo(level, pos, state, meltedState(state, level, (f & MELTABLE) == 0));
		}
	}

	/** Chance per tick: a snow layer its own (G7.10), packed ice and the rest of the slow tier the low one. */
	private static double chance(BlockState state, int f, Values c) {
		if (state.getBlock() instanceof SnowLayerBlock) return c.snowLayerMeltChance();
		return (f & MELTABLE) == 0 ? c.slowMeltChance() : c.meltChance();
	}

	/** A scheduled melt tick at about meltTickInterval (75 to 125 percent, so a field does not melt in lockstep). */
	private static void schedule(ServerLevel level, BlockPos pos, Block block, RandomSource random, Values c) {
		int interval = c.meltTickInterval();
		if (interval <= 0 || block instanceof FrostedIceBlock) return;
		if (level.getBlockTicks().hasScheduledTick(pos, block)) return;
		level.scheduleTick(pos.immutable(), block, scheduleDelay(interval, random));
	}

	static int scheduleDelay(int interval, RandomSource random) {
		return Math.max(1, interval * 3 / 4 + random.nextInt(interval / 2 + 1));
	}

	/** A salt source did the melt: tell the listener (B3). */
	private static void afterMelt(ServerLevel level, BlockPos source, BlockState s, RandomSource random) {
		SaltMeltListener l = saltListener;
		if (l != null && (flags.getInt(s.getBlock()) & SALT) != 0) l.onSaltMelt(level, source.immutable(), s, random);
	}

	/** Snow on this position rests on a keeps_snow block (blue ice, IW permafrost): heat and light leave it (G6 4b). */
	private static boolean keepsSnow(ServerLevel level, BlockPos pos) {
		return (flags.getInt(level.getBlockState(pos.below()).getBlock()) & KEEPS_SNOW) != 0;
	}

	private static boolean keepsSnow(ChunkReader reader, BlockPos pos) {
		BlockState below = reader.get(pos.below());
		return below != null && (flags.getInt(below.getBlock()) & KEEPS_SNOW) != 0;
	}

	private static boolean isMagma(BlockState s) {
		return (flags.getInt(s.getBlock()) & MAGMA) != 0;
	}

	/** Any block of the magma tag within r of pos (palette check of the sections in reach first). */
	private static boolean magmaWithin(ServerLevel level, BlockPos pos, int r) {
		if (r < 1) return false;
		ChunkReader reader = new ChunkReader(level);
		if (!reader.mayHave(pos, r, ChunkReader.MAGMA_PRED)) return false;
		int max2 = Math.min(r, MAX_RANGE) * Math.min(r, MAX_RANGE);
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int[] o : OFFSETS) {
			if (o[3] > max2) break;
			m.set(pos.getX() + o[0], pos.getY() + o[1], pos.getZ() + o[2]);
			BlockState s = reader.get(m);
			if (s != null && isMagma(s)) return true;
		}
		return false;
	}

	private static boolean isLava(BlockState s) {
		FluidState fluid = s.getFluidState();
		return !fluid.isEmpty() && fluid.is(FluidTags.LAVA);
	}

	/** A block with a lit property burns only while lit (campfires, braziers, candelabras). */
	private static boolean fireActive(BlockState s) {
		return !s.hasProperty(BlockStateProperties.LIT) || s.getValue(BlockStateProperties.LIT);
	}

	/** Block light at the target or a neighbor (an opaque target such as a snow block holds none itself). */
	private static boolean blockLit(ServerLevel level, BlockPos pos, BlockPos.MutableBlockPos m) {
		if (level.getBrightness(LightLayer.BLOCK, pos) > 0) return true;
		for (Direction d : DIRECTIONS) {
			m.setWithOffset(pos, d);
			if (level.getBrightness(LightLayer.BLOCK, m) > 0) return true;
		}
		return false;
	}

	private static void meltTo(ServerLevel level, BlockPos pos, BlockState state, BlockState to) {
		level.setBlockAndUpdate(pos, to);
		level.levelEvent(2001, pos, Block.getId(state)); // the block's break particles and sound
	}

	private static Map<Block, BlockState> steps() {
		List<? extends String> src = CommonConfig.MELTING.slowSteps.get();
		if (src != stepsSource) {
			steps = parseRows(src);
			stepsSource = src;
		}
		return steps;
	}

	private static Map<Block, BlockState> saltProducts() {
		List<? extends String> src = CommonConfig.MELTING.saltProducts.get();
		if (src != saltSource) {
			saltProducts = parseRows(src);
			saltSource = src;
		}
		return saltProducts;
	}

	private static Map<Block, BlockState> parseRows(List<? extends String> rows) {
		Map<Block, BlockState> map = new IdentityHashMap<>();
		for (String row : rows) {
			int eq = row.indexOf('=');
			Block from = block(row.substring(0, eq)), to = block(row.substring(eq + 1));
			if (from != null && to != null) map.put(from, to.defaultBlockState());
		}
		return map;
	}

	private static Block block(String id) {
		ResourceLocation rl = ResourceLocation.tryParse(id);
		if (rl == null || !ForgeRegistries.BLOCKS.containsKey(rl)) return null;
		return ForgeRegistries.BLOCKS.getValue(rl);
	}

	private static int[][] buildOffsets() {
		List<int[]> out = new ArrayList<>();
		int r2 = MAX_RANGE * MAX_RANGE;
		for (int dx = -MAX_RANGE; dx <= MAX_RANGE; dx++)
			for (int dy = -MAX_RANGE; dy <= MAX_RANGE; dy++)
				for (int dz = -MAX_RANGE; dz <= MAX_RANGE; dz++) {
					int d2 = dx * dx + dy * dy + dz * dz;
					if (d2 != 0 && d2 <= r2) out.add(new int[]{dx, dy, dz, d2});
				}
		int[][] arr = out.toArray(new int[0][]);
		Arrays.sort(arr, (a, b) -> Integer.compare(a[3], b[3]));
		return arr;
	}

	/** Reads block states only from chunks already loaded, never loading or generating one; caches the last chunk. */
	private static final class ChunkReader {
		static final Predicate<BlockState> TARGET = s -> (flags.getInt(s.getBlock()) & Melting.TARGET) != 0;
		static final Predicate<BlockState> MAGMA_PRED = s -> (flags.getInt(s.getBlock()) & MAGMA) != 0;
		private final ServerLevel level;
		private int cx = Integer.MIN_VALUE, cz = Integer.MIN_VALUE;
		private LevelChunk chunk;

		ChunkReader(ServerLevel level) {
			this.level = level;
		}

		BlockState get(BlockPos p) {
			if (level.isOutsideBuildHeight(p)) return null;
			LevelChunk c = chunkAt(p.getX() >> 4, p.getZ() >> 4);
			return c == null ? null : c.getBlockState(p);
		}

		private LevelChunk chunkAt(int x, int z) {
			if (x != cx || z != cz) {
				cx = x;
				cz = z;
				chunk = level.getChunkSource().getChunkNow(x, z);
			}
			return chunk;
		}

		/** Whether any loaded section in the cube of radius r around pos may hold a matching state (palette check). */
		boolean mayHave(BlockPos pos, int r, Predicate<BlockState> what) {
			int minY = Math.max(level.getMinBuildHeight(), pos.getY() - r);
			int maxY = Math.min(level.getMaxBuildHeight() - 1, pos.getY() + r);
			if (minY > maxY) return false;
			for (int x = (pos.getX() - r) >> 4; x <= (pos.getX() + r) >> 4; x++)
				for (int z = (pos.getZ() - r) >> 4; z <= (pos.getZ() + r) >> 4; z++) {
					LevelChunk c = chunkAt(x, z);
					if (c == null) continue;
					for (int i = c.getSectionIndex(minY), top = c.getSectionIndex(maxY); i <= top; i++) {
						LevelChunkSection section = c.getSection(i);
						if (!section.hasOnlyAir() && section.maybeHas(what)) return true;
					}
				}
			return false;
		}
	}
}
