package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Geodes under oceans generate lower, so fewer break through the sea floor and leave amethyst or prismarine shells
 * sticking out of the seabed. Covers vanilla's GeodeFeature: the amethyst geode and every Geode Plus geode (all are
 * built on it; Geode Plus' own OceanGeodeFeature is registered but unused). mixin/GeodeFeatureOceanDepthMixin.
 * Geodes whose inner layer is in [worldgen.ocean_geodes] untouched (Geode Plus' prismarine geode) keep their height.
 *
 * A geode counts as under an ocean when the biome at the sea floor of its column (OCEAN_FLOOR_WG) is in
 * #minecraft:is_ocean. Such a geode is moved down until its top (origin + its config's maxGenOffset, the bound of
 * GeodeFeature's placement box) is {@code clearance} blocks under {@code lowestFloorY}, the deepest sea floor the pack
 * generates, then up to {@code spread} blocks further so moved geodes don't share one layer. Geodes already that deep
 * stay. A share {@code stayShare} keeps its height regardless. Both rolls hash the world seed and the geode's position,
 * so they don't consume the feature's random and are the same every time. CWGConfig [worldgen.ocean_geodes], switch
 * [worldgen.systems] oceanGeodes.
 *
 * A geode that stays and still reaches the sea floor is patched after it places (floorPatch, TODO structures/terrain
 * #6). SWAP: its outer and middle layer from the old floor up become the floor block that column had before, so it
 * stays a mound of sea floor with the opening into its crystals. CUT: its blocks above the old floor become water and
 * the rim left on the surface becomes the floor block. The untouched geodes are left as they are.
 */
public final class OceanGeodes {

	private static final int SALT = "ocean-geodes".hashCode();
	private static final int SPREAD_SALT = "ocean-geodes-spread".hashCode();
	/** Set while the moved geode places, so its own place() call doesn't move it again. */
	private static final ThreadLocal<Boolean> PLACING = new ThreadLocal<>();

	/** The sea floor under a geode about to place (floorPatch), from {@link #beforePlace} to {@link #afterPlace}. */
	private static final ThreadLocal<Floor> FLOOR = new ThreadLocal<>();

	private OceanGeodes() {}

	/**
	 * Feature#place HEAD for a geode: the result of placing it lower, or null to go on as usual (not under an ocean,
	 * a geode that stays, the system off, or the moved geode's own call).
	 */
	public static Boolean placeLowered(FeaturePlaceContext<GeodeConfiguration> ctx, Predicate<FeaturePlaceContext<GeodeConfiguration>> place) {
		if (PLACING.get() != null || !WorldgenConfig.oceanGeodesEnabled() || untouched(ctx.config())) return null;
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		int reach = ctx.config().maxGenOffset;
		int highest = WorldgenConfig.oceanGeodeLowestFloorY() - WorldgenConfig.oceanGeodeClearance() - reach;
		if (origin.getY() <= highest) return null;
		int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
		if (!level.getBiome(new BlockPos(origin.getX(), floor, origin.getZ())).is(BiomeTags.IS_OCEAN)) return null;
		if (StructureFrequencies.roll(level.getSeed(), origin.getX(), origin.getZ(), SALT + origin.getY()) < WorldgenConfig.oceanGeodeStayShare()) return null;
		int spread = (int) (StructureFrequencies.roll(level.getSeed(), origin.getX(), origin.getZ(), SPREAD_SALT + origin.getY())
				* (WorldgenConfig.oceanGeodeSpread() + 1));
		BlockPos lowered = origin.atY(highest - spread);
		if (lowered.getY() - reach <= level.getMinBuildHeight()) return null;
		PLACING.set(Boolean.TRUE);
		try {
			return place.test(new FeaturePlaceContext<>(ctx.topFeature(), level, ctx.chunkGenerator(), ctx.random(), lowered, ctx.config()));
		} finally {
			PLACING.remove();
		}
	}

	// ── floorPatch ───────────────────────────────────────────────────────────

	/**
	 * The floor before the geode: per column of its box (x-major) the top solid y and its block (null: no floor in the
	 * box), and whether the shell is swapped (SWAP) or cut (CUT).
	 */
	record Floor(int minX, int minZ, int sizeX, int sizeZ, int minY, int maxY, int[] floorY, BlockState[] floor, Set<Block> shell, Set<Block> inner,
			boolean swap) {}

	/**
	 * Feature#place HEAD when {@link #placeLowered} left the geode where it is: notes the sea floor in its box when it
	 * is an ocean geode that is not untouched and the floor lies inside the box.
	 */
	public static void beforePlace(FeaturePlaceContext<GeodeConfiguration> ctx) {
		FLOOR.remove();
		CWGConfig.GeodeFloor mode = WorldgenConfig.oceanGeodeFloorPatch();
		if (PLACING.get() != null || !WorldgenConfig.oceanGeodesEnabled() || mode == CWGConfig.GeodeFloor.OFF) return;
		GeodeConfiguration config = ctx.config();
		if (untouched(config)) return;
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		int min = config.minGenOffset, max = config.maxGenOffset;
		int minY = origin.getY() + min, maxY = origin.getY() + max;
		int floorAt = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ()) - 1;
		if (floorAt - (max - min) > maxY || !level.getBiome(new BlockPos(origin.getX(), floorAt, origin.getZ())).is(BiomeTags.IS_OCEAN)) return;
		int size = max - min + 1;
		int[] floorY = new int[size * size];
		BlockState[] floor = new BlockState[size * size];
		boolean any = false;
		for (int dx = 0; dx < size; dx++) {
			for (int dz = 0; dz < size; dz++) {
				int x = origin.getX() + min + dx, z = origin.getZ() + min + dz;
				int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
				floorY[dx * size + dz] = y;
				if (y < minY || y > maxY) continue;
				floor[dx * size + dz] = level.getBlockState(new BlockPos(x, y, z));
				any = true;
			}
		}
		if (!any) return;
		Set<Block> shell = new HashSet<>(), inner = new HashSet<>();
		var b = config.geodeBlockSettings;
		for (int i = 0; i < 16; i++) {
			RandomSource r = RandomSource.create(i);
			shell.add(b.outerLayerProvider.getState(r, BlockPos.ZERO).getBlock());
			shell.add(b.middleLayerProvider.getState(r, BlockPos.ZERO).getBlock());
			inner.add(b.innerLayerProvider.getState(r, BlockPos.ZERO).getBlock());
			inner.add(b.alternateInnerLayerProvider.getState(r, BlockPos.ZERO).getBlock());
			inner.add(b.fillingProvider.getState(r, BlockPos.ZERO).getBlock());
		}
		for (BlockState s : b.innerPlacements) inner.add(s.getBlock());
		inner.remove(Blocks.AIR);
		shell.remove(Blocks.AIR);
		FLOOR.set(new Floor(origin.getX() + min, origin.getZ() + min, size, size, minY, maxY, floorY, floor, shell, inner, mode == CWGConfig.GeodeFloor.SWAP));
	}

	/** Feature#place RETURN: patches the floor noted by {@link #beforePlace} once the geode has placed. */
	public static void afterPlace(FeaturePlaceContext<GeodeConfiguration> ctx, boolean placed) {
		Floor f = FLOOR.get();
		FLOOR.remove();
		if (f == null || !placed) return;
		WorldGenLevel level = ctx.level();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Blocks3 world = new Blocks3() {
			public BlockState get(int x, int y, int z) { return level.getBlockState(pos.set(x, y, z)); }
			public void set(int x, int y, int z, BlockState state) { level.setBlock(pos.set(x, y, z), state, 2); }
		};
		patch(world, f, level.getSeaLevel());
	}

	/** Block access for the level-free core. */
	interface Blocks3 {
		BlockState get(int x, int y, int z);
		void set(int x, int y, int z, BlockState state);
	}

	/** The level-free core: patches every column with a noted floor; returns the blocks changed. */
	static int patch(Blocks3 world, Floor f, int seaLevel) {
		BlockState fallback = commonFloor(f);
		int changed = 0;
		for (int dx = 0; dx < f.sizeX(); dx++) {
			for (int dz = 0; dz < f.sizeZ(); dz++) {
				int i = dx * f.sizeZ() + dz;
				BlockState floor = f.floor()[i];
				if (floor == null) continue;
				if (!usable(floor, f)) floor = fallback;
				changed += patchColumn(world, f.minX() + dx, f.minZ() + dz, f.maxY(), f.minY(), f.floorY()[i], floor, seaLevel, f.shell(), f.inner(), f.swap());
			}
		}
		return changed;
	}

	/**
	 * One column, from the box top down. Above the old floor, CUT turns the geode's blocks into water (air over the sea
	 * level) and SWAP turns its shell blocks into the floor block and stops at its inner layer (the opening); air under
	 * water becomes water (an opened cavity) all the way down; at or under the old floor the first shell block becomes
	 * the floor block. Stops at the first other block (natural ground, or the inner layer). Returns the blocks changed.
	 */
	static int patchColumn(Blocks3 world, int x, int z, int top, int bottom, int oldFloor, BlockState floor, int seaLevel, Set<Block> shell, Set<Block> inner,
			boolean swap) {
		int changed = 0;
		for (int y = top; y >= bottom; y--) {
			BlockState s = world.get(x, y, z);
			if (s.isAir()) {
				if (y < seaLevel && isWater(world.get(x, y + 1, z))) { world.set(x, y, z, Blocks.WATER.defaultBlockState()); changed++; }
				continue;
			}
			if (isWater(s) && !shell.contains(s.getBlock()) && !inner.contains(s.getBlock())) continue;
			boolean isShell = shell.contains(s.getBlock());
			if (y > oldFloor) {
				if (!isShell && !inner.contains(s.getBlock())) return changed;   // something on the floor that isn't the geode
				if (swap && !isShell) return changed;   // the opening into the crystals
				world.set(x, y, z, swap ? floor : (y < seaLevel ? Blocks.WATER : Blocks.AIR).defaultBlockState());
				changed++;
				continue;
			}
			if (isShell && !s.equals(floor)) { world.set(x, y, z, floor); changed++; }
			return changed;
		}
		return changed;
	}

	private static boolean isWater(BlockState s) { return s.getFluidState().getType().isSame(net.minecraft.world.level.material.Fluids.WATER); }

	/** A floor block that can stand in: not air or water, not one of this geode's own blocks (an earlier geode). */
	private static boolean usable(BlockState s, Floor f) {
		return !s.isAir() && s.getFluidState().isEmpty() && !f.shell().contains(s.getBlock()) && !f.inner().contains(s.getBlock());
	}

	/** The most common usable floor block of the box, gravel when none. */
	private static BlockState commonFloor(Floor f) {
		Map<BlockState, Integer> counts = new HashMap<>();
		for (BlockState s : f.floor()) if (s != null && usable(s, f)) counts.merge(s, 1, Integer::sum);
		return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(Blocks.GRAVEL.defaultBlockState());
	}

	/**
	 * The geode's inner layer or alternate inner layer block is on the untouched list. Geode Plus' prismarine
	 * geode has plain prismarine inside and its budding prismarine in the alternate layer (amethyst's pattern).
	 */
	private static boolean untouched(GeodeConfiguration config) {
		java.util.List<? extends String> ids = WorldgenConfig.oceanGeodeUntouched();
		if (ids.isEmpty()) return false;
		return listed(ids, config.geodeBlockSettings.innerLayerProvider.getState(RandomSource.create(0), BlockPos.ZERO))
				|| listed(ids, config.geodeBlockSettings.alternateInnerLayerProvider.getState(RandomSource.create(0), BlockPos.ZERO));
	}

	private static boolean listed(java.util.List<? extends String> ids, BlockState state) {
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		return id != null && ids.contains(id.toString());
	}
}
