package com.SaintAlfonzo.creativecompat.surface;

import static com.SaintAlfonzo.creativecompat.surface.PassThroughTest.check;
import static com.SaintAlfonzo.creativecompat.surface.PassThroughTest.near;

import java.util.Random;

import com.SaintAlfonzo.creativecompat.surface.PassThroughTest.World;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Scratch fixes (scratch run r1115 phase 11): SQ-PTH-03 (vanilla's fast-fall reset clip on webs), SQ-PTH-11 (break
 * projectiles stalled in any web), the 7.2 performance fallback (region counts, per-tick count cache). Called from
 * {@link PassThroughTest#main}. Headless tags are unbound, so "is a fall-resetting block" is passed in as vanilla's
 * {@code #minecraft:fall_damage_resetting} would answer it (cobweb, sweet berry bush).
 */
final class PassFixesTest {

	private PassFixesTest() {}

	static void run() {
		fallResetClip();
		breakStall();
		regionCounts();
		countCache();
		leafExemptPathType();
	}

	/**
	 * creative-compat-publish #19: a deep leaf pile is DAMAGE_CACTUS for foxes and bees, not BLOCKED (vanilla's
	 * getBlockPathTypeStatic turns the open cell above a BLOCKED cell WALKABLE; DAMAGE_CACTUS carries up, malus -1).
	 */
	static void leafExemptPathType() {
		var exempt = LeafThorns.pathType(true);
		var other = LeafThorns.pathType(false);
		PassThroughTest.check("leaf path: exempt mobs get DAMAGE_CACTUS", exempt == net.minecraft.world.level.pathfinder.BlockPathTypes.DAMAGE_CACTUS);
		PassThroughTest.check("leaf path: exempt type is not BLOCKED", exempt != net.minecraft.world.level.pathfinder.BlockPathTypes.BLOCKED);
		PassThroughTest.check("leaf path: exempt type has malus -1", exempt.getMalus() < 0.0F);
		PassThroughTest.check("leaf path: other mobs keep DAMAGE_OTHER", other == net.minecraft.world.level.pathfinder.BlockPathTypes.DAMAGE_OTHER);
	}

	static boolean resets(BlockState s) {
		return s.is(Blocks.COBWEB) || s.is(Blocks.SWEET_BERRY_BUSH) || s.is(Blocks.LADDER);
	}

	/** A falling entity's per-tick path (center column x 0.5, z 0.5), as Entity#move clips it. */
	static boolean vanillaHit(World w, double fromY, double toY) {
		BlockHitResult r = PassThrough.clipFallReset(w, new Vec3(0.5, fromY, 0.5), new Vec3(0.5, toY, 0.5),
				PassFixesTest::resets, (p, s) -> false);
		return r.getType() != HitResult.Type.MISS;
	}

	static boolean fixedHit(World w, double fromY, double toY) {
		PassValues values = PassValues.defaults();
		BlockHitResult r = PassThrough.clipFallReset(w, new Vec3(0.5, fromY, 0.5), new Vec3(0.5, toY, 0.5),
				PassFixesTest::resets, (p, s) -> PassThrough.ownsFallReset(w, p, s, values));
		return r.getType() != HitResult.Type.MISS;
	}

	static void fallResetClip() {
		PassThrough.seedTier(Blocks.COBWEB, PassTier.COBWEB);
		PassThrough.seedTier(Blocks.SWEET_BERRY_BUSH, PassTier.BUSH);
		BlockState web = Blocks.COBWEB.defaultBlockState();
		// SQ-PTH-03 repro: a 1.5 b/t fall step through a lone web; vanilla's clip hits it and resets the fall distance
		World lone = new World().put(0, 10, 0, web);
		check("SQ-PTH-03 repro: vanilla fast-fall clip hits a lone web (resets fall distance)", vanillaHit(lone, 11.2, 9.7));
		check("fixed: a lone web is left out of the clip (fall distance x0.85 instead)", !fixedHit(lone, 11.2, 9.7));
		World stack = new World().put(0, 10, 0, web).put(0, 9, 0, web).put(0, 8, 0, web);
		check("fixed: 3 stacked webs (2 neighbors each) left out", !fixedHit(stack, 11.2, 7.5));
		World cube = new World();
		for (int x = -1; x <= 1; x++) for (int y = 9; y <= 11; y++) for (int z = -1; z <= 1; z++) cube.put(x, y, z, web);
		check("fixed: a dense web (cube) still resets (denseResetsFall)", fixedHit(cube, 12.4, 10.2));
		World bush = new World().put(0, 10, 0, Blocks.SWEET_BERRY_BUSH.defaultBlockState());
		check("bush ruling repro: vanilla fast-fall clip hits a sweet berry bush", vanillaHit(bush, 11.2, 9.7));
		check("fixed: a sweet berry bush (bush tier) is left out: no fall reset (owner ruling 2026-11-15)", !fixedHit(bush, 11.2, 9.7));
		World both = new World().put(0, 11, 0, web).put(0, 10, 0, Blocks.SWEET_BERRY_BUSH.defaultBlockState())
				.put(0, 9, 0, Blocks.LADDER.defaultBlockState());
		check("fixed: an untiered reset block (ladder) behind a web and a bush is still found", fixedHit(both, 12.2, 9.2));
		check("no block on the path: miss both ways", !vanillaHit(new World(), 11.2, 9.7) && !fixedHit(new World(), 11.2, 9.7));
		// what the scratch zombie should take: fall distance 14.55 entering a lone web -> x0.85 once
		PassRule.Cell c = PassRule.cell(PassTier.COBWEB, PassTier.COBWEB.defaults(), 0);
		PassState st = new PassState();
		st.result.reset();
		st.result.add(BlockPos.asLong(0, 10, 0), c, true, true);
		double f = PassThrough.newCellsFallFactor(st, st.result);
		check("lone web: fall distance 14.55 -> 12.37 (x0.85), not 0", near(14.55 * f, 12.3675) && !c.resetFall());
	}

	static void breakStall() {
		check("stall threshold 0.05 b/t", PassProjectiles.stalled(0.049 * 0.049) && !PassProjectiles.stalled(0.051 * 0.051));
		// SQ-PTH-11: snowball in an edge web cell, x0.35 a tick: 1.44 -> 0.50 -> 0.18 -> 0.062 -> 0.022: breaks on tick 4
		double v = 1.44;
		int tick = 0;
		while (tick < 20) {
			tick++;
			v *= 0.35;
			if (PassProjectiles.stalled(v * v)) break;
		}
		check("SQ-PTH-11: snowball stalling in an edge web breaks on its 4th web tick (" + tick + ")", tick == 4);
		check("a projectile hanging at 0.002 b/t counts as stalled", PassProjectiles.stalled(0.002 * 0.002));
	}

	/** Counts block reads, to show the region count reads fewer blocks than per-cell counts. */
	static final class Counting extends World {
		int reads;

		@Override
		public BlockState getBlockState(BlockPos pos) {
			reads++;
			return super.getBlockState(pos);
		}
	}

	static void regionCounts() {
		PassThrough.seedTier(Blocks.WHEAT, PassTier.DENSE_CROP);
		PassThrough.seedTier(Blocks.COBWEB, PassTier.COBWEB);
		PassThrough.seedTier(Blocks.STONE, null);
		BlockState[] pal = {Blocks.WHEAT.defaultBlockState(), Blocks.COBWEB.defaultBlockState(),
				Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()};
		Random rnd = new Random(1115);
		int bad = 0, cases = 0;
		for (int trial = 0; trial < 400; trial++) {
			World w = new World();
			for (int x = 0; x < 6; x++) for (int y = 0; y < 6; y++) for (int z = 0; z < 6; z++) {
				w.put(x, y, z, pal[rnd.nextInt(pal.length)]);
			}
			int n = 2 + rnd.nextInt(5);
			long[] pos = new long[n];
			PassTier[] tier = new PassTier[n];
			PassTier.Neighbours[] mode = new PassTier.Neighbours[n];
			int[] count = new int[n];
			for (int i = 0; i < n; i++) {
				pos[i] = BlockPos.asLong(1 + rnd.nextInt(4), 1 + rnd.nextInt(4), 1 + rnd.nextInt(4));
				boolean webCell = rnd.nextBoolean();
				tier[i] = webCell ? PassTier.COBWEB : PassTier.DENSE_CROP;
				mode[i] = webCell ? PassTier.Neighbours.FACES : PassTier.Neighbours.SIDES;
				count[i] = rnd.nextInt(4) == 0 ? 0 : -1; // some already known (cached)
			}
			int[] known = count.clone();
			PassThrough.countRegion(w, pos, tier, mode, count, n);
			for (int i = 0; i < n; i++) {
				cases++;
				int want = known[i] >= 0 ? known[i] : PassThrough.neighbours(w, BlockPos.of(pos[i]), tier[i], mode[i]);
				if (count[i] != want) bad++;
			}
		}
		check("region count = per-cell count (" + cases + " random cells, sides and faces)", bad == 0);

		// a zombie two cells tall in the middle of a 5x5x5 web cube: one 3x4x3 read (36), counts 6 / 6 (faces)
		Counting cube = new Counting();
		for (int x = 0; x < 5; x++) for (int y = 0; y < 5; y++) for (int z = 0; z < 5; z++) cube.put(x, y, z, pal[1]);
		long[] pos = {BlockPos.asLong(2, 2, 2), BlockPos.asLong(2, 3, 2)};
		int[] count = {-1, -1};
		PassThrough.countRegion(cube, pos, new PassTier[]{PassTier.COBWEB, PassTier.COBWEB},
				new PassTier.Neighbours[]{PassTier.Neighbours.FACES, PassTier.Neighbours.FACES}, count, 2);
		check("web cube, 2-cell zombie: 36 block reads, counts 6 / 6 (" + cube.reads + ")",
				cube.reads == 36 && count[0] == 6 && count[1] == 6);
		Counting field = new Counting();
		for (int x = 0; x < 6; x++) for (int z = 0; z < 6; z++) field.put(x, 0, z, pal[0]);
		long[] four = {BlockPos.asLong(2, 0, 2), BlockPos.asLong(3, 0, 2), BlockPos.asLong(2, 0, 3), BlockPos.asLong(3, 0, 3)};
		int[] c4 = {-1, -1, -1, -1};
		PassThrough.countRegion(field, four, new PassTier[]{PassTier.DENSE_CROP, PassTier.DENSE_CROP, PassTier.DENSE_CROP,
				PassTier.DENSE_CROP}, new PassTier.Neighbours[]{PassTier.Neighbours.SIDES, PassTier.Neighbours.SIDES,
				PassTier.Neighbours.SIDES, PassTier.Neighbours.SIDES}, c4, 4);
		check("field, horse over 2x2 cells: 16 block reads, counts 4 each (" + field.reads + ")",
				field.reads == 16 && c4[0] == 4 && c4[3] == 4);
	}

	static void countCache() {
		PassCountCache cache = new PassCountCache();
		Object lvl = new Object();
		int webTag = PassCountCache.tag(PassTier.COBWEB, PassTier.Neighbours.FACES);
		int cropTag = PassCountCache.tag(PassTier.DENSE_CROP, PassTier.Neighbours.SIDES);
		cache.at(lvl, 100L);
		long p = BlockPos.asLong(10, 64, -3);
		check("cache: empty -> -1", cache.count(p, webTag) == -1);
		cache.put(p, webTag, 6);
		check("cache: stored count read back", cache.count(p, webTag) == 6);
		check("cache: other tier at the same position -> -1", cache.count(p, cropTag) == -1);
		cache.at(lvl, 100L);
		check("cache: same tick keeps it", cache.count(p, webTag) == 6);
		cache.at(lvl, 101L);
		check("cache: next tick starts empty", cache.count(p, webTag) == -1);
		cache.put(p, webTag, 5);
		cache.at(new Object(), 101L);
		check("cache: another level starts empty", cache.count(p, webTag) == -1);
		cache.at(lvl, 102L);
		int ok = 0;
		for (int i = 0; i < 3000; i++) cache.put(BlockPos.asLong(i % 60, 64, i / 60), cropTag, i % 9);
		for (int i = 0; i < 3000; i++) {
			int k = cache.count(BlockPos.asLong(i % 60, 64, i / 60), cropTag);
			if (k == i % 9) ok++;
			else if (k != -1) ok = Integer.MIN_VALUE; // a wrong count would be a real fault
		}
		check("cache: 3000 cells in one tick, no wrong count, most kept (" + ok + ")", ok > 2500);
		cache.clear();
		check("cache: clear", cache.count(BlockPos.asLong(0, 64, 0), cropTag) == -1);
	}
}
