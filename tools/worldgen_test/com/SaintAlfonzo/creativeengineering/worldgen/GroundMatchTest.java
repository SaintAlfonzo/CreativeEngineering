package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.CEConfig.GroundMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/** Headless checks of GroundMatch: ground test, family decision, palette pick, and the template rewrite per mode. */
public class GroundMatchTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static TagKey<Block> GRASS;   // set after Bootstrap (touching a registry earlier throws)

	static List<BlockState> rep(BlockState st, int n) { return Collections.nCopies(n, st); }

	static List<BlockState> concat(List<BlockState>... parts) {
		List<BlockState> out = new ArrayList<>();
		for (List<BlockState> p : parts) out.addAll(p);
		return out;
	}

	static Holder<Block> h(Block b) { return b.builtInRegistryHolder(); }

	public static void main(String[] args) {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		GRASS = BlockTags.create(new ResourceLocation("creativeengineering", "ground_match/grass"));

		// the tags blocktags.js fills (subset)
		Map<TagKey<Block>, List<Holder<Block>>> tags = new HashMap<>();
		tags.put(GRASS, List.of(h(Blocks.GRASS_BLOCK), h(Blocks.DIRT), h(Blocks.COARSE_DIRT), h(Blocks.PODZOL), h(Blocks.ROOTED_DIRT), h(Blocks.MYCELIUM)));
		tags.put(GroundMatch.PLANTS, List.of(h(Blocks.POPPY), h(Blocks.GRASS), h(Blocks.TALL_GRASS), h(Blocks.OAK_SAPLING)));
		tags.put(GroundMatch.NOT_GROUND, List.of(h(Blocks.OAK_LEAVES), h(Blocks.OAK_LOG), h(Blocks.CACTUS)));
		Registry.BLOCK.bindTags(tags);
		check(Blocks.DIRT.defaultBlockState().is(GRASS) && Blocks.POPPY.defaultBlockState().is(GroundMatch.PLANTS), "tags bound");

		// 1. what counts as natural ground
		check(GroundMatch.isGround(Blocks.BASALT.defaultBlockState()), "basalt is ground");
		check(GroundMatch.isGround(Blocks.GRASS_BLOCK.defaultBlockState()), "grass block is ground");
		check(!GroundMatch.isGround(Blocks.AIR.defaultBlockState()), "air is not ground");
		check(!GroundMatch.isGround(Blocks.WATER.defaultBlockState()), "water is not ground");
		check(!GroundMatch.isGround(Blocks.POPPY.defaultBlockState()), "a flower is not ground");
		check(!GroundMatch.isGround(Blocks.SNOW.defaultBlockState()), "a snow layer is not ground");
		check(!GroundMatch.isGround(Blocks.OAK_LEAVES.defaultBlockState()), "leaves (not_ground tag) are not ground");
		check(!GroundMatch.isGround(Blocks.OAK_LOG.defaultBlockState()), "logs (not_ground tag) are not ground");

		// 2. family decision: volcanic peaks vs plains vs a border vs too few columns
		BlockState basalt = Blocks.BASALT.defaultBlockState(), blackstone = Blocks.BLACKSTONE.defaultBlockState(),
				magma = Blocks.MAGMA_BLOCK.defaultBlockState(), grass = Blocks.GRASS_BLOCK.defaultBlockState(),
				dirt = Blocks.DIRT.defaultBlockState(), stone = Blocks.STONE.defaultBlockState();
		GroundMatch.Sample volcanic = new GroundMatch.Sample(
				concat(rep(basalt, 120), rep(blackstone, 50), rep(magma, 10), rep(grass, 4)),
				concat(rep(basalt, 300), rep(stone, 200), rep(dirt, 5)));
		GroundMatch.Palette vp = GroundMatch.palette(volcanic, GRASS, 16, 0.35);
		check(vp != null, "volcanic sample: grass is foreign");
		check(vp != null && vp.columns() == 184 && vp.familyTops() == 4, "volcanic sample: 4 of 184 tops are grass");
		check(vp != null && vp.top().length == 180 && java.util.Arrays.stream(vp.top()).noneMatch(s -> s.is(GRASS)), "top palette has no grass");
		check(vp != null && vp.sub().length == 500 && java.util.Arrays.stream(vp.sub()).noneMatch(s -> s.is(GRASS)), "under palette has no dirt");
		GroundMatch.Sample plains = new GroundMatch.Sample(concat(rep(grass, 150), rep(stone, 30)), rep(dirt, 500));
		check(GroundMatch.palette(plains, GRASS, 16, 0.35) == null, "plains sample: grass is native, no change");
		GroundMatch.Sample border = new GroundMatch.Sample(concat(rep(grass, 70), rep(basalt, 130)), rep(stone, 10));
		check(GroundMatch.palette(border, GRASS, 16, 0.35) == null, "35% grass at the threshold counts as native");
		check(GroundMatch.palette(border, GRASS, 16, 0.36) != null, "just above the threshold it's foreign");
		GroundMatch.Sample few = new GroundMatch.Sample(rep(basalt, 15), rep(basalt, 40));
		check(GroundMatch.palette(few, GRASS, 16, 0.35) == null, "15 columns < minSamples 16: undecided, no change");
		GroundMatch.Sample noSubs = new GroundMatch.Sample(rep(basalt, 40), rep(dirt, 40));
		GroundMatch.Palette ns = GroundMatch.palette(noSubs, GRASS, 16, 0.35);
		check(ns != null && java.util.Arrays.equals(ns.sub(), ns.top()), "all under-blocks in the family: under palette falls back to the tops");

		// 3. pick: deterministic, keeps the sample's proportions
		BlockState[] pal = {basalt, basalt, basalt, blackstone};
		int b = 0, n = 20000;
		for (int i = 0; i < n; i++) if (GroundMatch.pick(pal, new BlockPos(i % 137, 60 + i % 5, i / 137)) == basalt) b++;
		check(Math.abs(b / (double) n - 0.75) < 0.02, "pick keeps proportions: basalt " + b / (double) n);
		check(GroundMatch.pick(pal, new BlockPos(5, 64, -9)) == GroundMatch.pick(pal, new BlockPos(5, 64, -9)), "pick is deterministic");

		// 3b. column exclusion by height: a deep piece no longer hides the surface (03_phase_3.md 4.1)
		List<BoundingBox> deep = List.of(new BoundingBox(0, -60, 0, 47, -40, 47));   // ancient city
		List<BoundingBox> house = List.of(new BoundingBox(0, 65, 0, 8, 72, 8));      // standing on ground at y 64
		check(!GroundMatch.covered(deep, 10, 10, 60, 65), "deep piece under the column: ground at y 64 still sampled");
		check(GroundMatch.covered(house, 5, 5, 60, 65), "piece standing on the ground: column skipped");
		check(GroundMatch.covered(house, 9, 9, 60, 65) && !GroundMatch.covered(house, 10, 10, 60, 65), "1 block of margin in XZ, not 2");
		check(!GroundMatch.covered(house, 5, 5, 50, 63), "ground well below the piece: sampled");

		// 4. the rewrite, on a 5x5 ruin: layer 0 dirt, layer 1 grass (stone wall at x=0), a poppy and a
		//    tall grass on top, a roof-garden grass block at layer 5, and a grass block outside the chunk box.
		int oy = 63;
		List<StructureBlockInfo> ruin = new ArrayList<>();
		for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
			ruin.add(new StructureBlockInfo(new BlockPos(x, oy, z), dirt, null));
			ruin.add(new StructureBlockInfo(new BlockPos(x, oy + 1, z), grass, null));
			if (x == 0) ruin.add(new StructureBlockInfo(new BlockPos(x, oy + 2, z), Blocks.STONE_BRICKS.defaultBlockState(), null));
		}
		ruin.add(new StructureBlockInfo(new BlockPos(2, oy + 2, 2), Blocks.POPPY.defaultBlockState(), null));
		ruin.add(new StructureBlockInfo(new BlockPos(3, oy + 2, 3), Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), null));
		ruin.add(new StructureBlockInfo(new BlockPos(3, oy + 3, 3), Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), null));
		ruin.add(new StructureBlockInfo(new BlockPos(1, oy + 5, 1), grass, null));   // roof garden
		ruin.add(new StructureBlockInfo(new BlockPos(20, oy + 1, 1), grass, null));  // other chunk
		BoundingBox box = new BoundingBox(0, -64, 0, 15, 319, 15);
		List<TagKey<Block>> fams = List.of(GRASS);
		int[] calls = {0};
		IntFunction<GroundMatch.Palette> volcanicFor = f -> { calls[0]++; return vp; };

		GroundMatch.Rewrite p = GroundMatch.rewrite(ruin, oy, box, fams, 3, GroundMode.PALETTE, 4, volcanicFor, pos -> false);
		check(p != null && calls[0] == 1, "PALETTE: palette asked once for the family");
		check(p != null && p.replaced() == 50 && p.plants() == 3, "PALETTE: 50 base blocks replaced, 3 plant blocks dropped (" + (p == null ? "-" : p.replaced() + "/" + p.plants()) + ")");
		Map<BlockPos, BlockState> at = new HashMap<>();
		if (p != null) for (StructureBlockInfo i : p.infos()) at.put(i.pos, i.state);
		check(!at.containsKey(new BlockPos(2, oy + 2, 2)) && !at.containsKey(new BlockPos(3, oy + 3, 3)), "PALETTE: poppy and both tall-grass halves gone");
		check(at.get(new BlockPos(1, oy + 5, 1)) == grass, "PALETTE: roof-garden grass above baseLayers untouched");
		check(at.get(new BlockPos(20, oy + 1, 1)) == grass, "PALETTE: grass outside the chunk box untouched");
		check(at.get(new BlockPos(0, oy + 2, 0)) == Blocks.STONE_BRICKS.defaultBlockState(), "PALETTE: the wall stays");
		boolean topsOk = true, subsOk = true;
		java.util.Set<BlockState> topSet = new java.util.HashSet<>(List.of(vp.top())), subSet = new java.util.HashSet<>(List.of(vp.sub()));
		for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
			BlockState l1 = at.get(new BlockPos(x, oy + 1, z)), l0 = at.get(new BlockPos(x, oy, z));
			if (x == 0 ? !subSet.contains(l1) : !topSet.contains(l1)) topsOk = false;   // under the wall = covered
			if (!subSet.contains(l0)) subsOk = false;
		}
		check(topsOk, "PALETTE: exposed layer-1 grass -> top palette, grass under the wall -> under palette");
		check(subsOk, "PALETTE: dirt under grass -> under palette");
		check(p != null && p.infos().stream().noneMatch(i -> i.state.is(GRASS) && i.pos.getY() < oy + 3 && i.pos.getX() < 16), "PALETTE: no grass/dirt left in the base");

		GroundMatch.Rewrite t = GroundMatch.rewrite(ruin, oy, box, fams, 3, GroundMode.TERRAIN, 4, f -> vp, pos -> pos.getY() == oy);
		check(t != null && t.keptTerrain() == 25 && t.replaced() == 25 && t.plants() == 3, "TERRAIN: layer 0 keeps the terrain (25), layer 1 filled (25)");
		check(t != null && t.infos().stream().noneMatch(i -> i.pos.getY() == oy && i.pos.getX() < 5), "TERRAIN: nothing placed where terrain was kept");

		GroundMatch.Rewrite r = GroundMatch.rewrite(ruin, oy, box, fams, 3, GroundMode.REMOVE, 4, f -> vp, pos -> false);
		check(r != null && r.removed() == 50 && r.infos().size() == ruin.size() - 53, "REMOVE: 50 base blocks + 3 plants not placed");

		calls[0] = 0;
		check(GroundMatch.rewrite(ruin, oy, box, fams, 3, GroundMode.PALETTE, 4, f -> { calls[0]++; return null; }, pos -> false) == null && calls[0] == 1,
				"native family: list unchanged (null)");
		calls[0] = 0;
		List<StructureBlockInfo> noBase = List.of(new StructureBlockInfo(new BlockPos(0, oy, 0), stone, null));
		check(GroundMatch.rewrite(noBase, oy, box, fams, 3, GroundMode.PALETTE, 4, volcanicFor, pos -> false) == null && calls[0] == 0,
				"no base blocks: never samples the ground");
		GroundMatch.Rewrite one = GroundMatch.rewrite(ruin, oy, box, fams, 1, GroundMode.PALETTE, 4, f -> vp, pos -> false);
		check(one != null && one.replaced() == 25 && one.plants() == 0, "baseLayers 1: only layer 0 replaced, grass (and its plants) above stay");

		// 5. config defaults (read through WorldgenConfig; an unloaded config falls back to the defaults)
		check(WorldgenConfig.groundMatchEnabled() && WorldgenConfig.groundMode() == GroundMode.TERRAIN && WorldgenConfig.groundBaseLayers() == 3
				&& WorldgenConfig.groundNativeShare() == 0.35 && WorldgenConfig.groundMinSamples() == 16 && WorldgenConfig.groundMaxPlantHeight() == 4
				&& WorldgenConfig.groundFamilies().equals(List.of("creativeengineering:ground_match/grass")), "CEConfig [worldgen.ground_match] defaults");
		GroundMatch.Rewrite low = GroundMatch.rewrite(ruin, oy, box, fams, 3, GroundMode.PALETTE, 1, f -> vp, pos -> false);
		check(low != null && low.plants() == 2, "maxPlantHeight 1: the upper tall-grass half stays (" + (low == null ? "-" : low.plants()) + " dropped)");

		System.out.println(fails == 0 ? "ALL " + checks + " PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
