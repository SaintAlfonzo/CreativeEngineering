package com.SaintAlfonzo.creativecompat.surface;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.electronwill.nightconfig.core.CommentedConfig;

import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import sun.misc.Unsafe;

/**
 * Headless checks for pass-through phase 1 (plan_passthrough.md 8, Phase 1 tests). Run by tools/test_passthrough.sh
 * against development/creativeCompat/build/classes (build first). In the surface package for package-private access.
 */
public final class PassThroughTest {

	static int failed, passed;

	static void check(String what, boolean ok) {
		if (ok) {
			passed++;
			System.out.println("ok   " + what);
		} else {
			failed++;
			System.out.println("FAIL " + what);
		}
	}

	static boolean near(double a, double b) {
		return Math.abs(a - b) < 1.0E-9;
	}

	/** A block world from a map; everything else is air. */
	static class World implements BlockGetter {
		final Map<BlockPos, BlockState> blocks = new HashMap<>();

		World put(int x, int y, int z, BlockState s) {
			blocks.put(new BlockPos(x, y, z), s);
			return this;
		}

		@Override public BlockEntity getBlockEntity(BlockPos pos) { return null; }
		@Override public BlockState getBlockState(BlockPos pos) { return blocks.getOrDefault(pos.immutable(), Blocks.AIR.defaultBlockState()); }
		@Override public FluidState getFluidState(BlockPos pos) { return Fluids.EMPTY.defaultFluidState(); }
		@Override public int getHeight() { return 384; }
		@Override public int getMinBuildHeight() { return -64; }
	}

	enum Hemp implements StringRepresentable {
		BOTTOM0, BOTTOM1, BOTTOM2, BOTTOM3, BOTTOM4, TOP0;
		@Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
	}

	static final Unsafe U;
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			U = (Unsafe) f.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** An unregistered block with these properties (Corn Delight / IE hemp stand-ins; mods are not on the classpath). */
	static Block fakeBlock(net.minecraft.world.level.block.state.properties.Property<?>... props) throws Exception {
		Block b = (Block) U.allocateInstance(Block.class);
		Field pf = BlockBehaviour.class.getDeclaredField("properties");
		pf.setAccessible(true);
		U.putObject(b, U.objectFieldOffset(pf), BlockBehaviour.Properties.of(Material.PLANT));
		StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(b);
		builder.add(props);
		StateDefinition<Block, BlockState> def = builder.create(Block::defaultBlockState, BlockState::new);
		Field sf = Block.class.getDeclaredField("stateDefinition");
		U.putObject(b, U.objectFieldOffset(sf), def);
		Field df = Block.class.getDeclaredField("defaultBlockState");
		U.putObject(b, U.objectFieldOffset(df), def.any());
		return b;
	}

	public static void main(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		tierDefaults();
		rules();
		neighbourCounts();
		growth();
		layers();
		fallDistance();
		modifierMath();
		stickPoint();
		stickAlong();
		surrounded();
		arrowDespawn();
		projectileGroups();
		exemptTags();
		config();
		PassPhase35Test.run(java.nio.file.Path.of("").toAbsolutePath());
		PassFixesTest.run();
		PassRework1115bTest.run();

		System.out.println(failed == 0 ? "ALL " + passed + " OK" : failed + " FAILED of " + (passed + failed));
		System.exit(failed == 0 ? 0 : 1);
	}

	static void tierDefaults() {
		check("eleven tiers", PassTier.values().length == 11);
		PassTier.Values dc = PassTier.DENSE_CROP.defaults();
		check("dense_crop 0.75 / dense 0.50 at 3, sides, tall_half", near(dc.walk(), 0.75) && near(dc.denseWalk(), 0.50)
				&& dc.denseFrom() == 3 && dc.neighbours() == PassTier.Neighbours.SIDES && dc.growth() == PassTier.Growth.TALL_HALF);
		PassTier.Values w = PassTier.COBWEB.defaults();
		check("cobweb 0.50/0.85, dense 0.35/0.20 from 3, faces, surrounded at 6, reset, web, stick 0.35, projectiles",
				near(w.walk(), 0.50) && near(w.fall(), 0.85) && near(w.denseWalk(), 0.35) && near(w.denseFall(), 0.20)
						&& w.denseFrom() == 3 && w.neighbours() == PassTier.Neighbours.FACES && w.surroundedAt() == 6
						&& w.surroundedCount() == 6 && w.denseResetsFall()
						&& w.surrounded() == PassTier.Surround.WEB && w.items() == PassTier.ItemRule.STICK
						&& near(w.itemStickChance(), 0.35) && w.projectiles());
		check("crop half", PassTier.CROP.defaults().growth() == PassTier.Growth.HALF && near(PassTier.CROP.defaults().walk(), 0.75));
		check("cane 0.50", near(PassTier.CANE.defaults().walk(), 0.50));
		check("bush 0.36", near(PassTier.BUSH.defaults().walk(), 0.36));
		PassTier.Values lv = PassTier.LAYERS.defaults();
		check("layers: minLayers 2, layerMaxSlow 0.5, solidBelowFull 2, depth strider, tag passthrough_layers, not inert",
				lv.minLayers() == 2 && near(lv.layerMaxSlow(), 0.5) && lv.solidBelowFull() == 2 && lv.depthStriderPasses()
						&& PassTier.LAYERS.tagPath().equals("passthrough_layers") && PassTier.LAYERS.layered()
						&& !lv.inert() && PassTier.COBWEB.tagPath().equals("passthrough/cobweb"));
		check("vine walkOnlyMoving", PassTier.VINE.defaults().walkOnlyMoving() && near(PassTier.VINE.defaults().walk(), 0.75));
		check("rope 0.75, no walk while climbing", near(PassTier.ROPE.defaults().walk(), 0.75) && PassTier.ROPE.noWalkWhileClimbing()
				&& !PassTier.VINE.noWalkWhileClimbing());
		check("grass inert, others not", PassTier.GRASS.defaults().inert() && !PassTier.CROP.defaults().inert()
				&& !PassTier.COBWEB.defaults().inert());
		check("title", PassTier.DENSE_CROP.title().equals("Dense Crop"));
	}

