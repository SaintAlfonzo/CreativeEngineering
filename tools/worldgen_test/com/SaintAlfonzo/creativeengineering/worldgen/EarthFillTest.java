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

/** Headless checks of EarthFill: the counting core, earth blocks of a real template through rotation, pool elements, overrides. */
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
		return new StructureTemplate.StructureBlockInfo(new BlockPos(x, y, z), b.defaultBlockState(), null);
	}

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
		Registry.BLOCK.bindTags(tags);
		List<TagKey<Block>> earth = List.of(earthTag);

		// 1. the counting core: water below y 10, air above 12, solid between; the water/air ratio per limit
		EarthFill.Terrain lake = (x, y, z) -> y < 10 ? EarthFill.Kind.WATER : y > 12 ? EarthFill.Kind.AIR : EarthFill.Kind.SOLID;
		long[] pad = longs(new int[]{0, 5, 0}, new int[]{1, 5, 0}, new int[]{2, 11, 0}, new int[]{3, 14, 0}, new int[]{4, 15, 0});
		EarthFill.Result r = EarthFill.count(List.of(pad), lake, -1, -1, false);
		check(r.earth() == 5 && r.water() == 2 && r.air() == 2 && !r.cancelled() && r.complete(), "counts 5 earth: 2 water, 2 air, 1 solid; no limits, not cancelled");
		check(!EarthFill.count(List.of(pad), lake, 2, 2, false).cancelled(), "at the limit is allowed");
		check(EarthFill.count(List.of(pad), lake, 1, -1, false).cancelled(), "water 2 > limit 1 cancels");
		check(EarthFill.count(List.of(pad), lake, -1, 1, false).cancelled(), "air 2 > limit 1 cancels");
		check(EarthFill.count(List.of(pad), lake, 0, 0, false).cancelled(), "0 and 0: any water or air cancels");
		check(!EarthFill.count(List.of(longs(new int[]{0, 11, 0})), lake, 0, 0, false).cancelled(), "all earth solid: nothing to cancel at 0/0");
		r = EarthFill.count(List.of(pad), lake, 0, -1, true);
		check(r.cancelled() && !r.complete() && r.earth() == 1 && r.water() == 1, "stopEarly: stops at the first water block (counted 1)");
		r = EarthFill.count(List.of(pad, pad), lake, -1, -1, false);
		check(r.earth() == 5, "the same position in two pieces counts once");
		check(EarthFill.count(List.<long[]>of(), lake, 0, 0, false).earth() == 0, "no earth, no result");

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

		LongArrayList out = new LongArrayList();
		EarthFill.addTemplate(t, new BlockPos(100, 64, -50), new StructurePlaceSettings(), earth, 3, out);
		check(out.contains(BlockPos.asLong(100, 64, -50)) && out.contains(BlockPos.asLong(102, 64, -48)) && out.contains(BlockPos.asLong(100, 65, -50)), "no rotation: local + origin");
		out.clear();
		EarthFill.addTemplate(t, new BlockPos(100, 64, -50), new StructurePlaceSettings().setRotation(Rotation.CLOCKWISE_90), earth, 3, out);
		// (x, z) -> (-z, x): the skirt at local (0, 1, 0) stays at the origin, local (2, 0, 2) goes to (-2, 0, 2)
		check(out.contains(BlockPos.asLong(100, 65, -50)) && out.contains(BlockPos.asLong(98, 64, -48)) && !out.contains(BlockPos.asLong(102, 64, -48)),
				"CLOCKWISE_90 turns the pad around its corner");
		check(out.size() == 11 && out.stream().allMatch(l -> BlockPos.getY(l) >= 64 && BlockPos.getY(l) <= 66), "rotation keeps 11 blocks and never moves Y");

		// 3. pool elements: single, list, terrain-matching (skipped), and a whole piece
		SinglePoolElement single = new SinglePoolElement(t);
		out.clear();
		EarthFill.addElement(single, BlockPos.ZERO, Rotation.NONE, null, earth, 3, out);
		check(out.size() == 11, "a rigid single element counts its earth");
		out.clear();
		EarthFill.addElement(single.setProjection(StructureTemplatePool.Projection.TERRAIN_MATCHING), BlockPos.ZERO, Rotation.NONE, null, earth, 3, out);
		check(out.isEmpty(), "a terrain-matching element (village street) is skipped");
		out.clear();
		ListPoolElement list = new ListPoolElement(List.<StructurePoolElement>of(new SinglePoolElement(t), new SinglePoolElement(t)), StructureTemplatePool.Projection.RIGID);
		EarthFill.addElement(list, BlockPos.ZERO, Rotation.NONE, null, earth, 3, out);
		check(out.size() == 22, "a list element counts each of its elements (22)");
		PoolElementStructurePiece piece = new PoolElementStructurePiece(null, new SinglePoolElement(t), new BlockPos(10, 70, 10), 0, Rotation.COUNTERCLOCKWISE_90,
				new BoundingBox(10, 70, 10, 13, 74, 13));
		long[] world = EarthFill.pieceEarth(piece, null, earth, 3);
		check(world.length == 11, "a pool-element piece gives its element's earth at its position and rotation (11)");

		// 4. overrides: parsing and matching
		List<EarthFill.Override> parsed = EarthFill.parseOverrides(List.of("philipsruins:ancient_ruins=0,16", "#minecraft:village=-1,-1", "garbage", "a:b=1", "a:b=x,2"));
		check(parsed.size() == 2 && parsed.get(0).key().equals("philipsruins:ancient_ruins") && parsed.get(0).water() == 0 && parsed.get(0).air() == 16
				&& parsed.get(1).key().equals("#minecraft:village") && parsed.get(1).water() == -1, "overrides: 2 valid lines parsed, 3 malformed ones skipped");

		ResourceKey<Registry<Structure>> reg = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/structure"));
		ResourceKey<Structure> key = ResourceKey.create(reg, new ResourceLocation("philipsruins:ancient_ruins"));
		Holder.Reference<Structure> holder = Holder.Reference.createStandAlone(null, key);
		holder.bind(key, new Fake());
		EarthFill.Limits lim = EarthFill.limitsFor(holder);
		check(lim.water() == 24 && lim.air() == 256, "no overrides configured: the global defaults (24 over water, 256 over air)");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
