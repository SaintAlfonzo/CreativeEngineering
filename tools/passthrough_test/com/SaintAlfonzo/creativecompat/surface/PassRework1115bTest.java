package com.SaintAlfonzo.creativecompat.surface;

import static com.SaintAlfonzo.creativecompat.surface.PassThroughTest.check;
import static com.SaintAlfonzo.creativecompat.surface.PassThroughTest.near;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.surface.PassThroughTest.World;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Pass-through rework (feature_writeup/plan_post_scratch_1115b.md): A fall distance in lone, stacked and dense webs;
 * B the stall break; C the 4 / 6 neighbor model, the baked chunk counts and what invalidates them, the growth rule
 * against neighbors, the config migration and the sync format; D the layer height rule. Called from
 * {@link PassThroughTest#main}.
 */
final class PassRework1115bTest {

	private PassRework1115bTest() {}

	static void run() {
		fallA();
		stallB();
		countDataC();
		invalidationC();
		growthC();
		migrationC();
		syncC();
		layersD();
	}

	static final BlockState WEB = Blocks.COBWEB.defaultBlockState();

	/** The fall-distance factor over the ticks of a fall through the cells {@code ys} (one new layer per tick). */
	static double fallThrough(World w, int... ys) {
		PassState st = new PassState();
		double f = 1.0;
		for (int i = 0; i < ys.length; i++) {
			st.result.reset();
			for (int j = Math.max(0, i - 1); j <= i; j++) {
				BlockPos p = new BlockPos(0, ys[j], 0);
				st.result.add(p.asLong(), PassThrough.cellAt(w, p, WEB, PassValues.defaults()), true, true);
			}
			f *= PassThrough.newCellsFallFactor(st, st.result);
			st.rememberCells();
		}
		return f;
	}

	static void fallA() {
		PassThrough.seedTier(Blocks.COBWEB, PassTier.COBWEB);
		World lone = new World().put(0, 10, 0, WEB);
		check("A: lone web scales the fall distance x0.85, no reset", near(fallThrough(lone, 10), 0.85)
				&& !PassThrough.cellAt(lone, new BlockPos(0, 10, 0), WEB, PassValues.defaults()).resetFall());
		World stack = new World().put(0, 10, 0, WEB).put(0, 9, 0, WEB).put(0, 8, 0, WEB);
		check("A: 3 stacked webs compound to x0.614 (1 or 2 face neighbors each: not dense)",
				near(fallThrough(stack, 10, 9, 8), 0.614125)
						&& !PassThrough.cellAt(stack, new BlockPos(0, 9, 0), WEB, PassValues.defaults()).resetFall());
		check("A: stacked webs left out of vanilla's fast-fall reset", !PassFixesTest.fixedHit(stack, 11.2, 7.5));
		World dense = new World().put(0, 10, 0, WEB).put(1, 10, 0, WEB).put(-1, 10, 0, WEB).put(0, 9, 0, WEB);
		PassRule.Cell d = PassThrough.cellAt(dense, new BlockPos(0, 10, 0), WEB, PassValues.defaults());
		check("A: a web with 3 of 6 face neighbors is dense and resets (denseResetsFall)", d.dense() && d.resetFall()
				&& d.neighbours() == 3 && PassFixesTest.fixedHit(dense, 11.2, 9.9));
		World two = new World().put(0, 10, 0, WEB).put(1, 10, 0, WEB).put(-1, 10, 0, WEB);
		PassRule.Cell t = PassThrough.cellAt(two, new BlockPos(0, 10, 0), WEB, PassValues.defaults());
		check("A: 2 of 6 is not dense: scales, no reset", !t.dense() && !t.resetFall() && near(t.fall(), 0.85)
				&& !PassFixesTest.fixedHit(two, 11.2, 9.9));
		// #8: a drop that enters a ground-level web and lands in the same move (PassThrough.beforeLanding): no state
		// yet, every cell is new; the cells counted there are remembered, so the same move's TAIL hook adds nothing
		PassResult landing = new PassResult();
		BlockPos g = new BlockPos(0, 10, 0);
		landing.add(g.asLong(), PassThrough.cellAt(lone, g, WEB, PassValues.defaults()), true, true);
		double before = PassThrough.newCellsFallFactor(null, landing);
		PassState st = new PassState();
		st.rememberCells(landing);
		check("#8: landing in a lone web without a state: x0.85; the TAIL hook then x1.0",
				near(before, 0.85) && near(PassThrough.newCellsFallFactor(st, landing), 1.0));
	}

	static void stallB() {
		PassState st = new PassState();
		double v = 1.44;
		int tick = 0;
		boolean broke = false;
		while (tick < 20 && !broke) {
			tick++;
			v *= 0.35;
			broke = PassProjectiles.stallBreaks(st, 3, v * v);
		}
		check("B: SQ-PTH-11 snowball (x0.35 a tick) stalls on web tick 4, breaks on tick 6 at breakStallTicks 3 (" + tick + ")",
				broke && tick == 6);
		st = new PassState();
		boolean any = false;
		for (int i = 0; i < 50; i++) any |= PassProjectiles.stallBreaks(st, 0, 0.0);
		check("B: breakStallTicks 0 = off", !any);
		st = new PassState();
		PassProjectiles.stallBreaks(st, 3, 0.01 * 0.01);
		PassProjectiles.stallBreaks(st, 3, 0.01 * 0.01);
		boolean moved = PassProjectiles.stallBreaks(st, 3, 0.2 * 0.2);
		boolean after = PassProjectiles.stallBreaks(st, 3, 0.01 * 0.01);
		check("B: moving again starts the count over", !moved && !after && st.stallTicks == 1);
		st = new PassState();
		int hover = 0;
		while (!PassProjectiles.stallBreaks(st, 3, 0.016 * 0.016)) hover++;
		check("B: a pearl hovering at 0.016 b/t breaks on its 3rd stalled tick", hover == 2);
		check("B: default breakStallTicks 3", PassValues.defaults().breakStallTicks == 3);
	}

	static void countDataC() {
		PassCountData d = new PassCountData();
		int k = PassCountData.key(17, 70, -3, -64);
		check("C2 key: chunk-local x 1, z 13, y 70", PassCountData.keyX(k) == 1 && PassCountData.keyZ(k) == 13
				&& PassCountData.keyY(k, -64) == 70);
		check("C2 data: empty -> -1", d.get(k, true) == -1);
		d.put(k, true, 6);
		check("C2 data: baked count read back, no recount", d.get(k, true) == 6);
		check("C2 data: other shape misses (SIDES vs FACES)", d.get(k, false) == -1);
		d.remove(k);
		check("C2 data: dropped on remove", d.get(k, true) == -1);
		int west = PassCountData.key(0, 64, 5, -64), east = PassCountData.key(15, 64, 5, -64), mid = PassCountData.key(7, 64, 7, -64);
		d.put(west, false, 2);
		d.put(east, false, 3);
		d.put(mid, false, 4);
		d.dropEdge(0);
		check("C2 data: west edge dropped, east and middle kept", d.get(west, false) == -1 && d.get(east, false) == 3
				&& d.get(mid, false) == 4);
		d.dropEdges();
		check("C2 data: all edges dropped, middle kept", d.get(east, false) == -1 && d.get(mid, false) == 4);
		int[] saved = d.save(x -> true);
		PassCountData back = new PassCountData();
		back.load(PassCountData.tagVersion(), saved);
		check("C2 data: save / load round trip", back.get(mid, false) == 4 && back.size() == 1);
		back.load(PassCountData.tagVersion() + 1, saved);
		check("C2 data: other tag version -> dropped on load", back.get(mid, false) == -1);
		check("C2 data: save leaves out a block that is gone", d.save(x -> false).length == 0);
		d.put(mid, false, 4);
		PassCountData.forgetAll();
		check("C2 data: forgetAll (tag reload) -> recount", d.get(mid, false) == -1);
		int ver = PassCountData.tagVersion();
		d.put(mid, false, 4);
		PassCountData.setTagVersion(ver + 7);
		check("C2 data: tag hash change -> recount", d.get(mid, false) == -1);
		PassCountData.setTagVersion(ver);
		// sparse: a field-sized chunk (16 x 16 crops) is 256 ints
		PassCountData field = new PassCountData();
		for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) field.put(PassCountData.key(x, 64, z, -64), false, 4);
		check("C2 data: a full field chunk saves 256 entries", field.save(x -> true).length == 256);
	}

	static void invalidationC() {
		PassThrough.seedTier(Blocks.WHEAT, PassTier.DENSE_CROP);
		PassThrough.seedTier(Blocks.CARROTS, PassTier.DENSE_CROP);
		PassThrough.seedTier(Blocks.POTATOES, null);
		PassThrough.seedTier(Blocks.AIR, null);
		PassThrough.seedTier(Blocks.STONE, null);
		PassThrough.seedTier(Blocks.DIRT, null);
		BlockState grown = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
		BlockState young = Blocks.WHEAT.defaultBlockState();
		check("C2: harvest (wheat 7 -> 0) keeps the counts", !PassCounts.changesCounts(grown, young));
		check("C2: growth (wheat 0 -> 7) keeps the counts", !PassCounts.changesCounts(young, grown));
		check("C2: break (wheat -> air) drops them", PassCounts.changesCounts(grown, Blocks.AIR.defaultBlockState()));
		check("C2: plant (air -> wheat) drops them", PassCounts.changesCounts(Blocks.AIR.defaultBlockState(), young));
		check("C2: same tier swap (wheat -> carrots, both dense_crop) keeps them",
				!PassCounts.changesCounts(young, Blocks.CARROTS.defaultBlockState()));
		check("C2: tier change (wheat -> untiered potatoes) drops them",
				PassCounts.changesCounts(young, Blocks.POTATOES.defaultBlockState()));
		check("C2: untiered change (stone -> dirt) keeps them",
				!PassCounts.changesCounts(Blocks.STONE.defaultBlockState(), Blocks.DIRT.defaultBlockState()));
		check("C2: nothing changed (null old) keeps them", !PassCounts.changesCounts(null, young));
		World w = new World().put(0, 0, 0, grown).put(1, 0, 0, grown).put(-1, 0, 0, grown).put(0, 0, 1, grown);
		check("C2: a plain BlockGetter counts directly (= neighbors)",
				PassCounts.count(w, BlockPos.ZERO, PassTier.DENSE_CROP, PassTier.Neighbours.SIDES) == 3);
	}

	static void growthC() {
		PassThrough.seedTier(Blocks.WHEAT, PassTier.DENSE_CROP);
		BlockState grown = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
		BlockState young = Blocks.WHEAT.defaultBlockState();
		World w = new World().put(0, 0, 0, grown).put(1, 0, 0, young).put(-1, 0, 0, young).put(0, 0, 1, young);
		PassRule.Cell g = PassThrough.cellAt(w, BlockPos.ZERO, grown, PassValues.defaults());
		check("C2 growth: a grown crop with 3 freshly planted neighbors is dense (0.50)",
				g != null && g.dense() && near(g.walk(), 0.50) && g.neighbours() == 3);
		check("C2 growth: the young neighbor itself is not slowed",
				PassThrough.cellAt(w, new BlockPos(1, 0, 0), young, PassValues.defaults()) == null);
		World two = new World().put(0, 0, 0, grown).put(1, 0, 0, grown).put(-1, 0, 0, grown);
		PassRule.Cell t = PassThrough.cellAt(two, BlockPos.ZERO, grown, PassValues.defaults());
		check("C1: dense crop with 2 of 4 side neighbours: 0.75; diagonals never count",
				t != null && !t.dense() && near(t.walk(), 0.75));
	}

	static final String OLD = """
            [surface]
            \t[surface.passthrough]
            \t\tenabled = true
            \t\t#Crops grown in thick fields.
            \t\t[surface.passthrough.dense_crop]
            \t\t\twalk = 0.75
            \t\t\t#Density count: NONE, RING (8 around, same layer) or CUBE (26 around, 3x3x3).
            \t\t\t#Allowed Values: NONE, RING, CUBE
            \t\t\tneighbours = "RING"
            \t\t\tdenseFrom = 6
            \t\t\tminLayers = 1

            \t\t#Cobwebs.
            \t\t[surface.passthrough.cobweb]
            \t\t\tneighbours = "CUBE"
            \t\t\tdenseFrom = %s

            \t\t#Immersive Weathering leaf piles, Twigs bamboo leaves.
            \t\t[surface.passthrough.leaf_pile]
            \t\t\twalk = %s
            \t\t\tminLayers = %s
            \t\t\tdepthStriderPasses = %s

            \t\t#Short grass.
            \t\t[surface.passthrough.grass]
            \t\t\twalk = 1.0
            """;

	static Object at(String text, String... path) {
		return new TomlParser().parse(text).get(List.of(path));
	}

	static void migrationC() {
		PassConfigMigration.Result r = PassConfigMigration.migrate(OLD.formatted("6", "0.45", "4", "true"));
		String t = r.text();
		check("C1 migrate: RING -> SIDES, CUBE -> FACES, denseFrom 6 -> 3",
				r.changed() && "SIDES".equals(at(t, "surface", "passthrough", "dense_crop", "neighbours"))
						&& "FACES".equals(at(t, "surface", "passthrough", "cobweb", "neighbours"))
						&& Integer.valueOf(3).equals(at(t, "surface", "passthrough", "dense_crop", "denseFrom"))
						&& Integer.valueOf(3).equals(at(t, "surface", "passthrough", "cobweb", "denseFrom")));
		check("D migrate: leaf_pile table and its comment gone, defaults not carried (no layers table added)",
				!t.contains("leaf_pile") && !t.contains("Immersive Weathering leaf piles") && !t.contains("[surface.passthrough.layers]")
						&& t.contains("[surface.passthrough.grass]"));
		CommentedConfig cfg = new TomlParser().parse(t);
		CompatConfig.SPEC.correct(cfg);
		check("C1 migrate: Forge's correction then keeps SIDES / 3 and adds surroundedAt 6",
				"SIDES".equals(String.valueOf((Object) cfg.get(List.of("surface", "passthrough", "dense_crop", "neighbours"))))
						&& Integer.valueOf(3).equals(cfg.get(List.of("surface", "passthrough", "dense_crop", "denseFrom")))
						&& Integer.valueOf(6).equals(cfg.get(List.of("surface", "passthrough", "cobweb", "surroundedAt"))));
		r = PassConfigMigration.migrate(OLD.formatted("5", "0.3", "6", "false"));
		t = r.text();
		check("C1 migrate: an edited denseFrom (5) stays", Integer.valueOf(5).equals(at(t, "surface", "passthrough", "cobweb", "denseFrom")));
		check("D migrate: edited leaf_pile values carried into layers (minLayers 6, leafMaxSlow 0.3, depth strider off)",
				Integer.valueOf(6).equals(at(t, "surface", "passthrough", "layers", "minLayers"))
						&& Double.valueOf(0.3).equals(at(t, "surface", "passthrough", "layers", "leafMaxSlow"))
						&& Boolean.FALSE.equals(at(t, "surface", "passthrough", "layers", "depthStriderPasses")));
		cfg = new TomlParser().parse(t);
		CompatConfig.SPEC.correct(cfg);
		check("D migrate: Forge's correction keeps the carried layers values",
				Integer.valueOf(6).equals(cfg.get(List.of("surface", "passthrough", "layers", "minLayers")))
						&& Double.valueOf(0.3).equals(cfg.get(List.of("surface", "passthrough", "layers", "leafMaxSlow"))));
		// a 1115b D file (layerMaxSlow, solidBelowFull, overrides): Forge's correction drops the old keys, adds the new
		cfg = new TomlParser().parse("[surface.passthrough.layers]\n\tlayerMaxSlow = 0.3\n\tsolidBelowFull = 2\n"
				+ "\tsolidBelowFullOverrides = [\"twigs:bamboo_leaves=0\"]\n\tminLayers = 3\n");
		CompatConfig.SPEC.correct(cfg);
		check("G7.4 migrate: Forge drops layerMaxSlow / solidBelowFull / overrides, keeps minLayers 3, adds snowMaxSlow 0.4",
				!cfg.contains(List.of("surface", "passthrough", "layers", "layerMaxSlow"))
						&& !cfg.contains(List.of("surface", "passthrough", "layers", "solidBelowFull"))
						&& !cfg.contains(List.of("surface", "passthrough", "layers", "solidBelowFullOverrides"))
						&& Integer.valueOf(3).equals(cfg.get(List.of("surface", "passthrough", "layers", "minLayers")))
						&& Double.valueOf(0.4).equals(cfg.get(List.of("surface", "passthrough", "layers", "snowMaxSlow"))));
		// after Forge corrects it (surroundedAt present, no leaf_pile), a second run does nothing
		com.electronwill.nightconfig.toml.TomlWriter wr = new com.electronwill.nightconfig.toml.TomlWriter();
		java.io.StringWriter sw = new java.io.StringWriter();
		wr.write(cfg, sw);
		String corrected = sw.toString();
		check("C1 migrate: a corrected file is left alone (one-time)", !PassConfigMigration.migrate(corrected).changed());
		check("C1 migrate: a table with surroundedAt keeps denseFrom 6",
				!PassConfigMigration.migrate("[surface.passthrough.cobweb]\n\tsurroundedAt = 6\n\tdenseFrom = 6\n").changed());
	}

	static void syncC() {
		Map<PassTier, PassTier.Values> m = new EnumMap<>(PassTier.class);
		for (PassTier t : PassTier.values()) m.put(t, t.defaults());
		m.put(PassTier.LAYERS, PassTier.LAYERS.defaults().layers(0.3, 0.7));
		PassValues v = new PassValues(true, 12000, 128, 5, false, true, m);
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		v.write(buf);
		PassValues back = PassValues.read(buf);
		check("G7.4 sync: format 3 round trip (breakStallTicks, snow switches, snow / leaf max slow)",
				PassValues.FORMAT == 3 && back.equals(v) && back.breakStallTicks == 5 && !back.snowWalkThrough
						&& back.snowFullBecomesBlock && near(back.tier(PassTier.LAYERS).snowMaxSlow(), 0.3)
						&& near(back.tier(PassTier.LAYERS).leafMaxSlow(), 0.7) && buf.readableBytes() == 0);
		FriendlyByteBuf old = new FriendlyByteBuf(Unpooled.buffer());
		old.writeVarInt(1);
		old.writeBoolean(true);
		old.writeVarInt(12000);
		PassValues mis = PassValues.read(old);
		check("C1 sync: another format -> system off on the client, buffer consumed", !mis.enabled && old.readableBytes() == 0);
	}

	static void layersD() {
		// G7.4: snow (closed) H = max - 1, snowMaxSlow 0.4 at 7 of 8
		double[] snow = {1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4};
		boolean ok = true;
		for (int l = 1; l <= 7; l++) ok &= near(PassRule.layerWalk(l, 8, false, 0.4), snow[l - 1]);
		check("G7.4: snow 1 to 7 of 8: 1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4", ok);
		check("G7.4: snow 8 of 8 acts whole (becomes a snow block)", Double.isNaN(PassRule.layerWalk(8, 8, false, 0.4))
				&& PassRule.highestWalkThrough(8, false) == 7);
		// leaf layers (open) H = max, leafMaxSlow 0.6 at full
		check("G7.4: leaf pile 8 of 8 walk-through at 0.6, 2 of 8 at 1 - 0.4 / 7",
				near(PassRule.layerWalk(8, 8, true, 0.6), 0.6) && near(PassRule.layerWalk(2, 8, true, 0.6), 1.0 - 0.4 / 7.0)
						&& PassRule.highestWalkThrough(8, true) == 8);
		check("G7.4: bamboo leaves (max 4, open): 2 -> 0.867, 3 -> 0.733, 4 -> 0.6",
				near(PassRule.layerWalk(2, 4, true, 0.6), 1.0 - 0.4 / 3.0)
						&& near(PassRule.layerWalk(3, 4, true, 0.6), 1.0 - 0.8 / 3.0)
						&& near(PassRule.layerWalk(4, 4, true, 0.6), 0.6));
		check("G7.4: one layer never slows; H under 2 never slows", near(PassRule.layerWalk(1, 8, true, 0.6), 1.0)
				&& Double.isNaN(PassRule.layerWalk(1, 2, false, 0.4)) && Double.isNaN(PassRule.layerWalk(2, 2, false, 0.4)));
		check("G7.4: snowMaxSlow 0.3 at 7", near(PassRule.layerWalk(7, 8, false, 0.3), 0.3));

		// through the block: snow layers in the layer tier (no tags headless: snow is not a leaf layer)
		PassThrough.seedTier(Blocks.SNOW, PassTier.LAYERS);
		World w = new World();
		PassValues d = PassValues.defaults();
		PassRule.Cell five = PassThrough.cellAt(w, BlockPos.ZERO, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 5), d);
		check("G7.4: 5-layer snow -> walk 0.60, fall 1.0, no density", five != null && near(five.walk(), 0.6)
				&& near(five.fall(), 1.0) && !five.dense());
		PassRule.Cell seven = PassThrough.cellAt(w, BlockPos.ZERO, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 7), d);
		check("G7.4: 7-layer snow walk-through at 0.40; 8 acts whole; 1 layer below minLayers 2 (no rule)",
				seven != null && near(seven.walk(), 0.4)
						&& PassThrough.cellAt(w, BlockPos.ZERO, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 8), d) == null
						&& PassThrough.cellAt(w, BlockPos.ZERO, Blocks.SNOW.defaultBlockState(), d) == null);
		check("G7.4: layers(state) = {5, 8}", java.util.Arrays.equals(PassThrough.layers(
				Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 5)), new int[]{5, 8}));
		List<String> t = PassThroughFormat.lines(five, PassTier.LAYERS.defaults(), 1.0, true, 5, 8).stream()
				.map(l -> PassThroughFormat.english(l, false, null, null)).toList();
		check("G7.4: WTHIT line 'Walk Through: 0.60x (5 of 8 layers)' " + t, t.equals(List.of("Walk Through: 0.60× (5 of 8 layers)")));

		// snow walk-through and the full layer -> snow block (SnowLayers)
		Map<PassTier, PassTier.Values> m = new EnumMap<>(PassTier.class);
		for (PassTier tt : PassTier.values()) m.put(tt, tt.defaults());
		PassValues off = new PassValues(true, 12000, 128, 3, false, false, m);
		BlockState s7 = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 7);
		BlockState s8 = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 8);
		check("G7.4: snow 1 to 7 walk-through, 8 not; switch off or system off: none",
				SnowLayers.walkThrough(s7, d) && SnowLayers.walkThrough(Blocks.SNOW.defaultBlockState(), d)
						&& !SnowLayers.walkThrough(s8, d) && !SnowLayers.walkThrough(s7, off)
						&& !SnowLayers.walkThrough(s7, PassValues.mismatch()));
		PassThrough.seedTier(Blocks.SNOW, null);
		check("G7.4: a snow layer outside the layer tier keeps vanilla collision", !SnowLayers.walkThrough(s7, d));
		PassThrough.seedTier(Blocks.SNOW, PassTier.LAYERS);
		check("G7.4: snow 8 -> snow block; 7 stays; switch off: 8 stays; snow block unchanged",
				SnowLayers.fullToBlock(s8, d).is(Blocks.SNOW_BLOCK) && SnowLayers.fullToBlock(s7, d) == s7
						&& SnowLayers.fullToBlock(s8, off) == s8
						&& SnowLayers.fullToBlock(Blocks.SNOW_BLOCK.defaultBlockState(), d).is(Blocks.SNOW_BLOCK));
	}
}
