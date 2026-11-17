package com.SaintAlfonzo.creativecommon.melting;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * Melting (feature_writeup/melting.md, plan_bugfix_1114.md 3.8): salt, fire and lava melt ice and snow; packed and blue
 * ice step down one tier. Sources and targets are the {@link MeltTags} block tags, rules and chances {@code [melting]}
 * in creativecommon-common.toml.
 *
 * <p>Target-driven: every random tick of a target ({@code mixin.MeltingBlockStateMixin}, BlockStateBase.randomTick)
 * rolls its chance first, then looks at its 6 neighbors (salt, fire, lava within 1), then, only when block light
 * reaches it or a neighbor, scans a sphere (nearest first, through any blocks) for fire and lava in reach. Dark
 * glaciers cost one roll and, on success, 6 block reads. Source-driven: a fire source whose light is below
 * {@link #DIM_LIGHT} (magma, 3) would hide in the dark at range, so its own random tick melts the targets in its sphere
 * (each with its own roll) and the target side ignores it; the chunk sections in reach are checked for targets by
 * palette first, so magma seas cost almost nothing.
 *
 * <p>Ultra-warm dimensions: melted ice leaves air, and blocks in {@link MeltTags#ULTRAWARM_INSTANT} become air the
 * moment they are placed ({@link #onPlaced}). Vanilla light melting is cut by {@code mixin.VanillaLightMeltMixin}
 * unless {@code vanillaLightMelting} is on.
 */
public final class Melting {

	private static final Logger LOGGER = LogUtils.getLogger();

	/** Fire sources giving less light than this are source-driven (when they random tick). Light 4 reaches 3 blocks. */
	public static final int DIM_LIGHT = 4;
	/** Largest range the config allows (sphere offsets are precomputed to it). */
	private static final int MAX_RANGE = 8;

	private static final byte MELTABLE = 1, SLOW = 2, SALT = 4, FIRE = 8, LAVA = 16, DRIVEN = 32, INSTANT = 64;
	private static final Direction[] DIRECTIONS = Direction.values();
	/** {dx, dy, dz, d2} for every offset within MAX_RANGE except the center, nearest first. */
	private static final int[][] OFFSETS = buildOffsets();

	private static volatile Reference2ByteOpenHashMap<Block> flags = new Reference2ByteOpenHashMap<>();
	private static List<? extends String> stepsSource;
	private static Map<Block, BlockState> steps = Map.of();

	/**
	 * The [melting] config values the tick paths read, copied from the spec on config load and reload so a random
	 * tick reads plain fields instead of ForgeConfigSpec values (SQ-MELT-P1). Before the first load: melting and the
	 * ultra-warm melt off, vanilla light melting as vanilla, the same as the former isLoaded() checks.
	 */
	private record Values(boolean enabled, int saltRange, int fireRange, int lavaRange, int slowFireRange,
			int slowLavaRange, double meltChance, double slowMeltChance, boolean placeWater,
			boolean vanillaLightMelting, boolean ultraWarmInstantMelt) {
		static final Values UNLOADED = new Values(false, 0, 0, 0, 0, 0, 0, 0, true, true, false);

		static Values read() {
			CommonConfig.MeltingConfig c = CommonConfig.MELTING;
			return new Values(c.enabled.get(), c.saltRange.get(), c.fireRange.get(), c.lavaRange.get(),
					c.slowFireRange.get(), c.slowLavaRange.get(), c.meltChance.get(), c.slowMeltChance.get(),
					c.placeWater.get(), c.vanillaLightMelting.get(), c.ultraWarmInstantMelt.get());
		}
	}

	private static volatile Values cfg = Values.UNLOADED;

	/**
	 * Told after a salt source melted a target by its random tick (plan_post_scratch_1115 B3): Creative Delight removes a
	 * salt layer by chance. Not called for {@link #meltStep} (the caller spends its own salt).
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
		Reference2ByteOpenHashMap<Block> map = new Reference2ByteOpenHashMap<>();
		mark(map, tags, MeltTags.MELTABLE, MELTABLE);
		mark(map, tags, MeltTags.MELTABLE_SLOW, SLOW);
		mark(map, tags, MeltTags.SALT, SALT);
		mark(map, tags, MeltTags.FIRE, FIRE);
		mark(map, tags, MeltTags.LAVA, LAVA);
		mark(map, tags, MeltTags.ULTRAWARM_INSTANT, INSTANT);

		// Fire blocks that give little light: source-driven when they random tick, else only found by touching targets
		TreeSet<String> driven = new TreeSet<>(), dimStill = new TreeSet<>();
		int fire = 0;
		for (Block block : tags.getTag(MeltTags.FIRE)) {
			fire++;
			int light = 0;
			boolean ticks = false;
			for (BlockState s : block.getStateDefinition().getPossibleStates()) {
				if (!fireActive(s)) continue;
				light = Math.max(light, s.getLightEmission());
				ticks |= s.isRandomlyTicking();
			}
			if (light >= DIM_LIGHT) continue;
			String id = String.valueOf(ForgeRegistries.BLOCKS.getKey(block));
			if (ticks) {
				map.put(block, (byte) (map.getByte(block) | DRIVEN));
				driven.add(id + " (light " + light + ")");
			} else {
				dimStill.add(id + " (light " + light + ")");
			}
		}
		map.trim();
		flags = map;
		if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
			LOGGER.info("[melting] tags: {} meltable, {} slow, {} salt, {} fire, {} lava, {} ultra-warm instant",
					count(tags, MeltTags.MELTABLE), count(tags, MeltTags.MELTABLE_SLOW), count(tags, MeltTags.SALT),
					fire, count(tags, MeltTags.LAVA), count(tags, MeltTags.ULTRAWARM_INSTANT));
			LOGGER.info("[melting] source-driven fire (light below {}, random ticks): {}", DIM_LIGHT, driven);
			if (!dimStill.isEmpty())
				LOGGER.warn("[melting] fire tag blocks with light below {} that do not random tick (they melt only touching"
						+ " targets): {}", DIM_LIGHT, dimStill);
		}
	}

	private static void mark(Reference2ByteOpenHashMap<Block> map, ITagManager<Block> tags, TagKey<Block> key, byte bit) {
		for (Block block : tags.getTag(key)) map.put(block, (byte) (map.getByte(block) | bit));
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
		byte f = flags.getByte(state.getBlock());
		if (f == 0 || (f & (MELTABLE | SLOW | DRIVEN)) == 0) return false;
		if (!cfg.enabled()) return false;
		if ((f & (MELTABLE | SLOW)) != 0) return targetTick(state, f, level, pos, random);
		if (fireActive(state)) sourceTick(level, pos, random);
		return false;
	}

	/** BlockStateBase.onPlace, after the block's own: snow placed in an ultra-warm dimension becomes air. */
	public static void onPlaced(BlockState state, Level level, BlockPos pos) {
		if (level.isClientSide || !level.dimensionType().ultraWarm()) return;
		if ((flags.getByte(state.getBlock()) & INSTANT) == 0) return;
		if (!cfg.ultraWarmInstantMelt()) return;
		if (level.getBlockState(pos) != state) return; // already replaced by the block's own onPlace
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		level.levelEvent(1501, pos, 0); // lava fizz: hiss and smoke
	}

	/** The light vanilla IceBlock / SnowLayerBlock (and Rare Ice) melt by: 0 unless vanillaLightMelting is on. */
	public static int vanillaLight(ServerLevel level, LightLayer layer, BlockPos pos) {
		if (!cfg.vanillaLightMelting()) return 0;
		return level.getBrightness(layer, pos);
	}

	// ------------------------------------------------------------------ public API (salt dispenser, Salt Melt, other jars)

	/** Whether the state is a melting target (meltable or meltable_slow). */
	public static boolean isTarget(BlockState state) {
		return (flags.getByte(state.getBlock()) & (MELTABLE | SLOW)) != 0;
	}

	/**
	 * Melts one stage at once, no roll and no source needed: a meltable target melts fully, a meltable_slow one steps
	 * down one tier. Used by a salt dispenser and the Salt Melt (plan_post_scratch_1115 B2, B6); a salt click by hand
	 * places a salt layer instead (B1). False when the block is not a target.
	 */
	public static boolean meltStep(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		byte f = flags.getByte(state.getBlock());
		if ((f & (MELTABLE | SLOW)) == 0) return false;
		melt(level, pos, state, (f & MELTABLE) == 0);
		return true;
	}

	/** What the state becomes when it melts: a slow step, water for ice outside ultra-warm dimensions, else air. */
	public static BlockState meltedState(BlockState state, Level level, boolean slow) {
		if (slow) {
			BlockState step = steps().get(state.getBlock());
			if (step != null) return step;
		}
		if (!level.dimensionType().ultraWarm() && cfg.placeWater() && state.is(BlockTags.ICE))
			return Blocks.WATER.defaultBlockState();
		return Blocks.AIR.defaultBlockState();
	}

	// ------------------------------------------------------------------ rules

	private static boolean targetTick(BlockState state, byte f, ServerLevel level, BlockPos pos, RandomSource random) {
		Values c = cfg;
		boolean slow = (f & MELTABLE) == 0; // in both tags: the faster rule wins
		if (random.nextDouble() >= (slow ? c.slowMeltChance() : c.meltChance())) return false;
		int saltR = c.saltRange();
		int fireR = slow ? c.slowFireRange() : c.fireRange();
		int lavaR = slow ? c.slowLavaRange() : c.lavaRange();

		// 1. the 6 neighbors (loaded: they border a ticking chunk at most)
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (Direction d : DIRECTIONS) {
			m.setWithOffset(pos, d);
			BlockState s = level.getBlockState(m);
			if (isSource(s, saltR >= 1, fireR >= 1, lavaR >= 1)) {
				melt(level, pos, state, slow);
				afterMelt(level, m, s, saltR >= 1, random);
				return true;
			}
		}

		// 2. the sphere: salt only past 1 (it gives no light), fire and lava only where block light reaches
		int saltScan = saltR >= 2 ? saltR : 0;
		boolean lit = (fireR >= 2 || lavaR >= 2) && blockLit(level, pos, m);
		int r = Math.max(saltScan, lit ? Math.max(fireR, lavaR) : 0);
		if (r < 2) return false;
		int s2 = saltScan * saltScan, f2 = lit ? fireR * fireR : 0, l2 = lit ? lavaR * lavaR : 0, max2 = r * r;
		ChunkReader reader = new ChunkReader(level);
		for (int[] o : OFFSETS) {
			int d2 = o[3];
			if (d2 == 1) continue; // done above
			if (d2 > max2) break;
			m.set(pos.getX() + o[0], pos.getY() + o[1], pos.getZ() + o[2]);
			BlockState s = reader.get(m);
			if (s != null && isSource(s, d2 <= s2, d2 <= f2, d2 <= l2)) {
				melt(level, pos, state, slow);
				afterMelt(level, m, s, d2 <= s2, random);
				return true;
			}
		}
		return false;
	}

	/** A dim fire source's own random tick: every target in its reach rolls its own chance. */
	private static void sourceTick(ServerLevel level, BlockPos pos, RandomSource random) {
		Values c = cfg;
		int fireR = c.fireRange(), slowR = c.slowFireRange();
		int r = Math.max(fireR, slowR);
		if (r < 1) return;
		ChunkReader reader = new ChunkReader(level);
		if (!reader.mayHaveTargets(pos, r)) return;
		double chance = c.meltChance(), slowChance = c.slowMeltChance();
		int f2 = fireR * fireR, s2 = slowR * slowR, max2 = r * r;
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int[] o : OFFSETS) {
			int d2 = o[3];
			if (d2 > max2) break;
			m.set(pos.getX() + o[0], pos.getY() + o[1], pos.getZ() + o[2]);
			BlockState s = reader.get(m);
			if (s == null) continue;
			byte f = flags.getByte(s.getBlock());
			if ((f & (MELTABLE | SLOW)) == 0) continue;
			boolean slow = (f & MELTABLE) == 0;
			if (d2 > (slow ? s2 : f2)) continue;
			if (random.nextDouble() < (slow ? slowChance : chance)) melt(level, m.immutable(), s, slow);
		}
	}

	/** A salt source in salt range did the melt: tell the listener (B3). */
	private static void afterMelt(ServerLevel level, BlockPos source, BlockState s, boolean saltInRange, RandomSource random) {
		SaltMeltListener l = saltListener;
		if (l != null && saltInRange && (flags.getByte(s.getBlock()) & SALT) != 0)
			l.onSaltMelt(level, source.immutable(), s, random);
	}

	private static boolean isSource(BlockState s, boolean salt, boolean fire, boolean lava) {
		byte f = flags.getByte(s.getBlock());
		if (salt && (f & SALT) != 0) return true;
		if (fire && (f & FIRE) != 0 && (f & DRIVEN) == 0 && fireActive(s)) return true;
		if (lava) {
			if ((f & LAVA) != 0) return true;
			FluidState fluid = s.getFluidState();
			return !fluid.isEmpty() && fluid.is(FluidTags.LAVA);
		}
		return false;
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

	private static void melt(ServerLevel level, BlockPos pos, BlockState state, boolean slow) {
		level.setBlockAndUpdate(pos, meltedState(state, level, slow));
		level.levelEvent(2001, pos, Block.getId(state)); // the block's break particles and sound
	}

	private static Map<Block, BlockState> steps() {
		List<? extends String> src = CommonConfig.MELTING.slowSteps.get();
		if (src != stepsSource) {
			Map<Block, BlockState> map = new IdentityHashMap<>();
			for (String row : src) {
				int eq = row.indexOf('=');
				Block from = block(row.substring(0, eq)), to = block(row.substring(eq + 1));
				if (from != null && to != null) map.put(from, to.defaultBlockState());
			}
			steps = map;
			stepsSource = src;
		}
		return steps;
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
		private static final Predicate<BlockState> TARGET = s -> (flags.getByte(s.getBlock()) & (MELTABLE | SLOW)) != 0;
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

		/** Whether any loaded section in the cube of radius r around pos may hold a target (palette check). */
		boolean mayHaveTargets(BlockPos pos, int r) {
			int minY = Math.max(level.getMinBuildHeight(), pos.getY() - r);
			int maxY = Math.min(level.getMaxBuildHeight() - 1, pos.getY() + r);
			if (minY > maxY) return false;
			for (int x = (pos.getX() - r) >> 4; x <= (pos.getX() + r) >> 4; x++)
				for (int z = (pos.getZ() - r) >> 4; z <= (pos.getZ() + r) >> 4; z++) {
					LevelChunk c = chunkAt(x, z);
					if (c == null) continue;
					for (int i = c.getSectionIndex(minY), top = c.getSectionIndex(maxY); i <= top; i++) {
						LevelChunkSection section = c.getSection(i);
						if (!section.hasOnlyAir() && section.maybeHas(TARGET)) return true;
					}
				}
			return false;
		}
	}
}