	static void rules() {
		PassTier.Values dc = PassTier.DENSE_CROP.defaults();
		for (int n : new int[]{0, 2, 3, 4}) {
			PassRule.Cell c = PassRule.cell(PassTier.DENSE_CROP, dc, n);
			boolean dense = n >= 3;
			check("dense_crop sides " + n + " of 4: walk " + c.walk() + (c.walkDense() ? " dense" : ""),
					near(c.walk(), dense ? 0.50 : 0.75) && c.walkDense() == dense && near(c.fall(), 1.0)
							&& !c.fallDense() && !c.surrounded() && !c.resetFall() && c.neighbours() == n);
		}
		check("dense_crop 4 of 4 is not surrounded (tier surrounded = none)", !PassRule.cell(PassTier.DENSE_CROP, dc, 4).surrounded());
		PassTier.Values w = PassTier.COBWEB.defaults();
		for (int n : new int[]{0, 2, 3, 5, 6}) {
			PassRule.Cell c = PassRule.cell(PassTier.COBWEB, w, n);
			boolean dense = n >= 3;
			check("cobweb faces " + n + " of 6: walk " + c.walk() + " fall " + c.fall() + (c.surrounded() ? " surrounded" : ""),
					near(c.walk(), dense ? 0.35 : 0.50) && near(c.fall(), dense ? 0.20 : 0.85) && c.dense() == dense
							&& c.walkDense() == dense && c.fallDense() == dense && c.resetFall() == dense
							&& c.surrounded() == (n == 6));
		}
		PassRule.Cell crop = PassRule.cell(PassTier.CROP, PassTier.CROP.defaults(), 4);
		check("crop ignores neighbors", near(crop.walk(), 0.75) && crop.neighbours() == 0 && !crop.dense());
		check("needsCount", PassRule.needsCount(dc) && !PassRule.needsCount(PassTier.CROP.defaults()));

		// the lowest walk and the lowest fall win, each with its own density
		PassResult r = new PassResult();
		r.add(1L, PassRule.cell(PassTier.DENSE_CROP, dc, 4), true, false);
		r.add(2L, PassRule.cell(PassTier.COBWEB, w, 2), true, true);
		check("feet in a dense field, head in a lone web: walk 0.50 (dense crop), fall 0.85 (web)",
				near(r.walk, 0.50) && r.walkTier == PassTier.DENSE_CROP && r.walkDense && r.walkNeighbours == 4
						&& near(r.fall, 0.85) && r.fallTier == PassTier.COBWEB && !r.fallDense && r.projectiles && r.cells() == 2);
		r.add(3L, PassRule.cell(PassTier.COBWEB, w, 6), true, true);
		check("a surrounded web cell marks the result", r.surrounded && r.surroundedCell == 3L && r.resetFall
				&& near(r.walk, 0.35) && near(r.fall, 0.20));
		r.reset();
		r.add(4L, PassRule.cell(PassTier.ROPE, PassTier.ROPE.defaults(), 0), false, false);
		check("rope while climbing: no walk", near(r.walk, 1.0) && r.any());
	}

