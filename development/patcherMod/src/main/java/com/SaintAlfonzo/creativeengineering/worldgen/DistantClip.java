package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Distant Horizons' own world generation without the bottom of the world (feature_writeup/locate.md 3.7, decision 4).
 * DH (distantGeneratorMode FEATURES) runs vanilla's biome, noise, surface and feature steps on throwaway ProtoChunks,
 * passing its own WorldGenRegion subclass (DhLitWorldGenRegion); chunks players use never come through here.
 *
 *  - The chunk is marked when its noise sampler is created (biome step) or its noise is filled, if the StructureManager
 *    reads a DH region.
 *  - For a marked chunk the noise settings (NoiseSettings#clampToHeightAccessor, in fillFromNoise and
 *    NoiseChunk#forChunk) start at clipBelowY instead of the world bottom: no density, aquifer or ore-vein work below it.
 *  - After the noise fill, the band below is filled: a column whose block at clipBelowY is water in an ocean biome
 *    keeps water down to a fake sand (ocean) or gravel (deep ocean) floor that rolls around oceanFloorY /
 *    deepOceanFloorY by floorVariation (smooth noise seeded from the world seed, rolls about floorScale wide);
 *    everything else is stone above y 0 and deepslate below.
 *  - Features whose origin is below clipBelowY are skipped (skipFeatures).
 *
 * CEConfig [worldgen.distant]. With DH's default opaque water (transparency DISABLED) the fake seabed isn't drawn at all.
 */
public final class DistantClip {

	private static final ClassValue<Boolean> IS_DH_REGION = new ClassValue<>() {
		@Override
		protected Boolean computeValue(Class<?> type) {
			for (Class<?> c = type; c != null; c = c.getSuperclass()) {
				if (c.getName().contains("DhLitWorldGenRegion")) return true;
			}
			return false;
		}
	};

	/** Marked chunks and their clip height. */
	private static final Map<ChunkAccess, Integer> CLIPPED = Collections.synchronizedMap(new WeakHashMap<>());

	private static final ResourceLocation FLOOR_RANDOM = new ResourceLocation("creativeengineering", "distant_seabed");

	private DistantClip() {}

	public static boolean isDistantLevel(Object level) {
		return level != null && IS_DH_REGION.get(level.getClass());
	}

	/** createNoiseChunk / fillFromNoise HEAD: mark a DH chunk. */
	public static void mark(ChunkAccess chunk, StructureManager manager) {
		if (!WorldgenConfig.distantClip() || manager == null) return;
		if (!isDistantLevel(LocateReflect.level(manager))) return;
		int clip = Math.floorDiv(WorldgenConfig.distantClipBelowY(), 16) * 16;
		if (clip <= chunk.getMinBuildHeight() || clip >= chunk.getMaxBuildHeight()) return;
		CLIPPED.putIfAbsent(chunk, clip);
	}

	/** NoiseSettings#clampToHeightAccessor in fillFromNoise and NoiseChunk#forChunk: start a marked chunk's noise at the clip. */
	public static NoiseSettings clamp(NoiseSettings clamped, LevelHeightAccessor accessor) {
		Integer clip = accessor instanceof ChunkAccess chunk ? CLIPPED.get(chunk) : null;
		if (clip == null) return clamped;
		int top = clamped.minY() + clamped.height();
		if (clip <= clamped.minY() || clip >= top) return clamped;
		return new NoiseSettings(clip, top - clip, clamped.noiseSizeHorizontal(), clamped.noiseSizeVertical());
	}

	/** ConfiguredFeature#place HEAD: skip a feature placed below the clip in DH's generation. */
	public static boolean skipFeature(Object level, BlockPos origin) {
		if (!WorldgenConfig.distantClip() || !WorldgenConfig.distantSkipFeatures() || !isDistantLevel(level)) return false;
		return origin.getY() < Math.floorDiv(WorldgenConfig.distantClipBelowY(), 16) * 16;
	}

	/** doFill RETURN: fill the band below the clip of a marked chunk. */
	public static void fillBand(ChunkAccess chunk, RandomState randomState) {
		Integer clip = CLIPPED.get(chunk);
		if (clip == null) return;
		int minY = chunk.getMinBuildHeight();
		ChunkPos cp = chunk.getPos();
		PositionalRandomFactory random = randomState.getOrCreateRandomFactory(FLOOR_RANDOM);
		int scale = Math.max(4, WorldgenConfig.distantFloorScale());
		int variation = WorldgenConfig.distantFloorVariation();
		int oceanFloor = WorldgenConfig.distantOceanFloorY();
		int deepFloor = WorldgenConfig.distantDeepOceanFloorY();
		BlockState stone = Blocks.STONE.defaultBlockState(), deepslate = Blocks.DEEPSLATE.defaultBlockState();
		BlockState water = Blocks.WATER.defaultBlockState(), sand = Blocks.SAND.defaultBlockState(), gravel = Blocks.GRAVEL.defaultBlockState();
		Heightmap oceanFloorMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap surfaceMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int lx = 0; lx < 16; lx++) {
			for (int lz = 0; lz < 16; lz++) {
				int x = cp.getMinBlockX() + lx, z = cp.getMinBlockZ() + lz;
				BlockState above = chunk.getBlockState(pos.set(x, clip, z));
				int floor = minY - 1;   // no water: solid up to the clip
				BlockState top = null;
				if (above.getFluidState().is(FluidTags.WATER)) {
					Holder<Biome> biome = chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(clip), QuartPos.fromBlock(z));
					if (biome.is(BiomeTags.IS_DEEP_OCEAN)) {
						floor = deepFloor;
						top = gravel;
					} else if (biome.is(BiomeTags.IS_OCEAN)) {
						floor = oceanFloor;
						top = sand;
					}
					if (top != null) {
						floor += Math.round((float) (roll(random, x, z, scale) * variation));
						floor = Mth.clamp(floor, minY, clip - 1);
					}
				}
				for (int y = minY; y < clip; y++) {
					BlockState state;
					if (top == null || y < floor) state = y < 0 ? deepslate : stone;
					else if (y == floor) state = top;
					else state = water;
					LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
					section.setBlockState(lx, y & 15, lz, state, false);
				}
				int solidTop = top == null ? clip - 1 : floor;
				oceanFloorMap.update(lx, solidTop, lz, top == null ? stone : top);
				surfaceMap.update(lx, clip - 1, lz, top == null ? stone : water);
			}
		}
	}

	/** Smooth value noise in [-1, 1]: a random value per lattice point every {@code scale} blocks, smoothstep-blended. */
	static double roll(PositionalRandomFactory random, int x, int z, int scale) {
		int gx = Math.floorDiv(x, scale), gz = Math.floorDiv(z, scale);
		double fx = smooth((x - gx * (double) scale) / scale), fz = smooth((z - gz * (double) scale) / scale);
		double a = lattice(random, gx, gz), b = lattice(random, gx + 1, gz), c = lattice(random, gx, gz + 1), d = lattice(random, gx + 1, gz + 1);
		return Mth.lerp(fz, Mth.lerp(fx, a, b), Mth.lerp(fx, c, d));
	}

	private static double lattice(PositionalRandomFactory random, int gx, int gz) {
		RandomSource r = random.at(gx, 0, gz);
		return r.nextDouble() * 2.0 - 1.0;
	}

	private static double smooth(double t) {
		return t * t * (3.0 - 2.0 * t);
	}
}
