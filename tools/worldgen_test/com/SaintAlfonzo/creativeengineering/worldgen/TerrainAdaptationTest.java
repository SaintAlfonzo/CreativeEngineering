package com.SaintAlfonzo.creativeengineering.worldgen;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Headless checks of TemplateTrim (block kinds, template columns, the trim, real ported templates) and
 * TerrainAdaptation (tag precedence, distance fields, the soft beard against vanilla's beard_thin, addSoft).
 * Arg 0: the instance root (for kubejs/data templates).
 */
public class TerrainAdaptationTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static Holder<Block> h(Block b) { return b.builtInRegistryHolder(); }

	static StructureBlockInfo info(int x, int y, int z, Block b) {
		return new StructureBlockInfo(new BlockPos(x, y, z), b.defaultBlockState(), null);
	}

	static boolean near(double a, double b, double tol) { return Math.abs(a - b) <= tol; }

	static long col(int x, int z) { return TemplateTrim.column(x, z); }

	/** A 7 x 3 x 5 test template: a grass pad over every column, a 3 x 3 plank hut on x 2-4, z 1-3, air above, two poppies on the pad. */
	static List<StructureBlockInfo> hut() {
		List<StructureBlockInfo> out = new ArrayList<>();
		for (int x = 0; x < 7; x++) for (int z = 0; z < 5; z++) {
			boolean hut = x >= 2 && x <= 4 && z >= 1 && z <= 3;
			out.add(info(x, 0, z, Blocks.GRASS_BLOCK));
			boolean poppy = (x == 0 && z == 0) || (x == 6 && z == 4);
			out.add(info(x, 1, z, hut ? Blocks.OAK_PLANKS : poppy ? Blocks.POPPY : Blocks.AIR));
			out.add(info(x, 2, z, hut ? Blocks.OAK_PLANKS : Blocks.AIR));
		}
		return out;
	}

	static StructureTemplate load(Path p) throws Exception {
		StructureTemplate t = new StructureTemplate();
		try (InputStream in = new FileInputStream(p.toFile())) { t.load(NbtIo.readCompressed(in)); }
		return t;
	}

	public static void main(String[] args) throws Exception {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		Path root = Path.of(args.length > 0 ? args[0] : ".");

		// the tags blocktags.js fills (the loose-ground part of #minecraft:dirt, sand, gravel, clay, snow; plants)
		Map<TagKey<Block>, List<Holder<Block>>> tags = new HashMap<>();
		tags.put(TemplateTrim.PAD, List.of(h(Blocks.GRASS_BLOCK), h(Blocks.DIRT), h(Blocks.COARSE_DIRT), h(Blocks.PODZOL), h(Blocks.SAND),
				h(Blocks.RED_SAND), h(Blocks.GRAVEL), h(Blocks.CLAY), h(Blocks.SNOW_BLOCK), h(Blocks.MYCELIUM), h(Blocks.ROOTED_DIRT),
				h(Blocks.MUD), h(Blocks.MOSS_BLOCK), h(Blocks.MUDDY_MANGROVE_ROOTS)));
		tags.put(TemplateTrim.PLANTS, List.of(h(Blocks.POPPY), h(Blocks.GRASS), h(Blocks.TALL_GRASS), h(Blocks.FERN), h(Blocks.LARGE_FERN),
				h(Blocks.DANDELION), h(Blocks.SNOW), h(Blocks.MOSS_CARPET), h(Blocks.OAK_SAPLING)));
		TagKey<Block> earth = BlockTags.create(new ResourceLocation("creativeengineering", "earth_fill/earth"));
		tags.put(earth, List.of(h(Blocks.GRASS_BLOCK), h(Blocks.DIRT)));
		Registry.BLOCK.bindTags(tags);

		// 1. block kinds
		check(TemplateTrim.kind(Blocks.AIR.defaultBlockState()) == TemplateTrim.Kind.AIR, "air is AIR");
		check(TemplateTrim.kind(Blocks.CAVE_AIR.defaultBlockState()) == TemplateTrim.Kind.AIR, "cave air is AIR");
		check(TemplateTrim.kind(Blocks.GRASS_BLOCK.defaultBlockState()) == TemplateTrim.Kind.PAD, "grass block is PAD");
		check(TemplateTrim.kind(Blocks.POPPY.defaultBlockState()) == TemplateTrim.Kind.PLANT, "poppy is PLANT");
		check(TemplateTrim.kind(Blocks.SNOW.defaultBlockState()) == TemplateTrim.Kind.PLANT, "a snow layer is PLANT");
		check(TemplateTrim.kind(Blocks.STRUCTURE_BLOCK.defaultBlockState()) == TemplateTrim.Kind.MARKER
				&& TemplateTrim.kind(Blocks.JIGSAW.defaultBlockState()) == TemplateTrim.Kind.MARKER, "structure block and jigsaw are MARKER");
		check(TemplateTrim.kind(Blocks.OAK_PLANKS.defaultBlockState()) == TemplateTrim.Kind.BUILT
				&& TemplateTrim.kind(Blocks.DIRT_PATH.defaultBlockState()) == TemplateTrim.Kind.BUILT
				&& TemplateTrim.kind(Blocks.FARMLAND.defaultBlockState()) == TemplateTrim.Kind.BUILT, "planks, dirt path, farmland are BUILT");

		// 2. template columns
		List<StructureBlockInfo> hut = hut();
		TemplateTrim.Columns cols = TemplateTrim.columns(hut);
		check(cols.solid().size() == 35 && cols.built().size() == 9, "hut: 35 solid columns, 9 built (got " + cols.solid().size() + ", " + cols.built().size() + ")");
		check(cols.footprint(true) == cols.built() && cols.footprint(false) == cols.solid(), "footprint: built when trimmed, solid otherwise");
		check(!cols.placed(0, 0) && cols.placed(3, 2), "placed: pad corner dropped, hut column kept");
		List<StructureBlockInfo> plate = new ArrayList<>();
		for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) plate.add(info(x, 0, z, Blocks.DIRT));
		TemplateTrim.Columns plateCols = TemplateTrim.columns(plate);
		check(plateCols.built().isEmpty() && plateCols.footprint(true) == plateCols.solid() && plateCols.placed(0, 0),
				"a pad-only plate: no built column, footprint = whole plate, everything placed");

		// 3. the trim
		TemplateTrim.Result r = TemplateTrim.trim(hut);
		check(r != null && r.air() == 50 && r.pad() == 26 && r.plants() == 2 && r.columns() == 26,
				"hut trim: 50 air, 26 pad, 2 poppies in 26 columns (got " + (r == null ? "null" : r.air() + ", " + r.pad() + ", " + r.plants() + ", " + r.columns()) + ")");
		check(r != null && r.infos().size() == hut.size() - 78, "hut trim keeps the other " + (hut.size() - 78) + " blocks");
		boolean hutWhole = true;
		for (StructureBlockInfo i : r.infos()) {
			int x = i.pos.getX(), z = i.pos.getZ();
			if (!(x >= 2 && x <= 4 && z >= 1 && z <= 3)) hutWhole = false;
		}
		check(hutWhole && r.infos().stream().filter(i -> i.state.is(Blocks.GRASS_BLOCK)).count() == 9,
				"only hut columns remain, their 9 grass blocks under the hut included");
		check(TemplateTrim.trim(plate) == null, "a pad-only plate (foundation piece) is left alone");
		List<StructureBlockInfo> marked = new ArrayList<>(hut);
		marked.add(info(0, 2, 2, Blocks.STRUCTURE_BLOCK));
		TemplateTrim.Result rm = TemplateTrim.trim(marked);
		check(rm != null && rm.infos().stream().anyMatch(i -> i.state.is(Blocks.STRUCTURE_BLOCK)), "a structure block in a trimmed column is kept");
		List<StructureBlockInfo> compact = new ArrayList<>();
		for (StructureBlockInfo i : hut) if (i.pos.getX() >= 2 && i.pos.getX() <= 4 && i.pos.getZ() >= 1 && i.pos.getZ() <= 3) compact.add(i);
		check(TemplateTrim.trim(compact) == null, "nothing to drop -> null (list untouched)");
		List<StructureBlockInfo> shifted = new ArrayList<>();
		for (StructureBlockInfo i : hut) shifted.add(new StructureBlockInfo(i.pos.offset(1000, 64, -300), i.state, null));
		TemplateTrim.Result rs = TemplateTrim.trim(shifted);
		check(rs != null && rs.air() == 50 && rs.pad() == 26, "world-placed copy trims the same (columns by world x, z)");

		// 4. earth fill counts only placed earth when trimmed
		StructureTemplate hutT = EarthFillTest.template(hut);
		check(EarthFill.localEarth(hutT, List.of(earth), 3).length == 35, "earth fill, untrimmed: 35 pad blocks");
		check(EarthFill.localEarth(hutT, List.of(earth), 3, true).length == 9, "earth fill, trimmed: 9 (only under the hut)");
		check(EarthFill.localEarth(EarthFillTest.template(plate), List.of(earth), 3, true).length == 25, "earth fill, trimmed plate: all 25 (plates aren't trimmed)");

		// 5. real ported templates (numbers from an independent python read of the same nbt)
		Path ballista = root.resolve("kubejs/data/illager_siege_weapons/structures/ballista.nbt");
		if (Files.exists(ballista)) {
			StructureTemplate bt = load(ballista);
			List<StructureBlockInfo> blocks = EarthFill.firstPalette(bt);
			TemplateTrim.Columns bc = TemplateTrim.columns(blocks);
			check(blocks.size() == 546 && bc.solid().size() == 56 && bc.built().size() == 56, "ballista: 546 blocks, 56 built columns, no pad");
			TemplateTrim.Result br = TemplateTrim.trim(blocks);
			check(br != null && br.air() == 210 && br.pad() == 0 && br.columns() == 35, "ballista trim: 210 air in 35 air-only columns");
		} else check(false, "ballista.nbt missing at " + ballista);
		Object[][] real = {
				{"kubejs/data/villager_circus_tent/structures/circus_tent.nbt", 841, 577, 6072, 264, 0},
				{"kubejs/data/forest_ruins/structures/ruin_3.nbt", 238, 170, 519, 69, 24},
		};
		for (Object[] row : real) {
			Path p = root.resolve((String) row[0]);
			if (!Files.exists(p)) { check(false, "missing " + p); continue; }
			List<StructureBlockInfo> blocks = EarthFill.firstPalette(load(p));
			TemplateTrim.Columns c = TemplateTrim.columns(blocks);
			TemplateTrim.Result t = TemplateTrim.trim(blocks);
			check(c.solid().size() == (int) row[1] && c.built().size() == (int) row[2] && t != null && t.air() == (int) row[3]
					&& t.pad() == (int) row[4] && t.plants() == (int) row[5],
					p.getFileName() + ": " + c.solid().size() + " solid / " + c.built().size() + " built columns, trims "
							+ (t == null ? "nothing" : t.air() + " air, " + t.pad() + " pad, " + t.plants() + " plants"));
		}

		// 6. tag precedence and vanilla mapping
		check(TerrainAdaptation.resolve(t -> t == TerrainAdaptation.Mode.SOFT.tag || t == TerrainAdaptation.Mode.NONE.tag) == TerrainAdaptation.Mode.NONE,
				"soft + none -> none");
		check(TerrainAdaptation.resolve(t -> t == TerrainAdaptation.Mode.SOFT.tag || t == TerrainAdaptation.Mode.BEARD_THIN.tag) == TerrainAdaptation.Mode.BEARD_THIN,
				"soft + beard_thin -> beard_thin");
		check(TerrainAdaptation.resolve(t -> false) == null, "no tag -> null (structure keeps its own)");
		check(TerrainAdaptation.Mode.SOFT.vanilla == TerrainAdjustment.BEARD_THIN && TerrainAdaptation.Mode.NONE.vanilla == TerrainAdjustment.NONE
				&& TerrainAdaptation.Mode.BEARD_BOX.vanilla == TerrainAdjustment.BEARD_BOX, "soft is beard_thin to vanilla; others map 1:1");
		check(TerrainAdaptation.Mode.SOFT.tag.location().toString().equals("creativeengineering:terrain_adaptation/soft")
				&& TerrainAdaptation.TRIM.location().toString().equals("creativeengineering:template_trim"), "tag ids");

		// 7. config defaults
		check(WorldgenConfig.softSkirtBump() == 0 && TerrainAdaptation.skirt() == 4, "skirtBump default 0 -> skirt 4");
		check(WorldgenConfig.terrainAdaptationEnabled() && WorldgenConfig.templateTrimEnabled(), "both switches default on");

		// 8. distance fields
		LongOpenHashSet one = new LongOpenHashSet(new long[]{col(0, 0)});
		TerrainAdaptation.DistanceField f1 = TerrainAdaptation.field(one);
		check(f1.at(0, 0) == 0f && near(f1.at(3, 4), 5, 1e-6) && near(f1.at(-12, 0), 12, 1e-6), "one column: 0 on it, 5 at (3,4), 12 at (-12,0)");
		check(Float.isInfinite(f1.at(9, 9)) && Float.isInfinite(f1.at(13, 0)), "beyond reach 12: infinite");
		LongOpenHashSet ell = new LongOpenHashSet(new long[]{col(0, 0), col(1, 0), col(2, 0), col(0, 1), col(0, 2)});
		TerrainAdaptation.DistanceField fl = TerrainAdaptation.field(ell);
		check(near(fl.at(2, 2), 2, 1e-6) && near(fl.at(4, 0), 2, 1e-6) && near(fl.at(3, 3), Math.sqrt(10), 1e-6), "L shape: nearest column wins");
		check(TerrainAdaptation.field(new LongOpenHashSet()) == null, "no columns -> no field");
		var rotated = TerrainAdaptation.placedColumns(new LongOpenHashSet(new long[]{col(2, 0)}), new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90));
		check(rotated.contains(col(0, 2)), "rotation clockwise 90: local (2,0) -> (0,2), as placeInWorld puts it");

		// 9. the soft beard against vanilla
		Method vanilla = Beardifier.class.getDeclaredMethod("getBeardContribution", int.class, int.class, int.class, int.class);
		vanilla.setAccessible(true);
		double worst = 0;
		for (int dx = 0; dx <= 8; dx++) for (int dz = 0; dz <= 6; dz++) for (int dy = -8; dy <= 8; dy++) {
			double v = (double) vanilla.invoke(null, dx, dy, dz, dy);
			worst = Math.max(worst, Math.abs(v - TerrainAdaptation.beard(Math.sqrt(dx * dx + dz * dz), dy)));
		}
		check(worst < 1e-3, "beard(h, dy) = vanilla getBeardContribution(dx, dy, dz, dy) for h = |(dx,dz)| (worst diff " + worst + ")");

		// a 5 x 5 box footprint at the origin, ground y 64
		LongOpenHashSet square = new LongOpenHashSet();
		for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) square.add(col(x, z));
		BoundingBox box = new BoundingBox(0, 64, 0, 4, 70, 4);
		TerrainAdaptation.SoftBox soft8 = TerrainAdaptation.softBox(box, square, 0, 0, 64, 8);
		double worstBox = 0;
		for (int x = -9; x <= 13; x++) for (int z = -9; z <= 13; z++) for (int y = 56; y <= 72; y++) {
			int dx = Math.max(0, Math.max(box.minX() - x, x - box.maxX())), dz = Math.max(0, Math.max(box.minZ() - z, z - box.maxZ()));
			double v = dx < 12 && dz < 12 ? (double) vanilla.invoke(null, dx, y - 64, dz, y - 64) * 0.8 : 0;
			worstBox = Math.max(worstBox, Math.abs(v - soft8.density(x, y, z)));
		}
		check(worstBox < 1e-3, "skirt 8 (bump 4) on a box footprint = vanilla beard_thin around that box (worst diff " + worstBox + ")");

		TerrainAdaptation.SoftBox soft4 = TerrainAdaptation.softBox(box, square, 0, 0, 64, 4);
		check(near(soft4.density(6, 60, 2), soft8.density(8, 60, 2), 1e-9) && near(soft4.density(5, 66, 2), soft8.density(6, 66, 2), 1e-9),
				"skirt 4: distance h behaves like 2h at skirt 8");
		check(soft4.density(2, 60, 2) > 0 && soft4.density(2, 67, 2) < 0, "inside the footprint: fill below the floor, carve above");
		check(soft4.density(10, 60, 2) == 0 && soft4.density(4 + 6, 63, 2) == 0 && soft4.density(4 + 5, 63, 2) != 0, "skirt 4 ends at 6 blocks (12 / scale 2)");
		check(soft4.density(2, 64 + 12, 2) == 0 && soft4.density(2, 64 - 13, 2) == 0, "nothing 12 above or 13 below the ground");
		TerrainAdaptation.SoftBox soft12 = TerrainAdaptation.softBox(box, square, 0, 0, 64, 12);
		check(soft12.density(4 + 11, 62, 2) != 0 && soft12.density(4 + 12, 62, 2) == 0, "skirt 12 (bump 8) reaches 12, vanilla's limit");
		check(Math.abs(soft4.density(7, 62, 2)) < Math.abs(soft8.density(7, 62, 2)), "a tighter skirt has less effect 3 blocks out");

		// footprint follows the built columns: an L inside an 8 x 8 template box
		LongOpenHashSet lPlaced = new LongOpenHashSet(new long[]{col(0, 0), col(1, 0), col(0, 1)});
		BoundingBox lbox = new BoundingBox(0, 64, 0, 7, 70, 7);
		TerrainAdaptation.SoftBox softL = TerrainAdaptation.softBox(lbox, lPlaced, 100, 200, 64, 4);
		check(softL.density(100, 60, 200) > 0 && softL.density(107, 60, 207) == 0, "footprint, not box: built corner filled, far box corner untouched");
		check(near(softL.distance(103, 200), 2, 1e-6), "placed at an origin: distances relative to it");
		TerrainAdaptation.SoftBox boxOnly = TerrainAdaptation.softBox(box, new LongOpenHashSet(), 0, 0, 64, 4);
		check(boxOnly.placed.isEmpty() && near(boxOnly.distance(7, 8), 5, 1e-9) && boxOnly.distance(2, 2) == 0, "no template: the box is the footprint (round corners)");

		// 10. addSoft walks the rigid list after vanilla's loop and rewinds it
		ObjectArrayList<Beardifier.Rigid> rigids = new ObjectArrayList<>();
		rigids.add(new Beardifier.Rigid(new BoundingBox(50, 64, 50, 52, 66, 52), TerrainAdjustment.BEARD_THIN, 0));
		rigids.add(new Beardifier.Rigid(soft4, TerrainAdjustment.NONE, 0));
		ObjectListIterator<Beardifier.Rigid> it = rigids.iterator();
		while (it.hasNext()) it.next();
		double total = TerrainAdaptation.addSoft(1.0, it, 2, 60, 2);
		check(near(total, 1.0 + soft4.density(2, 60, 2), 1e-12), "addSoft adds only SoftBox rigids");
		check(!it.hasPrevious() && it.nextIndex() == 0, "addSoft leaves the iterator at the start (where back() puts it)");

		System.out.println(fails == 0 ? "ALL " + checks + " PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