	static void neighbourCounts() {
		PassThrough.seedTier(Blocks.WHEAT, PassTier.DENSE_CROP);
		PassThrough.seedTier(Blocks.COBWEB, PassTier.COBWEB);
		PassThrough.seedTier(Blocks.STONE, null);
		BlockState wheat = Blocks.WHEAT.defaultBlockState();
		World field = new World();
		for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) field.put(x, 0, z, wheat);
		PassTier.Neighbours SIDES = PassTier.Neighbours.SIDES, FACES = PassTier.Neighbours.FACES;
		check("sides: inside 4", PassThrough.neighbours(field, new BlockPos(2, 0, 2), PassTier.DENSE_CROP, SIDES) == 4);
		check("sides: edge 3", PassThrough.neighbours(field, new BlockPos(0, 0, 2), PassTier.DENSE_CROP, SIDES) == 3);
		check("sides: corner 2", PassThrough.neighbours(field, new BlockPos(0, 0, 0), PassTier.DENSE_CROP, SIDES) == 2);
		field.put(1, 1, 1, wheat).put(1, -1, 1, wheat);
		check("sides ignore other layers", PassThrough.neighbours(field, new BlockPos(1, 0, 1), PassTier.DENSE_CROP, SIDES) == 4);
		check("sides ignore diagonals: a 3x3 field's middle has 4, not 8",
				PassThrough.neighbours(field, new BlockPos(1, 0, 1), PassTier.DENSE_CROP, SIDES) == 4);
		field.put(3, 0, 2, Blocks.STONE.defaultBlockState());
		check("sides: a solid block does not count", PassThrough.neighbours(field, new BlockPos(2, 0, 2), PassTier.DENSE_CROP, SIDES) == 3);
		field.put(3, 0, 2, Blocks.COBWEB.defaultBlockState());
		check("sides: another tier does not count", PassThrough.neighbours(field, new BlockPos(2, 0, 2), PassTier.DENSE_CROP, SIDES) == 3);
		check("old name RING counts as SIDES", PassThrough.neighbours(field, new BlockPos(2, 0, 2), PassTier.DENSE_CROP, PassTier.Neighbours.RING) == 3
				&& PassTier.Neighbours.RING.canonical() == SIDES && PassTier.Neighbours.CUBE.canonical() == FACES
				&& !PassTier.Neighbours.CONFIG.contains(PassTier.Neighbours.RING));

