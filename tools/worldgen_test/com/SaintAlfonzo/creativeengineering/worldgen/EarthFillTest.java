package com.SaintAlfonzo.creativeengineering.worldgen;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Headless checks of EarthFill: the counting core, earth and water blocks of a real template through rotation, pool elements, overrides, seabed. */
public class EarthFillTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static Holder<Block> h(Block b) { return b.builtInRegistryHolder(); }

	static long[] longs(int[]... xyz) {
		long[] out = new long[xyz.length];
		for (int i = 0; i < xyz.length; i++) out[i] = BlockPos.asLong(xyz[i][0], xyz[i][1], xyz[i][2]);
		return out;
	}

	static StructureTemplate template(List<StructureTemplate.StructureBlockInfo> blocks) throws Exception {
		StructureTemplate t = new StructureTemplate();
		Constructor<StructureTemplate.Palette> c = StructureTemplate.Palette.class.getDeclaredConstructor(List.class);
		c.setAccessible(true);
		Field f = StructureTemplate.class.getDeclaredField("palettes");
		f.setAccessible(true);
		((List<Object>) f.get(t)).add(c.newInstance(blocks));
		return t;
	}

	static StructureTemplate.StructureBlockInfo info(int x, int y, int z, Block b) {
		return info(x, y, z, b.defaultBlockState());
	}

	static StructureTemplate.StructureBlockInfo info(int x, int y, int z, BlockState state) {
		return new StructureTemplate.StructureBlockInfo(new BlockPos(x, y, z), state, null);
	}

	static EarthFill.Limits earthLimits(int water, int air) { return new EarthFill.Limits(-1, water, air); }

	static final class Fake extends Structure {
		Fake() { super(null); }
		public Optional<GenerationStub> findGenerationPoint(GenerationContext c) { return Optional.empty(); }
		public net.minecraft.world.level.levelgen.structure.StructureType<?> type() { return null; }
	}

	public static void main(String[] args) throws Exception {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		TagKey<Block> earthTag = BlockTags.create(new ResourceLocation("creativeengineering", "earth_fill/earth"));
		Map<TagKey<Block>, List<Holder<Block>>> tags = new HashMap<>();
		tags.put(earthTag, List.of(h(Blocks.DIRT), h(Blocks.GRASS_BLOCK), h(Blocks.SAND)));
		tags.put(EarthFill.STONE_BLOCKS, List.of(h(Blocks.STONE)));
		Registry.BLOCK.bindTags(tags);
		List<TagKey<Block>> earth = List.of(earthTag);

		// 1. the counting core: water below y 10, air above 12, solid between; the water/air ratio per limit
		EarthFill.Terrain lake = (x, y, z) -> y < 10 ? EarthFill.Kind.WATER : y > 12 ? EarthFill.Kind.AIR : EarthFill.Kind.SOLID;
		long[] pad = longs(new int[]{0, 5, 0}, new int[]{1, 5, 0}, new int[]{2, 11, 0}, new int[]{3, 14, 0}, new int[]{4, 15, 0});
		EarthFill.Result r = EarthFill.count(List.of(pad), lake, earthLimits(-1, -1), false);
		check(r.blocks() == 5 && r.water() == 2 && r.air() == 2 && !r.cancelled() && r.complete(), "counts 5 earth: 2 water, 2 air, 1 solid; no limits, not cancelled");
		check(!EarthFill.count(List.of(pad), lake, earthLimits(2, 2), false).cancelled(), "at the limit is allowed");
		check(EarthFill.count(List.of(pad), lake, earthLimits(1, -1), false).cancelled(), "water 2 > limit 1 cancels");
		check(EarthFill.count(List.of(pad), lake, earthLimits(-1, 1), false).cancelled(), "air 2 > limit 1 cancels");
		check(EarthFill.count(List.of(pad), lake, earthLimits(0, 0), false).cancelled(), "0 and 0: any water or air cancels");
		check(!EarthFill.count(List.of(longs(new int[]{0, 11, 0})), lake, earthLimits(0, 0), false).cancelled(), "all earth solid: nothing to cancel at 0/0");
		r = EarthFill.count(List.of(pad), lake, earthLimits(0, -1), true);
		check(r.cancelled() && !r.complete() && r.blocks() == 1 && r.water() == 1, "stopEarly: stops at the first water block (counted 1)");
		r = EarthFill.count(List.of(pad, pad), lake, earthLimits(-1, -1), false);
		check(r.blocks() == 5, "the same position in two pieces counts once");
		check(EarthFill.count(List.<long[]>of(), lake, earthLimits(0, 0), false).blocks() == 0, "no earth, no result");

		// 1b. earth after the beard (7.3.1): a slope, ground at y 60 - x, so a pad at y 61 has x air blocks under its bottom layer
		EarthFill.Terrain slope = (x, y, z) -> y <= 60 - x ? EarthFill.Kind.SOLID : EarthFill.Kind.AIR;
		int[][] row = new int[13][];
		for (int x = 0; x <= 12; x++) row[x] = new int[]{x, 61, 0};
		long[] padRow = longs(row);
		EarthFill.Reach reach = new EarthFill.Reach(6, 10, 8);
		EarthFill.EarthLimits open = new EarthFill.EarthLimits(-1, -1, -1);
		EarthFill.EarthResult er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.NONE)), slope, open, reach, false);
		check(er.blocks() == 13 && er.air() == 13, "no beard: every earth block over air counts (13)");
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD)), slope, open, reach, false);
		check(er.air() == 6, "beard reach 6: only the air deeper than 6 below the piece counts (x 7..12 = 6)");
		check(er.mound() == 4, "mound height 8: columns with more than 8 air blocks under the piece (x 9..12 = 4)");
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BURY)), slope, open, reach, false);
		check(er.air() == 2, "bury reach 10: x 11 and 12 (2)");
		long[] upper = longs(new int[]{6, 63, 0}, new int[]{7, 63, 0});
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(upper, 61, EarthFill.Beard.BEARD)), slope, open, reach, false);
		check(er.air() == 1, "an upper-layer block is judged by the gap under the piece's bottom, not under itself (x 7 counts, x 6 doesn't)");
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD), new EarthFill.EarthPiece(longs(new int[]{10, 62, 0}, new int[]{11, 62, 0}), 61, EarthFill.Beard.BEARD)),
				slope, open, reach, false);
		check(er.mound() == 4 && er.blocks() == 15, "a column counts once as a mound, however many earth blocks it holds");
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD)), slope, new EarthFill.EarthLimits(-1, -1, 3), reach, false);
		check(er.cancelled() && er.complete(), "mound 4 > limit 3 cancels");
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD)), slope, new EarthFill.EarthLimits(-1, 2, -1), reach, true);
		check(er.cancelled() && !er.complete() && er.air() == 3, "stopEarly: stops at the third air block past the beard");
		check(!EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD)), slope, new EarthFill.EarthLimits(-1, 6, 4), reach, false).cancelled(),
				"at the limits is allowed");
		check(EarthFill.earthBlocks(List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD), new EarthFill.EarthPiece(upper, 61, EarthFill.Beard.BEARD),
				new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD))) == 15, "a start's size counts each earth position once (13 + 2)");
		// earthfill #10: a start sunk 4 raises the beard's ground level by 4 (EarthCheck.cancels builds the piece at minY + sink),
		// so under the real bottom the beard reaches 6 - 4 = 2: a gap of 3 or more (x >= 3) is past it
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(padRow, 61 + 4, EarthFill.Beard.BEARD)), slope, open, reach, false);
		check(er.air() == 10, "sunk 4: the beard reaches 2 under the real bottom, x 3..12 count (10; unsunk 6)");
		check(er.mound() == 8, "sunk 4: mound measured from the raised level, x 5..12 (8; unsunk 4)");
		// terrain #2 beardFootprint: a stone floor on the same slope (row z 1) and the earth pad's own row (z 0)
		int[][] stoneRow = new int[13][];
		for (int x = 0; x <= 12; x++) stoneRow[x] = new int[]{x, 61, 1};
		List<EarthFill.EarthPiece> padPiece = List.of(new EarthFill.EarthPiece(padRow, 61, EarthFill.Beard.BEARD));
		check(EarthFill.baseMound(List.of(new EarthFill.BasePiece(longs(stoneRow), 61)), List.of(), slope, reach) == 4,
				"base mound: a stone floor's columns deeper than 8 count (x 9..12 = 4), with no earth at all");
		check(EarthFill.baseMound(List.of(new EarthFill.BasePiece(padRow, 61)), padPiece, slope, reach) == 0,
				"base mound: columns that hold earth are left to countEarth");
		er = EarthFill.withBaseMound(EarthFill.countEarth(padPiece, slope, new EarthFill.EarthLimits(-1, -1, 6), reach, false),
				EarthFill.baseMound(List.of(new EarthFill.BasePiece(longs(stoneRow), 61)), padPiece, slope, reach), new EarthFill.EarthLimits(-1, -1, 6));
		check(er.mound() == 8 && er.cancelled(), "earth mound 4 + base mound 4 = 8 > limit 6 cancels (4 alone is kept)");
		EarthFill.Terrain shelf = (x, y, z) -> y <= 58 ? EarthFill.Kind.SOLID : EarthFill.Kind.AIR;
		check(EarthFill.countEarth(List.of(new EarthFill.EarthPiece(longs(new int[]{0, 61, 0}), 65, EarthFill.Beard.BEARD)), shelf, open, reach, false).air() == 0,
				"sunk 4: ground 2 below the real bottom is within the beard's reach, not counted");
		check(EarthFill.countEarth(List.of(new EarthFill.EarthPiece(longs(new int[]{0, 60, 0}), 64, EarthFill.Beard.BEARD)),
				(x, y, z) -> y <= 56 ? EarthFill.Kind.SOLID : EarthFill.Kind.AIR, open, reach, false).air() == 1,
				"brief case: bottom 60, sink 4, earth at y 60 over a 4-block gap counts over air (before the fix: not)");

		// earthfill #9: an override with every limit -1 is never cancelled, also when log = ALL measures it
		EarthFill.EarthResult over = new EarthFill.EarthResult(10, 5, 5, 3, true, true), under = new EarthFill.EarthResult(10, 0, 0, 0, false, true);
		EarthFill.Footprint wetFp = new EarthFill.Footprint(10, 2);
		check(!EarthFill.earthCancelled(true, under, wetFp, 0.5), "unlimited override + log ALL + wet footprint: kept");
		check(!EarthFill.earthCancelled(true, over, null, 0.5), "unlimited override + over a size limit (mound unset): kept");
		check(EarthFill.earthCancelled(false, under, wetFp, 0.5), "limited structure + wet footprint: cancelled");
		check(EarthFill.earthCancelled(false, over, null, -1), "limited structure over its limits: cancelled");
		check(!EarthFill.earthCancelled(false, under, wetFp, -1), "surplus -1: footprint never cancels");
		// a river, water up to y 62 over its bed at y 55 (x < 5) or 49; a house floor set into the top water layer (y 62), bottom 62
		EarthFill.Terrain river = (x, y, z) -> y <= (x >= 5 ? 49 : 55) ? EarthFill.Kind.SEABED : y <= 62 ? EarthFill.Kind.WATER : EarthFill.Kind.AIR;
		long[] floor = longs(new int[]{0, 62, 0}, new int[]{1, 62, 0}, new int[]{5, 62, 0}, new int[]{6, 62, 0});
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(floor, 62, EarthFill.Beard.BEARD)), river, open, reach, false);
		check(er.water() == 2 && er.air() == 0, "beard: water as deep as its reach is filled (x 0, 1: 6 under the piece), deeper water counts (x 5, 6: 12)");
		check(er.mound() == 2, "and the deep-water columns are mound columns");
		er = EarthFill.countEarth(List.of(new EarthFill.EarthPiece(floor, 62, EarthFill.Beard.NONE)), river, open, reach, false);
		check(er.water() == 4, "no beard: every earth block in water counts");
		// the footprint's water share (2026-10-19): a lake (water at y 62, its bed below) for x >= 6, dry ground (top y 62) for x < 6
		EarthFill.Terrain lakeShore = (x, y, z) -> x >= 6 ? (y <= 58 ? EarthFill.Kind.SEABED : y <= 62 ? EarthFill.Kind.WATER : EarthFill.Kind.AIR)
				: y <= 62 ? EarthFill.Kind.SOLID : EarthFill.Kind.AIR;
		EarthFill.Footprint fp = EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 9, 1, 62)), lakeShore, 3);
		check(fp.water() == 8 && fp.ground() == 12, "footprint: every box column at the layer, water x 6..9 (8), ground x 0..5 (12)");
		check(!fp.tooWet(0.5), "8 water against 12 ground passes at surplus 0.5");
		fp = EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 14, 0, 62)), lakeShore, 3);
		check(fp.water() == 9 && fp.ground() == 6 && !fp.tooWet(0.5), "9 water against 6 ground: exactly 50% more, passes");
		fp = EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 15, 0, 62)), lakeShore, 3);
		check(fp.water() == 10 && fp.tooWet(0.5) && !fp.tooWet(1.0) && !fp.tooWet(-1), "10 against 6 is too wet at 0.5, not at 1.0, never at -1");
		check(EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 15, 0, 62), new EarthFill.Layer(10, 0, 15, 0, 62)), lakeShore, 3).water() == 10,
				"a position in two layers counts once");
		check(EarthFill.footprint(List.of(new EarthFill.Layer(6, 0, 9, 0, 57)), lakeShore, 3).water() == 0, "seabed under the layer is neither water nor ground");
		check(EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 5, 0, 63)), lakeShore, 3).ground() == 0, "air at the layer counts as neither");
		// buried positions: the same lake shore under 10 blocks of rock from x 0 to 4 (its top at y 72), open from x 5 on
		EarthFill.Terrain buried = new EarthFill.Terrain() {
			public EarthFill.Kind at(int x, int y, int z) { return lakeShore.at(x, y, z) == EarthFill.Kind.AIR && x < 5 && y <= 72 ? EarthFill.Kind.SOLID : lakeShore.at(x, y, z); }
			public int top(int x, int z) { return x < 5 ? 72 : 62; }
		};
		fp = EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 9, 0, 62)), buried, 3);
		check(fp.ground() == 1 && fp.water() == 4, "more than 3 blocks of terrain above: buried, neither (x 0..4 skipped, x 5 ground, x 6..9 water)");
		check(EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 9, 0, 62)), buried, 10).ground() == 6, "depth 10: the 10 blocks of rock are shallow enough");
		// pruning: the jigsaw tree from junctions. 0 = street (root), 1 = house on the street, 2 = villager in the house,
		// 3 = another house on the street, 4 = a lamp on house 3 by an up jigsaw (same column); 5 = a piece with no junctions
		List<net.minecraft.world.level.levelgen.structure.BoundingBox> boxes = List.of(
				new net.minecraft.world.level.levelgen.structure.BoundingBox(0, 64, 0, 20, 64, 2),
				new net.minecraft.world.level.levelgen.structure.BoundingBox(4, 64, 3, 10, 70, 9),
				new net.minecraft.world.level.levelgen.structure.BoundingBox(6, 65, 5, 6, 66, 5),
				new net.minecraft.world.level.levelgen.structure.BoundingBox(12, 64, 3, 18, 70, 9),
				new net.minecraft.world.level.levelgen.structure.BoundingBox(15, 71, 6, 15, 73, 6),
				new net.minecraft.world.level.levelgen.structure.BoundingBox(30, 64, 30, 31, 65, 31));
		List<List<EarthFill.Joint>> joints = List.of(
				List.of(new EarthFill.Joint(7, 3, 0), new EarthFill.Joint(15, 3, 0)),        // street -> both houses' doors
				List.of(new EarthFill.Joint(7, 2, 0), new EarthFill.Joint(6, 5, -1)),        // house 1: up to the street, down to its villager
				List.of(new EarthFill.Joint(6, 6, 1)),                                         // villager: up to house 1 (one block over)
				List.of(new EarthFill.Joint(15, 2, 0), new EarthFill.Joint(15, 6, 3)),       // house 3: up to the street, its roof jigsaw
				List.of(new EarthFill.Joint(15, 6, -3)),                                       // lamp on house 3's roof (same column)
				List.of());
		int[][] tree = EarthFill.links(boxes, joints);
		check(java.util.Arrays.equals(tree[0], new int[]{-1, 0, 1, 0, 3, -1}), "jigsaw tree from junctions: houses on the street, villager in house 1, lamp on house 3, a loose piece unlinked");
		check(tree[1][1] == 0 && tree[1][3] == 1 && tree[1][2] == 1 && tree[1][4] == 1, "and which parent junction leads to each child");
		boolean[] drop = EarthFill.withChildren(new boolean[]{false, true, false, false, false, false}, tree[0]);
		check(java.util.Arrays.equals(drop, new boolean[]{false, true, true, false, false, false}), "dropping house 1 drops its villager, not the other house or its lamp");
		drop = EarthFill.withChildren(new boolean[]{false, false, false, false, false, true}, tree[0]);
		check(java.util.Arrays.equals(drop, new boolean[]{false, false, false, false, false, true}), "an unlinked piece drops alone");
		List<List<EarthFill.Joint>> wrongDelta = new ArrayList<>(joints);
		wrongDelta.set(2, List.of(new EarthFill.Joint(6, 6, -1)));
		check(EarthFill.links(boxes, wrongDelta)[0][2] == -1, "a junction pair must have opposite deltaY");
		List<Integer> bounds = List.of(150, 600, 2000);
		check(EarthFill.sizeOf(149, bounds) == 0 && EarthFill.sizeOf(150, bounds) == 1 && EarthFill.sizeOf(599, bounds) == 1 && EarthFill.sizeOf(600, bounds) == 2
				&& EarthFill.sizeOf(2000, bounds) == 3 && EarthFill.sizeOf(90000, bounds) == 3, "sizes: under 150 small, 150..599 medium, 600..1999 large, 2000+ huge");
		check(EarthFill.sizeOf(700, List.of(2000, 150, 600)) == 2, "earthfill #13: unsorted bounds are sorted first (700 is large)");

		// earthfill #14: pieces with no earth join the footprint as spans, by the surface within their height (the
		// predicted terrain knows each column's top; the lake's and the shore's are both y 62)
		EarthFill.Terrain lakeTop = new EarthFill.Terrain() {
			public EarthFill.Kind at(int x, int y, int z) { return lakeShore.at(x, y, z); }
			public int top(int x, int z) { return 62; }
		};
		fp = EarthFill.footprint(List.of(), List.of(new EarthFill.Span(0, 0, 15, 0, 62, 75)), lakeTop, 3);
		check(fp.water() == 10 && fp.ground() == 6, "span over the lake shore: water x 6..15 (10), ground x 0..5 (6), by the column top");
		check(EarthFill.footprint(List.of(), List.of(new EarthFill.Span(0, 0, 15, 0, 70, 80)), lakeTop, 3).water() == 0,
				"span floating 8 above the surface (bottom 70, top y 62): neither");
		check(EarthFill.footprint(List.of(), List.of(new EarthFill.Span(0, 0, 15, 0, 20, 40)), lakeTop, 3).ground() == 0,
				"span buried under the surface (top 40 < 62): neither");
		check(EarthFill.footprint(List.of(), List.of(new EarthFill.Span(0, 0, 15, 0, 64, 75)), lakeTop, 3).water() == 10,
				"span bottom 64: the surface at 62 is within baseLayers below it, counted");
		fp = EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 9, 0, 62)), List.of(new EarthFill.Span(0, 0, 15, 0, 62, 75)), lakeTop, 3);
		check(fp.water() == 10 && fp.ground() == 6, "a column already in a layer is not counted again by a span");
		PoolElementStructurePiece street = new PoolElementStructurePiece(null,
				new SinglePoolElement(new StructureTemplate()) {
					@Override public net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection getProjection() {
						return net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.TERRAIN_MATCHING;
					}
				}, BlockPos.ZERO, 0, Rotation.NONE, new BoundingBox(0, 0, 0, 1, 1, 1));
		check(!EarthFill.rigid(street), "a terrain-matching street never joins the footprint");
		check(EarthFill.rigid(new PoolElementStructurePiece(null, new SinglePoolElement(new StructureTemplate()), BlockPos.ZERO, 0, Rotation.NONE,
				new BoundingBox(0, 0, 0, 1, 1, 1))), "a rigid house does");

		// earthfill #13 E1: a two-number override keeps the size's mound limit, only three -1 lift everything
		check(!EarthFill.unlimited(new EarthFill.Override("a:b", -1, -1, EarthFill.Override.UNSET)), "\"a:b=-1,-1\" is not unlimited (mound from the size)");
		check(EarthFill.unlimited(new EarthFill.Override("a:b", -1, -1, -1)), "\"a:b=-1,-1,-1\" is unlimited");
		check(!EarthFill.unlimited(null), "no override: not unlimited");

		// D4 (2026-10-30): pruning drops a piece whose own footprint is too wet, once it has pruneMinWetColumns water columns
		List<net.minecraft.world.level.levelgen.structure.StructurePiece> village = new ArrayList<>();
		for (int i = 0; i < 4; i++) village.add(new PoolElementStructurePiece(null, new SinglePoolElement(new StructureTemplate()), BlockPos.ZERO, 0,
				Rotation.NONE, new BoundingBox(i * 10, 64, 0, i * 10 + 7, 70, 7)));
		// piece 1 dry, piece 2 on a lake (40 water / 24 ground), piece 3 over a small pond (5 water / 2 ground)
		java.util.Map<Object, EarthFill.Footprint> fps = new java.util.IdentityHashMap<>();
		fps.put(village.get(1), new EarthFill.Footprint(0, 64));
		fps.put(village.get(2), new EarthFill.Footprint(40, 24));
		fps.put(village.get(3), new EarthFill.Footprint(5, 2));
		List<String> why = new ArrayList<>();
		List<net.minecraft.world.level.levelgen.structure.StructurePiece> kept = EarthFill.pruned(village, p -> new long[0], EarthFill.Beard.BEARD, 0,
				(x, y, z) -> EarthFill.Kind.SOLID, fps::get, 0.0, 16, why);
		check(kept.size() == 3 && !kept.contains(village.get(2)) && kept.contains(village.get(3)),
				"wet piece (40 / 24) pruned; the pond piece (5 water) is under pruneMinWetColumns 16 and stays; root never judged");
		check(why.size() == 1 && why.get(0).contains("footprint 40 water / 24 ground"), "the log line names the footprint");
		check(EarthFill.pruned(village, p -> new long[0], EarthFill.Beard.BEARD, 0, (x, y, z) -> EarthFill.Kind.SOLID, fps::get, -1, 16, new ArrayList<>()) == village,
				"surplus -1: the water share never prunes");
		// E3: a sunk piece is pruned at its raised ground level. Earth at y 60 over a 4-block gap, bottom 60, sink 4:
		// unsunk the beard (6) fills it, raised to 64 the beard reaches only 2 below the bottom and the earth hangs
		EarthFill.Terrain gap = (x, y, z) -> y <= 56 ? EarthFill.Kind.SOLID : EarthFill.Kind.AIR;
		List<net.minecraft.world.level.levelgen.structure.StructurePiece> two = List.of(village.get(0), new PoolElementStructurePiece(null,
				new SinglePoolElement(new StructureTemplate()), BlockPos.ZERO, 0, Rotation.NONE, new BoundingBox(0, 60, 0, 8, 66, 0)));
		long[] sunkPad = new long[9];
		for (int x = 0; x < 9; x++) sunkPad[x] = new BlockPos(x, 60, 0).asLong();
		java.util.function.Function<net.minecraft.world.level.levelgen.structure.StructurePiece, long[]> padOf = p -> p == two.get(1) ? sunkPad : new long[0];
		check(EarthFill.pruned(two, padOf, EarthFill.Beard.BEARD, 0, gap, null, 0, 16, new ArrayList<>()) == two, "unsunk: the beard fills the 4-block gap, kept");
		check(EarthFill.pruned(two, padOf, EarthFill.Beard.BEARD, 4, gap, null, 0, 16, new ArrayList<>()).size() == 1, "sink 4: 9 earth hang (> pruneMaxHanging 8), pruned");

		// earthfill #15 (phase 4, 2026-10-30). WET pruning (pruneWetJigsaws): no piece limits, only the water share
		check(EarthFill.pruned(two, padOf, EarthFill.Beard.BEARD, 4, gap, false, null, 0, 16, new ArrayList<>()) == two,
				"wet only: the 9 hanging earth don't drop the piece");
		kept = EarthFill.pruned(village, p -> new long[0], EarthFill.Beard.BEARD, 0, (x, y, z) -> EarthFill.Kind.SOLID, false, fps::get, 0.0, 16, new ArrayList<>());
		check(kept.size() == 3 && !kept.contains(village.get(2)), "wet only: the lake piece (40 / 24) still goes");
		// which starts prune: villages FULL, other surface jigsaws WET, single pieces / legacy / underground / opted out NONE
		List<net.minecraft.world.level.levelgen.structure.StructurePiece> legacy = List.of(new net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece(
				net.minecraft.util.RandomSource.create(0), 0, 0), village.get(1));
		check(EarthFill.pruneMode(true, true, true, false, false, true, village) == EarthFill.Prune.FULL, "tagged prune: FULL");
		check(EarthFill.pruneMode(true, true, false, false, false, true, village) == EarthFill.Prune.WET, "other surface jigsaw: WET");
		check(EarthFill.pruneMode(true, false, false, false, false, true, village) == EarthFill.Prune.NONE, "pruneWetJigsaws off: NONE");
		check(EarthFill.pruneMode(true, true, false, false, false, false, village) == EarthFill.Prune.NONE, "underground jigsaw: NONE");
		check(EarthFill.pruneMode(true, true, false, false, false, true, village.subList(0, 1)) == EarthFill.Prune.NONE, "one piece: NONE");
		check(EarthFill.pruneMode(true, true, false, false, false, true, legacy) == EarthFill.Prune.NONE, "first piece not a jigsaw piece (legacy): NONE");
		check(EarthFill.pruneMode(true, true, true, true, false, true, village) == EarthFill.Prune.NONE, "earth_fill/no_prune wins over the prune tag");
		check(EarthFill.pruneMode(true, true, true, false, true, true, village) == EarthFill.Prune.NONE, "unlimited override: NONE");
		check(EarthFill.pruneMode(false, true, true, false, false, true, village) == EarthFill.Prune.NONE, "prune off: NONE");
		// E7: the whole-start water share needs footprintMinWater water columns
		check(new EarthFill.Footprint(1, 0).tooWet(0.0) && !new EarthFill.Footprint(1, 0).tooWet(0.0, 9), "E7: 1 water / 0 ground is under the floor 9");
		check(new EarthFill.Footprint(9, 0).tooWet(0.0, 9) && !new EarthFill.Footprint(9, 9).tooWet(0.0, 9), "E7: 9 water / 0 ground at the floor is too wet, 9 / 9 is not");
		EarthFill.EarthResult none = new EarthFill.EarthResult(10, 0, 0, 0, false, true);
		check(!EarthFill.earthCancelled(false, none, new EarthFill.Footprint(2, 0), 0.0, 9) && EarthFill.earthCancelled(false, none, new EarthFill.Footprint(2, 0), 0.0, 0),
				"E7: the verdict uses the floor; 0 = any sample");
		// E12: stacked layers count a column once, at its lowest layer that is not buried
		EarthFill.Terrain deep = new EarthFill.Terrain() {
			public EarthFill.Kind at(int x, int y, int z) { return y <= 50 ? EarthFill.Kind.SEABED : y <= 66 ? EarthFill.Kind.WATER : EarthFill.Kind.AIR; }
			public int top(int x, int z) { return 66; }
		};
		check(EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 9, 0, 64), new EarthFill.Layer(0, 0, 9, 0, 62)), deep, 3).water() == 10,
				"E12: two stacked layers in deep water: 10 columns, not 20");
		fp = EarthFill.footprint(List.of(new EarthFill.Layer(0, 0, 4, 0, 62), new EarthFill.Layer(0, 0, 4, 0, 70)), buried, 3);
		check(fp.ground() == 5 && fp.water() == 0, "E12: a buried lower layer leaves the column to the layer above (rock at 70, 2 under the top)");
		// E8: a parent forgets its junctions to dropped children by identity, whatever their order
		int[][] outOfOrder = {{-1, 0, 1, 0, 3, -1}, {-1, 1, 1, 0, 1, -1}};   // house 1 by the street's 2nd junction, house 3 by its 1st
		List<List<String>> live = new ArrayList<>();
		live.add(new ArrayList<>(List.of("to house 3", "to house 1")));
		for (int i = 1; i < 6; i++) live.add(new ArrayList<>(List.of("own " + i)));
		EarthFill.forgetJunctions(boxes, joints, outOfOrder, new boolean[]{false, true, true, true, true, false}, live);
		check(live.get(0).isEmpty(), "E8: both houses dropped: the street keeps neither junction (index removal left one)");
		live.set(0, new ArrayList<>(List.of("to house 3", "to house 1")));
		EarthFill.forgetJunctions(boxes, joints, outOfOrder, new boolean[]{false, true, true, false, false, false}, live);
		check(live.get(0).equals(List.of("to house 3")), "E8: house 1 dropped: only its junction goes");
		// an unlinked dropped piece: kept junctions pointing into its box (and into no kept box) go
		List<List<EarthFill.Joint>> toLoose = new ArrayList<>(joints);
		toLoose.set(0, List.of(new EarthFill.Joint(7, 3, 0), new EarthFill.Joint(30, 30, 0)));
		live.set(0, new ArrayList<>(List.of("to house 1", "to the loose piece")));
		EarthFill.forgetJunctions(boxes, toLoose, tree, new boolean[]{false, false, false, false, false, true}, live);
		check(live.get(0).equals(List.of("to house 1")), "E8: the junction into the unlinked dropped piece goes, the one into kept house 1 stays");

		// 2. earth blocks of a template: bottom layers only, rotation, offset. 3x4x3: grass pad y0, dirt skirt y1 at x0, stone walls, dirt roof garden y3.
		List<StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>();
		for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) blocks.add(info(x, 0, z, Blocks.GRASS_BLOCK));
		blocks.add(info(0, 1, 0, Blocks.DIRT));
		blocks.add(info(1, 1, 1, Blocks.STONE));
		blocks.add(info(1, 2, 1, Blocks.STONE));
		blocks.add(info(1, 3, 1, Blocks.DIRT));      // roof garden: above the base layers
		blocks.add(info(2, 2, 2, Blocks.SAND));      // layer 2: inside 3 base layers, outside 2
		StructureTemplate t = template(blocks);
		check(EarthFill.localEarth(t, earth, 3).length == 9 + 1 + 1, "3 base layers: 9 pad + 1 skirt + 1 sand = 11 (roof garden and stone left out)");
		check(EarthFill.localEarth(t, earth, 2).length == 10, "2 base layers: the layer-2 sand drops out (10)");
		check(EarthFill.localEarth(t, earth, 1).length == 9, "1 base layer: the pad only (9)");
		List<TagKey<Block>> withStone = List.of(earthTag, EarthFill.STONE_BLOCKS);
		check(EarthFill.localEarth(t, withStone, 3).length == 13, "stone base (earth_fill/stone_base): the 2 base-layer stone blocks count too (13)");
		check(EarthFill.localEarth(t, earth, 3).length == 11, "and the plain earth list still gives 11 afterwards (separate caches)");

		Function<StructureTemplate, long[]> earth3 = tt -> EarthFill.localEarth(tt, earth, 3);
		LongArrayList out = new LongArrayList();
		EarthFill.addTemplate(t, new BlockPos(100, 64, -50), new StructurePlaceSettings(), earth3, out);
		check(out.contains(BlockPos.asLong(100, 64, -50)) && out.contains(BlockPos.asLong(102, 64, -48)) && out.contains(BlockPos.asLong(100, 65, -50)), "no rotation: local + origin");
		out.clear();
		EarthFill.addTemplate(t, new BlockPos(100, 64, -50), new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90), earth3, out);
		// (x, z) -> (-z, x): the skirt at local (0, 1, 0) stays at the origin, local (2, 0, 2) goes to (-2, 0, 2)
		check(out.contains(BlockPos.asLong(100, 65, -50)) && out.contains(BlockPos.asLong(98, 64, -48)) && !out.contains(BlockPos.asLong(102, 64, -48)),
				"CLOCKWISE_90 turns the pad around its corner");
		check(out.size() == 11 && out.stream().allMatch(l -> BlockPos.getY(l) >= 64 && BlockPos.getY(l) <= 66), "rotation keeps 11 blocks and never moves Y");

		// 3. pool elements: single, list, terrain-matching (skipped), and a whole piece
		SinglePoolElement single = new SinglePoolElement(t);
		out.clear();
		EarthFill.addElement(single, BlockPos.ZERO, Rotation.NONE, null, earth3, out);
		check(out.size() == 11, "a rigid single element counts its earth");
		out.clear();
		EarthFill.addElement(single.setProjection(StructureTemplatePool.Projection.TERRAIN_MATCHING), BlockPos.ZERO, Rotation.NONE, null, earth3, out);
		check(out.isEmpty(), "a terrain-matching element (village street) is skipped");
		out.clear();
		ListPoolElement list = new ListPoolElement(List.<StructurePoolElement>of(new SinglePoolElement(t), new SinglePoolElement(t)), StructureTemplatePool.Projection.RIGID);
		EarthFill.addElement(list, BlockPos.ZERO, Rotation.NONE, null, earth3, out);
		check(out.size() == 22, "a list element counts each of its elements (22)");
		PoolElementStructurePiece piece = new PoolElementStructurePiece(null, new SinglePoolElement(t), new BlockPos(10, 70, 10), 0, Rotation.COUNTERCLOCKWISE_90,
				new BoundingBox(10, 70, 10, 13, 74, 13));
		long[] world = EarthFill.pieceEarth(piece, null, earth, 3);
		check(world.length == 11, "a pool-element piece gives its element's earth at its position and rotation (11)");

		// 4. overrides: parsing and matching
		List<EarthFill.Override> parsed = EarthFill.parseOverrides("earth-fill", List.of("philipsruins:ancient_ruins=0,16", "#minecraft:village=-1,-1,-1", "garbage", "a:b=1",
				"a:b=x,2", "a:b=1,2,3,4"), true);
		check(parsed.size() == 2 && parsed.get(0).key().equals("philipsruins:ancient_ruins") && parsed.get(0).a() == 0 && parsed.get(0).b() == 16
				&& parsed.get(0).c() == EarthFill.Override.UNSET && parsed.get(1).key().equals("#minecraft:village") && parsed.get(1).c() == -1,
				"earth overrides: 2 or 3 numbers parsed (mound left out = from the size), 4 malformed lines skipped");
		check(EarthFill.parseOverrides("water-fill", List.of("a:b=1,2,3"), false).isEmpty(), "water overrides take exactly 2 numbers");
		parsed = EarthFill.parseOverrides("earth-fill", List.of("village_plains=1,2", "#village=1,2", "Bad Id=1,2"), true);
		check(parsed.size() == 2 && parsed.get(0).key().equals("minecraft:village_plains") && parsed.get(1).key().equals("#minecraft:village"),
				"earthfill #13: an id or tag without a namespace is minecraft's; an id that doesn't parse is skipped");

		ResourceKey<Registry<Structure>> reg = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/structure"));
		ResourceKey<Structure> key = ResourceKey.create(reg, new ResourceLocation("philipsruins:ancient_ruins"));
		Holder.Reference<Structure> holder = Holder.Reference.createStandAlone(null, key);
		holder.bind(key, new Fake());
		EarthFill.EarthLimits lim = EarthFill.earthLimitsFor(holder, 2);
		check(lim.water() == 24 && lim.air() == 160 && lim.mound() == 64, "no overrides configured: a large start gets 24 over water, 160 over air, 64 mound columns");
		lim = EarthFill.earthLimitsFor(holder, 0);
		check(lim.water() == 24 && lim.air() == 24 && lim.mound() == 8, "a small start: 24 over water, 24 over air, 8 mound columns");
		EarthFill.Limits wlim = EarthFill.waterLimitsFor(holder);
		check(wlim.ground() == 32 && wlim.water() == -1 && wlim.air() == 16, "water fill, no matching override: the global defaults (32 into ground, 16 into air, water never)");

		// 5. water fill: the water blocks of a template (all layers), counted against ground / seabed / water / air
		List<StructureTemplate.StructureBlockInfo> basin = new ArrayList<>();
		for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) basin.add(info(x, 0, z, Blocks.STONE_BRICKS));
		basin.add(info(1, 1, 1, Blocks.WATER));
		basin.add(info(0, 1, 0, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.WATERLOGGED, true)));
		basin.add(info(2, 1, 2, Blocks.STONE_BRICK_STAIRS));                 // dry stairs: no water
		basin.add(info(1, 5, 1, Blocks.KELP));                               // high up: every layer counts
		basin.add(info(2, 2, 0, Blocks.SEAGRASS));
		basin.add(info(0, 2, 2, Blocks.DIRT));
		StructureTemplate wt = template(basin);
		check(EarthFill.localWater(wt).length == 4, "water blocks: water + waterlogged stairs + kelp (layer 5) + seagrass = 4; dry stairs, bricks, dirt left out");
		PoolElementStructurePiece wpiece = new PoolElementStructurePiece(null, new SinglePoolElement(wt), new BlockPos(0, 60, 0), 0, Rotation.NONE,
				new BoundingBox(0, 60, 0, 3, 66, 3));
		long[] wworld = EarthFill.pieceWater(wpiece, null);
		check(wworld.length == 4 && java.util.Arrays.stream(wworld).anyMatch(l -> l == BlockPos.asLong(1, 65, 1)), "a piece's water lands at its position (kelp at y 65)");
		// the sea at x < 2: water y 61-62, seabed below, air from 63; dry land at x >= 2: solid up to 61, air above
		EarthFill.Terrain shore = (x, y, z) -> x >= 2 ? (y <= 61 ? EarthFill.Kind.SOLID : EarthFill.Kind.AIR)
				: y >= 63 ? EarthFill.Kind.AIR : y >= 61 ? EarthFill.Kind.WATER : EarthFill.Kind.SEABED;
		EarthFill.Result wr = EarthFill.count(List.of(wworld), shore, new EarthFill.Limits(-1, -1, -1), false);
		// water (1,61,1) and stairs (0,61,0) in the sea; kelp (1,65,1) above it; seagrass (2,62,0) on land, in air
		check(wr.blocks() == 4 && wr.ground() == 0 && wr.water() == 2 && wr.air() == 2, "shore: 2 in the sea, 2 in air, none into dry ground");
		check(EarthFill.count(List.of(wworld), shore, new EarthFill.Limits(-1, -1, 1), false).cancelled(), "water: air 2 > limit 1 cancels");
		long[] sunk = longs(new int[]{0, 50, 0}, new int[]{1, 50, 0}, new int[]{3, 50, 0});
		wr = EarthFill.count(List.of(sunk), shore, new EarthFill.Limits(0, -1, -1), false);
		check(wr.ground() == 1 && wr.cancelled(), "water sunk into the seabed is free, into dry ground counts (1 > 0 cancels)");
		check(!EarthFill.count(List.of(longs(new int[]{0, 50, 0})), shore, new EarthFill.Limits(0, 0, 0), false).cancelled(), "seabed only: nothing to cancel at 0/0/0");
		check(!EarthFill.count(List.of(pad), (x, y, z) -> EarthFill.Kind.SEABED, earthLimits(0, 0), false).cancelled(), "earth fill treats a seabed as solid");

		// 6. seabed: a column's top non-air block decides
		LevelHeightAccessor height = LevelHeightAccessor.create(-64, 384);
		BlockState[] sea = new BlockState[384], land = new BlockState[384], empty = new BlockState[384];
		for (int i = 0; i < 384; i++) {
			int y = i - 64;
			sea[i] = y < 40 ? Blocks.STONE.defaultBlockState() : y < 63 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
			land[i] = y < 20 ? Blocks.STONE.defaultBlockState() : y < 30 ? Blocks.WATER.defaultBlockState()   // an aquifer under the land
					: y < 70 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
			empty[i] = Blocks.AIR.defaultBlockState();
		}
		check(EarthFill.topIsWater(new NoiseColumn(-64, sea), height), "an ocean column is topped by water");
		check(!EarthFill.topIsWater(new NoiseColumn(-64, land), height), "a land column is not, even with an aquifer below");
		check(!EarthFill.topIsWater(new NoiseColumn(-64, empty), height), "an empty column is not");

		// 7. the packed column (shared terrain cache) reads exactly as the NoiseColumn it was built from
		BlockState[] palette = {Blocks.AIR.defaultBlockState(), Blocks.CAVE_AIR.defaultBlockState(), Blocks.WATER.defaultBlockState(),
				Blocks.STONE.defaultBlockState(), Blocks.KELP.defaultBlockState(), Blocks.LAVA.defaultBlockState(),
				Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true)};
		java.util.Random rnd = new java.util.Random(42);
		int bad = 0;
		for (int n = 0; n < 2000; n++) {
			// some columns are narrower than the level (noise settings above its bottom), all have runs like real terrain
			int minY = n % 5 == 0 ? -32 : -64, len = n % 5 == 0 ? 300 : 384;
			BlockState[] states = new BlockState[len];
			int surface = rnd.nextInt(len + 1);
			for (int i = 0; i < len; i++) {
				BlockState s = palette[rnd.nextInt(palette.length)];
				states[i] = i >= surface ? (rnd.nextInt(4) == 0 ? s : palette[0]) : s;
			}
			if (n % 7 == 0) java.util.Arrays.fill(states, palette[0]);
			NoiseColumn noise = new NoiseColumn(minY, states);
			EarthFill.Column packed = new EarthFill.Column(noise, height);
			boolean topWater = EarthFill.topIsWater(noise, height);
			if (packed.top != EarthFill.topOf(noise, height) || packed.topWater != topWater) bad++;
			for (int y = -70; y < 330; y++) {
				BlockState s = noise.getBlock(y);
				EarthFill.Kind want = s.isAir() ? EarthFill.Kind.AIR : EarthFill.isWater(s) ? EarthFill.Kind.WATER
						: topWater ? EarthFill.Kind.SEABED : EarthFill.Kind.SOLID;
				if (packed.at(y) != want) bad++;
			}
		}
		check(bad == 0, "packed columns read as their NoiseColumn: top, top water and every y (2000 random columns, " + bad + " mismatches)");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
