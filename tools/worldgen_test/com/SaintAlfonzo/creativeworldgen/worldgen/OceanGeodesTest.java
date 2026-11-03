package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Headless checks of OceanGeodes' floorPatch core (TODO structures/terrain #6): columns of a geode that reaches the
 * sea floor at y 40 (sea level 63), patched against a map world, CUT then SWAP.
 */
public class OceanGeodesTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static final Map<Long, BlockState> WORLD = new HashMap<>();
	static final int SEA = 63;

	static long key(int x, int y, int z) { return net.minecraft.core.BlockPos.asLong(x, y, z); }

	static final OceanGeodes.Blocks3 MAP = new OceanGeodes.Blocks3() {
		public BlockState get(int x, int y, int z) {
			BlockState s = WORLD.get(key(x, y, z));
			return s != null ? s : y < SEA ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
		}
		public void set(int x, int y, int z, BlockState state) { WORLD.put(key(x, y, z), state); }
	};

	/** A column from y 30 up: stone below 33, then the given blocks from y 33 (null = water / air by sea level). */
	static void column(int x, Block... fromY33) {
		for (int y = 30; y < 33; y++) WORLD.put(key(x, y, 0), Blocks.STONE.defaultBlockState());
		for (int i = 0; i < fromY33.length; i++) if (fromY33[i] != null) WORLD.put(key(x, 33 + i, 0), fromY33[i].defaultBlockState());
	}

	static Block at(int x, int y) { return MAP.get(x, y, 0).getBlock(); }

	public static void main(String[] args) {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		Block basalt = Blocks.SMOOTH_BASALT, calcite = Blocks.CALCITE, amethyst = Blocks.AMETHYST_BLOCK, air = Blocks.AIR, sand = Blocks.SAND;
		Set<Block> shell = Set.of(basalt, calcite), inner = Set.of(amethyst, Blocks.BUDDING_AMETHYST, Blocks.AMETHYST_CLUSTER);

		// x 0: shell sticking 2 above the floor (y 41, 42), calcite at the floor
		column(0, basalt, calcite, amethyst, air, air, air, amethyst, calcite, basalt, basalt);
		// x 1: the inner layer at the floor, shell above it
		column(1, basalt, calcite, amethyst, air, air, air, air, amethyst, calcite, basalt);
		// x 2: the cavity open at the floor (air under water at y 39, 40)
		column(2, basalt, calcite, amethyst, amethyst, amethyst, amethyst, air, air);
		// x 3: plain sand floor next to the geode
		column(3, sand, sand, sand, sand, sand, sand, sand, sand);
		// x 4: kelp on a basalt floor block (an earlier geode's): the noted floor is unusable, so it takes the box's common floor
		column(4, sand, sand, sand, sand, sand, sand, sand, basalt, Blocks.KELP);
		int[] floorY = {40, 40, 40, 40, 40};
		BlockState s = sand.defaultBlockState();
		BlockState[] floor = {s, s, s, s, basalt.defaultBlockState()};
		OceanGeodes.Floor f = new OceanGeodes.Floor(0, 0, 5, 1, 30, 46, floorY, floor, shell, inner, false);
		int changed = OceanGeodes.patch(MAP, f, SEA);

		check(at(0, 42) == Blocks.WATER && at(0, 41) == Blocks.WATER, "shell above the floor becomes water");
		check(at(0, 40) == sand && at(0, 39) == amethyst, "shell at the floor becomes the column's old floor block; below it stays");
		check(at(1, 42) == Blocks.WATER && at(1, 41) == Blocks.WATER && at(1, 40) == amethyst, "the inner layer at the floor stays (the opening)");
		check(at(2, 40) == Blocks.WATER && at(2, 39) == Blocks.WATER && at(2, 38) == amethyst, "an open cavity under water fills with water down to the inner layer");
		check(at(3, 40) == sand, "plain floor untouched");
		check(at(4, 40) == sand && at(4, 41) == Blocks.KELP, "kelp passed over; the basalt floor becomes the common floor, sand");
		check(changed == 8, "8 blocks changed (got " + changed + ")");

		// over the sea level the protruding shell becomes air
		WORLD.clear();
		column(0, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand, sand,
				sand, sand, sand, sand, sand, sand, sand, calcite, basalt);   // floor y 64 (sand at 33..63), calcite 64, basalt 65
		OceanGeodes.Floor dry = new OceanGeodes.Floor(0, 0, 1, 1, 50, 70, new int[]{63}, new BlockState[]{s}, shell, inner, false);
		OceanGeodes.patch(MAP, dry, SEA);
		check(at(0, 65) == air && at(0, 64) == air && at(0, 63) == sand, "above the sea level the geode's blocks become air");

		// SWAP: the same geode keeps its shape, its shell from the floor up turns to the floor block
		WORLD.clear();
		column(0, basalt, calcite, amethyst, air, air, air, amethyst, calcite, basalt, basalt);   // shell up to y 42
		column(1, basalt, calcite, amethyst, air, air, air, air, amethyst, calcite, basalt);      // inner at the floor, shell above
		column(2, basalt, calcite, amethyst, amethyst, amethyst, amethyst, air, air);             // open cavity at 39, 40
		OceanGeodes.Floor sw = new OceanGeodes.Floor(0, 0, 3, 1, 30, 46, new int[]{40, 40, 40}, new BlockState[]{s, s, s}, shell, inner, true);
		changed = OceanGeodes.patch(MAP, sw, SEA);
		check(at(0, 42) == sand && at(0, 41) == sand && at(0, 40) == sand && at(0, 39) == amethyst,
				"swap: the shell sticking up and the rim become sand, the column keeps its height");
		check(at(1, 42) == sand && at(1, 41) == sand && at(1, 40) == amethyst, "swap: the inner layer at the floor stays (the opening)");
		check(at(2, 40) == Blocks.WATER && at(2, 39) == Blocks.WATER && at(2, 38) == amethyst, "swap: an open cavity under water still fills with water");
		check(changed == 7, "swap: 7 blocks changed (got " + changed + ")");
		WORLD.clear();
		column(0, sand, sand, sand, sand, sand, sand, sand, amethyst, basalt);   // crystals above the floor with shell on top
		OceanGeodes.patch(MAP, new OceanGeodes.Floor(0, 0, 1, 1, 30, 46, new int[]{39}, new BlockState[]{s}, shell, inner, true), SEA);
		check(at(0, 41) == sand && at(0, 40) == amethyst, "swap: stops at the inner layer above the floor (crystals left showing)");

		System.out.println(fails == 0 ? "ALL " + checks + " PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