		World cube = new World();
		BlockState web = Blocks.COBWEB.defaultBlockState();
		for (int x = 0; x < 3; x++) for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) cube.put(x, y, z, web);
		check("faces: center of 3x3x3 = 6", PassThrough.neighbours(cube, new BlockPos(1, 1, 1), PassTier.COBWEB, FACES) == 6);
		check("faces: corner = 3", PassThrough.neighbours(cube, new BlockPos(0, 0, 0), PassTier.COBWEB, FACES) == 3);
		cube.blocks.remove(new BlockPos(0, 0, 0));
		check("faces: a missing corner does not count (still 6)", PassThrough.neighbours(cube, new BlockPos(1, 1, 1), PassTier.COBWEB, FACES) == 6);
		cube.blocks.remove(new BlockPos(1, 2, 1));
		check("faces: open above = 5", PassThrough.neighbours(cube, new BlockPos(1, 1, 1), PassTier.COBWEB, FACES) == 5);
		check("none = 0", PassThrough.neighbours(cube, new BlockPos(1, 1, 1), PassTier.COBWEB, PassTier.Neighbours.NONE) == 0);
		World lone = new World().put(0, 0, 0, web);
		PassRule.Cell c = PassThrough.cellAt(lone, BlockPos.ZERO, web, PassValues.defaults());
		check("cellAt: lone web 0.50 / 0.85, 0 neighbors", c != null && near(c.walk(), 0.50) && near(c.fall(), 0.85) && c.neighbours() == 0);
		check("cellAt: untiered block = null", PassThrough.cellAt(lone, BlockPos.ZERO, Blocks.STONE.defaultBlockState(), PassValues.defaults()) == null);
	}

	static void growth() throws Exception {
		check("halfGrown 0-7: 0 no, 3 no, 4 yes, 7 yes", !PassMath.halfGrown(0, 0, 7) && !PassMath.halfGrown(3, 0, 7)
				&& PassMath.halfGrown(4, 0, 7) && PassMath.halfGrown(7, 0, 7));
		check("halfGrown 0-3 (rice): 1 no, 2 yes", !PassMath.halfGrown(1, 0, 3) && PassMath.halfGrown(2, 0, 3));
		check("halfGrown 0-4 (budding tomatoes): 1 no, 2 yes", !PassMath.halfGrown(1, 0, 4) && PassMath.halfGrown(2, 0, 4));
		check("enumHalfGrown hemp bottom0..4: 0,1 no; 2,3,4 yes", !PassMath.enumHalfGrown(0, 5) && !PassMath.enumHalfGrown(1, 5)
				&& PassMath.enumHalfGrown(2, 5) && PassMath.enumHalfGrown(3, 5) && PassMath.enumHalfGrown(4, 5));

		// one-block crop (vanilla wheat), rule half and tall_half (no upper: same as half)
		World w = new World();
		BlockPos p = new BlockPos(0, 0, 0);
		for (int age : new int[]{0, 3, 4, 7}) {
			BlockState s = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
			boolean want = age >= 4;
			check("wheat age " + age + " half", PassGrowth.passes(PassTier.Growth.HALF, w, p, s, PassTier.DENSE_CROP) == want);
			check("wheat age " + age + " tall_half", PassGrowth.passes(PassTier.Growth.TALL_HALF, w, p, s, PassTier.DENSE_CROP) == want);
			check("wheat age " + age + " any", PassGrowth.passes(PassTier.Growth.ANY, w, p, s, PassTier.DENSE_CROP));
		}
		check("no growth property counts as grown", PassGrowth.passes(PassTier.Growth.HALF, w, p,
				Blocks.TALL_GRASS.defaultBlockState(), PassTier.TALL_PLANT));

		// Corn Delight stand-in: one block, own age 0-7 on each half, bool upper
		IntegerProperty age7 = IntegerProperty.create("age", 0, 7);
		BooleanProperty upper = BooleanProperty.create("upper");
		Block corn = fakeBlock(age7, upper);
		PassThrough.seedTier(corn, PassTier.DENSE_CROP);
		for (int lowerAge : new int[]{0, 3, 4, 7}) {
			World f = new World();
			BlockState lower = corn.defaultBlockState().setValue(age7, lowerAge).setValue(upper, false);
			BlockState top = corn.defaultBlockState().setValue(age7, 0).setValue(upper, true);
			f.put(0, 0, 0, lower).put(0, 1, 0, top);
			boolean want = lowerAge >= 4;
			check("corn lower age " + lowerAge + ": lower " + want,
					PassGrowth.passes(PassTier.Growth.TALL_HALF, f, new BlockPos(0, 0, 0), lower, PassTier.DENSE_CROP) == want);
			check("corn lower age " + lowerAge + ": top (own age 0) looks down, " + want,
					PassGrowth.passes(PassTier.Growth.TALL_HALF, f, new BlockPos(0, 1, 0), top, PassTier.DENSE_CROP) == want);
		}
		BlockState orphanTop = corn.defaultBlockState().setValue(upper, true);
		check("corn top with no lower block counts as grown",
				PassGrowth.passes(PassTier.Growth.TALL_HALF, new World().put(0, 5, 0, orphanTop), new BlockPos(0, 5, 0), orphanTop, PassTier.DENSE_CROP));

		// IE hemp stand-in: growth enum bottom0..bottom4, top0
		EnumProperty<Hemp> growth = EnumProperty.create("growth", Hemp.class);
		Block hemp = fakeBlock(growth);
		PassThrough.seedTier(hemp, PassTier.DENSE_CROP);
		for (Hemp g : new Hemp[]{Hemp.BOTTOM0, Hemp.BOTTOM1, Hemp.BOTTOM2, Hemp.BOTTOM4}) {
			BlockState lower = hemp.defaultBlockState().setValue(growth, g);
			World f = new World().put(0, 0, 0, lower);
			boolean want = g.ordinal() >= 2;
			check("hemp " + g.getSerializedName() + ": " + want,
					PassGrowth.passes(PassTier.Growth.TALL_HALF, f, BlockPos.ZERO, lower, PassTier.DENSE_CROP) == want);
		}
		BlockState hempTop = hemp.defaultBlockState().setValue(growth, Hemp.TOP0);
		World hf = new World().put(0, 0, 0, hemp.defaultBlockState().setValue(growth, Hemp.BOTTOM4)).put(0, 1, 0, hempTop);
		check("hemp top0 over bottom4 looks down: grown", PassGrowth.passes(PassTier.Growth.TALL_HALF, hf, new BlockPos(0, 1, 0), hempTop, PassTier.DENSE_CROP));

		// rice panicles stand-in: a different same-tier block on top of rice (age 0-3) looks down
		IntegerProperty age3 = IntegerProperty.create("age", 0, 3);
		Block rice = fakeBlock(age3);
		Block panicles = fakeBlock(IntegerProperty.create("age", 0, 3));
		PassThrough.seedTier(rice, PassTier.DENSE_CROP);
		PassThrough.seedTier(panicles, PassTier.DENSE_CROP);
		BlockState pan0 = panicles.defaultBlockState();
		World rf = new World().put(0, 0, 0, rice.defaultBlockState().setValue(age3, 3)).put(0, 1, 0, pan0);
		check("rice panicles age 0 on a grown rice base: counts", PassGrowth.passes(PassTier.Growth.TALL_HALF, rf, new BlockPos(0, 1, 0), pan0, PassTier.DENSE_CROP));
		check("rice panicles age 0 under rule half alone: does not", !PassGrowth.passes(PassTier.Growth.HALF, rf, new BlockPos(0, 1, 0), pan0, PassTier.DENSE_CROP));
		BlockState rice1 = rice.defaultBlockState().setValue(age3, 1);
		check("rice base age 1: no; age 2: yes", !PassGrowth.passes(PassTier.Growth.TALL_HALF, new World(), BlockPos.ZERO, rice1, PassTier.DENSE_CROP)
				&& PassGrowth.passes(PassTier.Growth.TALL_HALF, new World(), BlockPos.ZERO, rice.defaultBlockState().setValue(age3, 2), PassTier.DENSE_CROP));
	}

	static void layers() {
		PassThrough.seedTier(Blocks.SNOW, PassTier.LAYERS);
		PassTier.Values v = PassTier.LAYERS.defaults();
		World w = new World();
		for (int n = 1; n <= 8; n++) {
			BlockState s = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, n);
			check("layers " + n + " vs minLayers 2: " + (n >= 2), PassThrough.grown(w, BlockPos.ZERO, s, PassTier.LAYERS, v) == (n >= 2));
		}
		check("minLayers ignored for blocks without layers", PassThrough.grown(w, BlockPos.ZERO, Blocks.COBWEB.defaultBlockState(),
				PassTier.COBWEB, PassTier.COBWEB.defaults()));
		check("layers() = -1 without the property", PassGrowth.layers(Blocks.COBWEB.defaultBlockState()) == -1);
		check("layersMax: snow 8, none -1", PassGrowth.layersMax(Blocks.SNOW.defaultBlockState()) == 8
				&& PassGrowth.layersMax(Blocks.COBWEB.defaultBlockState()) == -1);
	}

	static void fallDistance() {
		check("1 web: x0.85", near(PassMath.fallDistanceAfter(10, 0.85), 8.5));
		check("3 webs: about x0.61", near(PassMath.fallDistanceAfter(1, 0.85, 0.85, 0.85), 0.614125));
		PassTier.Values w = PassTier.COBWEB.defaults();
		PassRule.Cell lone = PassRule.cell(PassTier.COBWEB, w, 0);
		PassState st = new PassState();
		// tick 1: enters web at y=10 (two cells side by side on that layer)
		st.result.reset();
		st.result.add(BlockPos.asLong(0, 10, 0), lone, true, true);
		st.result.add(BlockPos.asLong(1, 10, 0), lone, true, true);
		double f1 = PassThrough.newCellsFallFactor(st, st.result);
		st.rememberCells();
		// tick 2: still in the same cells
		double f2 = PassThrough.newCellsFallFactor(st, st.result);
		// tick 3: enters y=9 below
		st.result.reset();
		st.result.add(BlockPos.asLong(0, 10, 0), lone, true, true);
		st.result.add(BlockPos.asLong(0, 9, 0), lone, true, true);
		double f3 = PassThrough.newCellsFallFactor(st, st.result);
		st.rememberCells();
		// tick 4: only y=8 new
		st.result.reset();
		st.result.add(BlockPos.asLong(0, 9, 0), lone, true, true);
		st.result.add(BlockPos.asLong(0, 8, 0), lone, true, true);
		double f4 = PassThrough.newCellsFallFactor(st, st.result);
		check("two cells side by side on one layer = one block passed (x0.85)", near(f1, 0.85));
		check("staying in the same cell: x1", near(f2, 1.0));
		check("each new layer: x0.85", near(f3, 0.85) && near(f4, 0.85));
		check("three layers in total: about x0.61", near(f1 * f2 * f3 * f4, 0.614125));
	}

	static void modifierMath() {
		check("modifier amount walk 0.75 -> -0.25", near(PassMath.modifierAmount(0.75), -0.25));
		double base = 0.1;
		double asphalt = 1.5 * base - base; // BlockRunner ADDITION
		double v = PassMath.attributeValue(base, new double[]{asphalt}, new double[]{}, new double[]{PassMath.modifierAmount(0.75)});
		check("asphalt 1.5 x crop 0.75 = 1.125x", near(v, 0.1125));
		double sprint = PassMath.attributeValue(base, new double[]{}, new double[]{}, new double[]{0.3, -0.5});
		check("sprint 1.3 x dense 0.5 multiply", near(sprint, 0.065));
		check("FOV without block speeds = vanilla walking 1.0", PassMath.fovSpeedPart(false,
				PassMath.attributeValue(base, new double[]{}, new double[]{}, new double[]{}), 0.1F) == 1.0F);
		check("FOV with sprint only", Math.abs(PassMath.fovSpeedPart(false, 0.13, 0.1F) - 1.15F) < 1e-6);
		for (double walk : new double[]{0.75, 0.5, 0.35}) {
			double m = PassMath.wadeFactor(walk, 0.8);
			check("wading drag for walk " + walk + ": factor " + String.format("%.4f", m) + " gives that speed",
					Math.abs(PassMath.wadeRatio(m, 0.8) - walk) < 1e-9);
		}
		check("wading: walk 1.0 = no drag", near(PassMath.wadeFactor(1.0, 0.8), 1.0));
		check("wading: walk at or below 1-keep clamps to 0", near(PassMath.wadeFactor(0.1, 0.8), 0.0));
	}

	static void stickPoint() {
		Random rnd = new Random(42);
		double[] cell = {0, 0, 0};
		boolean allInside = true;
		for (int i = 0; i < 2000; i++) {
			double[] from = {rnd.nextDouble() * 3 - 1, rnd.nextDouble() * 3 - 1, rnd.nextDouble() * 3 - 1};
			double[] to = {rnd.nextDouble() * 3 - 1, rnd.nextDouble() * 3 - 1, rnd.nextDouble() * 3 - 1};
			double roll = rnd.nextBoolean() ? rnd.nextDouble() : -1;
			double[] p = PassMath.stickPoint(from, to, cell, roll, 0.01);
			if (p == null) continue;
			for (int a = 0; a < 3; a++) {
				if (p[a] < 0.01 - 1e-12 || p[a] > 0.99 + 1e-12) allInside = false;
			}
		}
		check("stick point always inside the cell (2000 random paths)", allInside);
		double[] p = PassMath.stickPoint(new double[]{-0.5, 0.5, 0.5}, new double[]{1.5, 0.5, 0.5}, cell, -1, 0.01);
		check("crossing with no roll hit: stops just inside the exit", p != null && near(p[0], 0.99) && near(p[1], 0.5));
		p = PassMath.stickPoint(new double[]{-0.5, 0.5, 0.5}, new double[]{1.5, 0.5, 0.5}, cell, 0.5, 0.01);
		check("roll 0.5: half way along the clipped path", p != null && near(p[0], 0.5));
		p = PassMath.stickPoint(new double[]{0.5, 1.4, 0.5}, new double[]{0.5, -0.6, 0.5}, cell, -1, 0.01);
		check("dropped straight through: stops near the bottom", p != null && near(p[1], 0.01));
		check("path ending inside with no roll: keeps moving (null)",
				PassMath.stickPoint(new double[]{0.5, 1.4, 0.5}, new double[]{0.5, 0.6, 0.5}, cell, -1, 0.01) == null);
		check("path missing the cell: null",
				PassMath.stickPoint(new double[]{2, 2, 2}, new double[]{3, 2, 2}, cell, 0.5, 0.01) == null);
		p = PassMath.stickPoint(new double[]{0.3, 0.3, 0.3}, new double[]{0.3, 0.3, 0.3}, cell, 0.2, 0.01);
		check("no motion inside: sticks where it is", p != null && near(p[0], 0.3) && near(p[1], 0.3));
	}

	// ---- phase 2 ----

	static boolean insideAny(double[] p, double[][] cells, int n, double inset) {
		for (int i = 0; i < n; i++) {
			boolean in = true;
			for (int a = 0; a < 3; a++) {
				if (p[a] < cells[i][a] + inset - 1e-12 || p[a] > cells[i][a] + 1 - inset + 1e-12) in = false;
			}
			if (in) return true;
		}
		return false;
	}

	static void stickAlong() {
		Random rnd = new Random(7);
		double[][] wall = {{0, 0, 0}, {1, 0, 0}, {3, 0, 0}, {0, 1, 0}};
		boolean allInside = true;
		for (int i = 0; i < 5000; i++) {
			double[] from = {rnd.nextDouble() * 6 - 1, rnd.nextDouble() * 4 - 1, rnd.nextDouble() * 3 - 1};
			double[] to = {rnd.nextDouble() * 6 - 1, rnd.nextDouble() * 4 - 1, rnd.nextDouble() * 3 - 1};
			double roll = rnd.nextBoolean() ? rnd.nextDouble() : -1;
			double[] p = PassMath.stickAlong(from, to, wall, wall.length, roll, 0.01);
			if (p != null && !insideAny(p, wall, wall.length, 0.01)) allInside = false;
		}
		check("stickAlong: point always inside a web cell (5000 random paths, 4 cells)", allInside);

		double[][] two = {{1, 0, 0}, {0, 0, 0}}; // listed out of order on purpose
		double[] p = PassMath.stickAlong(new double[]{-0.5, 0.5, 0.5}, new double[]{2.5, 0.5, 0.5}, two, 2, -1, 0.01);
		check("two touching webs crossed with no hit: stops inside the far exit, not between them",
				p != null && near(p[0], 1.99));
		double[][] gap = {{0, 0, 0}, {2, 0, 0}};
		p = PassMath.stickAlong(new double[]{-0.5, 0.5, 0.5}, new double[]{3.5, 0.5, 0.5}, gap, 2, -1, 0.01);
		check("webs with a gap: the first web's exit", p != null && near(p[0], 0.99));
		p = PassMath.stickAlong(new double[]{-0.5, 0.5, 0.5}, new double[]{2.5, 0.5, 0.5}, two, 2, 0.75, 0.01);
		check("roll 0.75 over two touching webs: three quarters along the run (x 1.5)", p != null && near(p[0], 1.5));
		check("path ending inside the webs with no hit: keeps moving",
				PassMath.stickAlong(new double[]{-0.5, 0.5, 0.5}, new double[]{1.5, 0.5, 0.5}, two, 2, -1, 0.01) == null);
		p = PassMath.stickAlong(new double[]{0.5, 0.5, 0.5}, new double[]{0.5, 0.5, 0.5}, two, 2, -1, 0.01);
		check("no motion inside a web: sticks where it is", p != null && near(p[1], 0.5));
		check("no motion outside every web: null",
				PassMath.stickAlong(new double[]{5, 5, 5}, new double[]{5, 5, 5}, two, 2, 0.3, 0.01) == null);
		p = PassMath.stickAlong(new double[]{0.5, 3.0, 0.5}, new double[]{0.5, -2.0, 0.5}, new double[][]{{0, 0, 0}}, 1, -1, 0.01);
		check("fast item crossing a whole web in one tick still sticks (near the bottom)", p != null && near(p[1], 0.01));
		check("touching a cell only at its corner does not count",
				PassMath.stickAlong(new double[]{-1, -1, 0.5}, new double[]{2, 2, 0.5}, new double[][]{{1, -1, 0}}, 1, 0.5, 0.01) == null);

		// every item that enters a lone web ends up stuck in it: simulated falls at different speeds, chance 0.35
		Random r2 = new Random(11);
		boolean always = true;
		double[] depthSum = new double[1];
		int runs = 0;
		double[][] lone = {{0, 0, 0}};
		for (int i = 0; i < 2000; i++) {
			double y = 1.5 + r2.nextDouble(), vy = -(0.05 + r2.nextDouble() * 1.5);
			double x = 0.2 + r2.nextDouble() * 0.6;
			double[] stuck = null;
			for (int tick = 0; tick < 200 && stuck == null && y > -3; tick++) {
				double ny = y + vy;
				double roll = r2.nextDouble() < 0.35 ? r2.nextDouble() : -1;
				stuck = PassMath.stickAlong(new double[]{x, y, 0.5}, new double[]{x, ny, 0.5}, lone, 1, roll, 0.01);
				y = ny;
				vy = (vy - 0.04) * 0.98;
			}
			if (stuck == null || !insideAny(stuck, lone, 1, 0.01)) always = false;
			else { depthSum[0] += stuck[1]; runs++; }
		}
		check("2000 falls through a lone web: every item sticks inside it", always);
		check("stick heights spread over the cell (mean " + String.format("%.2f", depthSum[0] / Math.max(1, runs)) + ")",
				runs > 0 && depthSum[0] / runs > 0.05 && depthSum[0] / runs < 0.95);
	}

	static void surrounded() {
		double dw = PassTier.COBWEB.defaults().denseWalk();
		check("surrounded: jump climbs 0.2 x 0.35 = 0.07", near(PassMath.surroundedVertical(true, false, dw), 0.07));
		check("surrounded: sneak descends 0.15 x 0.35 = 0.0525", near(PassMath.surroundedVertical(false, true, dw), -0.0525));
		check("surrounded: no input hangs", near(PassMath.surroundedVertical(false, false, dw), 0.0));
		check("surrounded: jump wins over sneak", PassMath.surroundedVertical(true, true, dw) > 0);
		PassResult r = new PassResult();
		PassTier.Values web = PassTier.COBWEB.defaults();
		r.add(5L, PassRule.cell(PassTier.COBWEB, web, 5), true, true);
		check("not surrounded at 5 neighbors", !r.surrounded && r.surroundedTier == null);
		r.add(9L, PassRule.cell(PassTier.COBWEB, web, 26), true, true);
		check("surrounded cell carries its tier", r.surrounded && r.surroundedCell == 9L && r.surroundedTier == PassTier.COBWEB);
		r.reset();
		check("reset clears the surrounded tier", r.surroundedTier == null && !r.surrounded);
	}

	static void arrowDespawn() {
		check("despawn: pickable arrow 12000", PassMath.despawnLimit(true, true, false, 12000) == 12000);
		check("despawn: demoted arrow vanilla 1200", PassMath.despawnLimit(true, true, true, 12000) == 1200);
		check("despawn: unpickable arrow keeps vanilla's code", PassMath.despawnLimit(true, false, false, 12000) == -1);
		check("despawn: system off keeps vanilla's code", PassMath.despawnLimit(false, true, false, 12000) == -1);
		check("oldest: highest life", PassMath.oldestIndex(new int[]{30, 2000, 5, 1999}, 4) == 1);
		check("oldest: tie goes to the earliest joined", PassMath.oldestIndex(new int[]{7, 9, 9}, 3) == 1);
		check("oldest: none", PassMath.oldestIndex(new int[0], 0) == -1);
		// cap 3: four arrows join in order; the oldest (highest life) is demoted, and despawns at once past 1200
		int[] lives = {1500, 400, 900, 1};
		int demoted = PassMath.oldestIndex(lives, 4);
		check("cap 3, 4th joins: arrow with life 1500 demoted and gone at once (>= 1200)",
				demoted == 0 && lives[demoted] >= PassMath.despawnLimit(true, true, true, 12000));
		int[] young = {300, 400, 900, 1};
		int d2 = PassMath.oldestIndex(young, 4);
		check("cap 3, all under a minute: oldest (900) demoted, despawns at 1200",
				d2 == 2 && young[d2] < PassMath.despawnLimit(true, true, true, 12000));
	}

	static void projectileGroups() {
		Object[][] cases = {
				{net.minecraft.world.entity.projectile.Arrow.class, PassGroup.HANG},
				{net.minecraft.world.entity.projectile.SpectralArrow.class, PassGroup.HANG},
				{net.minecraft.world.entity.projectile.ThrownTrident.class, PassGroup.HANG},
				{net.minecraft.world.entity.projectile.Snowball.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.ThrownEnderpearl.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.ThrownEgg.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.LargeFireball.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.SmallFireball.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.DragonFireball.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.WitherSkull.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.LlamaSpit.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.ShulkerBullet.class, PassGroup.BREAK},
				{net.minecraft.world.entity.projectile.ThrownPotion.class, PassGroup.SETTLE},
				{net.minecraft.world.entity.projectile.ThrownExperienceBottle.class, PassGroup.SETTLE},
				{net.minecraft.world.entity.projectile.FishingHook.class, PassGroup.SNAG},
				{net.minecraft.world.entity.projectile.FireworkRocketEntity.class, PassGroup.LINGER},
				{net.minecraft.world.entity.projectile.EvokerFangs.class, PassGroup.SLOW},
		};
		for (Object[] c : cases) {
			Class<?> k = (Class<?>) c[0];
			check("default group " + k.getSimpleName() + " = " + c[1], PassGroup.byClassChain(PassGroup.chainOf(k)) == c[1]);
		}
		check("default group: Quark pickarang chain = SNAG", PassGroup.byClassChain(List.of(
				"vazkii.quark.content.tools.entity.rang.Pickarang", "vazkii.quark.content.tools.entity.rang.AbstractPickarang",
				"net.minecraft.world.entity.projectile.Projectile")) == PassGroup.SNAG);
		check("default group: Botania chakram chain = SNAG (before throwable BREAK)", PassGroup.byClassChain(List.of(
				"vazkii.botania.common.entity.ThornChakramEntity", "net.minecraft.world.entity.projectile.ThrowableProjectile",
				"net.minecraft.world.entity.projectile.Projectile")) == PassGroup.SNAG);
		check("default group: a modded arrow subclass = HANG", PassGroup.byClassChain(List.of(
				"com.example.FancyArrow", "net.minecraft.world.entity.projectile.AbstractArrow")) == PassGroup.HANG);
		check("default group: unknown projectile = SLOW", PassGroup.byClassChain(List.of(
				"com.example.Thing", "net.minecraft.world.entity.projectile.Projectile")) == PassGroup.SLOW);
		check("group tag id creativecompat:passthrough/projectile/break",
				PassProjectiles.groupTag(PassGroup.BREAK).location().toString().equals("creativecompat:passthrough/projectile/break"));
		check("seven groups", PassGroup.values().length == 7
				&& PassProjectiles.groupTag(PassGroup.EXEMPT).location().getPath().equals("passthrough/projectile/exempt"));
	}

	static void exemptTags() {
		check("tier tag creativecompat:passthrough/cobweb",
				PassThrough.blockTag(PassTier.COBWEB).location().toString().equals("creativecompat:passthrough/cobweb"));
		check("exempt tag creativecompat:passthrough_exempt/cobweb",
				PassThrough.exemptTag(PassTier.COBWEB).location().toString().equals("creativecompat:passthrough_exempt/cobweb"));
		check("exempt all tag", PassThrough.exemptAllTag().location().toString().equals("creativecompat:passthrough_exempt/all"));
		check("entity tags are entity-type tags", PassThrough.exemptTag(PassTier.BUSH).isFor(net.minecraft.core.Registry.ENTITY_TYPE_REGISTRY));
		check("unbound exempt tag exempts nobody (spider not hardcoded)", !EntityType.SPIDER.is(PassThrough.exemptTag(PassTier.COBWEB)));
	}

	static void config() {
		CommentedConfig cfg = CommentedConfig.inMemory();
		CompatConfig.SPEC.correct(cfg);
		CompatConfig.SPEC.setConfig(cfg);
		for (PassTier t : PassTier.values()) {
			List<String> keys = t.layered()
					? List.of("layerMaxSlow", "solidBelowFull", "minLayers", "solidBelowFullOverrides", "fall", "depthStriderPasses")
					: List.of("walk", "fall", "growth", "neighbours", "denseFrom", "denseWalk", "denseFall",
					"denseResetsFall", "surrounded", "surroundedAt", "walkOnlyMoving", "items", "itemStickChance",
					"projectiles", "depthStriderPasses");
			for (String k : keys) {
				if (!cfg.contains(List.of("surface", "passthrough", t.key(), k))) {
					check("toml has surface.passthrough." + t.key() + "." + k, false);
				}
			}
		}
		check("toml has every tier key", true);
		check("layers table has no walk / neighbours keys (its walk comes from the height)",
				!cfg.contains(List.of("surface", "passthrough", "layers", "walk"))
						&& !cfg.contains(List.of("surface", "passthrough", "layers", "neighbours"))
						&& !cfg.contains(List.of("surface", "passthrough", "cobweb", "minLayers")));
		check("toml breakStallTicks 3, overrides empty",
				Integer.valueOf(3).equals(cfg.get(List.of("surface", "passthrough", "breakStallTicks")))
						&& List.of().equals(cfg.get(List.of("surface", "passthrough", "layers", "solidBelowFullOverrides"))));
		check("toml enabled / arrowLifeTicks / arrowLongLifeCap",
				Boolean.TRUE.equals(cfg.get(List.of("surface", "passthrough", "enabled")))
						&& Integer.valueOf(12000).equals(cfg.get(List.of("surface", "passthrough", "arrowLifeTicks")))
						&& Integer.valueOf(128).equals(cfg.get(List.of("surface", "passthrough", "arrowLongLifeCap"))));
		PassValues fromToml = PassValues.fromConfig();
		check("toml defaults = code defaults", fromToml.equals(PassValues.defaults()));
		cfg.set(List.of("surface", "passthrough", "dense_crop", "denseWalk"), 0.4);
		CompatConfig.SPEC.afterReload(); // what Forge does on a file save
		PassValues edited = PassValues.fromConfig();
		check("an edit reaches the snapshot", near(edited.tier(PassTier.DENSE_CROP).denseWalk(), 0.4) && !edited.equals(fromToml));

		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		edited.write(buf);
		PassValues back = PassValues.read(buf);
		check("sync packet round trip", back.equals(edited) && buf.readableBytes() == 0);
		check("no server values before login", !PassValues.usingRemote());
		PassValues.setRemote(back);
		check("of(client) = remote, of(server) = local", PassValues.of(true) == back && PassValues.of(false) != back);
		PassValues.setRemote(null);
		check("after logout the client uses its own", PassValues.of(true) != back);
	}
}
