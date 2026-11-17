package com.SaintAlfonzo.creativedevtools.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The last verbs moved from {@code /zq} (tools/scratch_query/zq_probe.js, retired): season (Creative
 * Calendar's cached season type, read reflectively so Devtools loads without Calendar), floating (sand and gravel over
 * air), under (structure starts judged on their placed blocks, the SQ-EFB judge) and nbt (an item stack's NBT as the
 * game holds it). Same arguments as the /zq versions; output in the {@code [cdevtest] verb=... key=value} form.
 */
final class TestJudges {

	private static final String SEASON_TYPES = "com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes";
	/** /zq under's default earth tag, under its owning jar's namespace. */
	static final String EARTH_TAG = "creativeworldgen:earth_fill/earth";
	static final int MAX_STARTS = 30;
	static final int MAX_FLOAT_LINES = 10;

	private TestJudges() {
	}

	static void register() {
		TestCommand.add("season", TestJudges::season);
		TestCommand.add("floating", TestJudges::floating);
		TestCommand.add("under", TestJudges::under);
		TestCommand.add("nbt", TestJudges::nbt);
	}

	/** The level named by a positional dim word (the /zq form), else {@code dim=}, else the caller's. */
	private static ServerLevel level(TestCtx c, String dimWord) {
		if (dimWord == null) return c.level();
		if (c.args.has("dim")) throw new TestArgs.Bad("dim given twice");
		ServerLevel l = c.server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, TestCtx.id(dimWord)));
		if (l == null) throw new TestArgs.Bad("no level " + dimWord);
		return l;
	}

	// ---- season

	/** season B1,B2,...: SeasonTypes.tagged (the cached season type, null = untagged) of each biome. */
	private static void season(TestCtx c) throws Exception {
		StringBuilder raw = new StringBuilder(c.args.word("biomes"));
		while (c.args.hasNext()) raw.append(',').append(c.args.word("biome"));
		c.args.checkOpts();
		List<String> biomes = GroundJudge.list(raw.toString());
		Class<?> types;
		try {
			types = Class.forName(SEASON_TYPES, true, TestJudges.class.getClassLoader());
		} catch (ClassNotFoundException | LinkageError e) {
			throw new TestArgs.Bad("Creative Calendar not loaded");
		}
		Method tagged = types.getMethod("tagged", Holder.class);
		Object enabled = types.getMethod("enabled").invoke(null);
		Registry<Biome> reg = c.server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		for (String b : biomes) {
			ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, TestCtx.id(b));
			Holder<Biome> h = reg.getHolder(key).orElseThrow(() -> new TestArgs.Bad("no biome " + b));
			Object type;
			try {
				type = tagged.invoke(null, h);
			} catch (InvocationTargetException e) {
				throw e.getCause() instanceof Exception ex ? ex : e;
			}
			c.emit(c.line().kv("biome", b).kv("type", type).kv("enabled", enabled));
		}
	}

	// ---- floating

	/** floating X Z R [dim=]: sand, red sand and gravel with air below, y 0 to 140, loaded chunks only (first 10 printed). */
	private static void floating(TestCtx c) {
		int cx = c.args.integer("x"), cz = c.args.integer("z"), r = c.args.integer("r");
		c.args.end();
		c.args.checkOpts("dim");
		if (r < 0 || r > 64) throw new TestArgs.Bad("r out of range 0..64: " + r);
		ServerLevel level = c.level();
		int y1 = Math.max(0, level.getMinBuildHeight()), y2 = Math.min(140, level.getMaxBuildHeight() - 1);
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		int found = 0, skipped = 0;
		for (int x = cx - r; x <= cx + r; x++) {
			for (int z = cz - r; z <= cz + r; z++) {
				if (!level.hasChunk(x >> 4, z >> 4)) {
					skipped++;
					continue;
				}
				String below = null;
				for (int y = y1; y <= y2; y++) {
					String id = TestWorld.blockId(level.getBlockState(m.set(x, y, z)));
					if (GroundJudge.floats(id, below)) {
						if (found < MAX_FLOAT_LINES) c.emit(c.line().kv("block", id).kv("pos", x + "," + y + "," + z));
						found++;
					}
					below = id;
				}
			}
		}
		c.emit(c.line().kv("n", found).kv("shown", Math.min(found, MAX_FLOAT_LINES)).kv("skipped_columns", skipped));
	}

	// ---- under

	private static GroundJudge.Kind kind(ServerLevel level, BlockPos.MutableBlockPos m, int x, int y, int z) {
		BlockState st = level.getBlockState(m.set(x, y, z));
		String fl = String.valueOf(ForgeRegistries.FLUIDS.getKey(st.getFluidState().getType()));
		if (fl.contains("water")) return GroundJudge.Kind.WATER;
		if (st.isAir() || !st.getMaterial().isSolid()) return GroundJudge.Kind.AIR;
		return GroundJudge.Kind.SOLID;
	}

	/**
	 * under X Z RCHUNKS [IDTEXT|-] [DIM] [earth=TAG]: every structure start in the loaded chunks of the square, judged
	 * on the blocks really placed. Per start (first 30), over the columns of its pieces (each column once, from the
	 * lowest box minY to the highest maxY): air_below / air_max / over6 (columns whose first block under the built
	 * bottom is air, the deepest gap, gaps over 6), water_below / water_max, base_water (water in the bottom 3 layers),
	 * base_earth (earth tag ids in the bottom 3 layers, top 6), ring (top blocks on a ring 3 out from the box, top 6),
	 * foreign (base earth ids the ring never shows). Ends with {@code starts=} and {@code skipped_chunks=}.
	 */
	@SuppressWarnings("deprecation")
	private static void under(TestCtx c) {
		int bx = c.args.integer("x"), bz = c.args.integer("z"), r = c.args.integer("rChunks");
		String filt = c.args.wordOr("-");
		String dimWord = c.args.wordOr(null);
		c.args.end();
		c.args.checkOpts("dim", "earth");
		if (r < 0 || r > 32) throw new TestArgs.Bad("rChunks out of range 0..32: " + r);
		if (filt.equals("-")) filt = null;
		ServerLevel level = level(c, dimWord);
		Registry<Structure> sreg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		TagKey<Block> earthTag = TagKey.create(Registry.BLOCK_REGISTRY, TestCtx.id(c.args.opt("earth", EARTH_TAG)));
		Set<String> earth = new HashSet<>();
		for (Holder<Block> h : Registry.BLOCK.getTagOrEmpty(earthTag)) earth.add(String.valueOf(ForgeRegistries.BLOCKS.getKey(h.value())));
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		int cx0 = bx >> 4, cz0 = bz >> 4, shown = 0, skipped = 0;
		for (int cx = cx0 - r; cx <= cx0 + r && shown < MAX_STARTS; cx++) {
			for (int cz = cz0 - r; cz <= cz0 + r && shown < MAX_STARTS; cz++) {
				if (!level.hasChunk(cx, cz)) {
					skipped++;
					continue;
				}
				Map<Structure, StructureStart> starts = new HashMap<>(level.getChunk(cx, cz).getAllStarts());
				List<Map.Entry<Structure, StructureStart>> sorted = new ArrayList<>(starts.entrySet());
				sorted.sort(java.util.Comparator.comparing(e -> String.valueOf(sreg.getKey(e.getKey()))));
				for (Map.Entry<Structure, StructureStart> e : sorted) {
					if (shown >= MAX_STARTS) break;
					StructureStart st = e.getValue();
					if (!st.isValid()) continue;
					String id = String.valueOf(sreg.getKey(e.getKey()));
					if (filt != null && !id.contains(filt)) continue;
					c.emit(judgeStart(c, level, m, id, st, earth));
					shown++;
				}
			}
		}
		c.emit(c.line().kv("starts", shown).kv("skipped_chunks", skipped));
	}

	private static TestLine judgeStart(TestCtx c, ServerLevel level, BlockPos.MutableBlockPos m, String id, StructureStart st, Set<String> earth) {
		Map<Long, int[]> cols = new HashMap<>();
		for (StructurePiece pc : st.getPieces()) {
			BoundingBox b = pc.getBoundingBox();
			for (int x = b.minX(); x <= b.maxX(); x++) {
				for (int z = b.minZ(); z <= b.maxZ(); z++) {
					int[] col = cols.get(BlockPos.asLong(x, 0, z));
					if (col == null) cols.put(BlockPos.asLong(x, 0, z), new int[]{x, z, b.minY(), b.maxY()});
					else {
						col[2] = Math.min(col[2], b.minY());
						col[3] = Math.max(col[3], b.maxY());
					}
				}
			}
		}
		int n = 0, airB = 0, gapMax = 0, deep = 0, watB = 0, watMax = 0, baseWat = 0, unloaded = 0;
		Map<String, Integer> baseEarth = new TreeMap<>();
		for (int[] col : cols.values()) {
			int x = col[0], z = col[1];
			if (!level.hasChunk(x >> 4, z >> 4)) {
				unloaded++;
				continue;
			}
			GroundJudge.Under u = GroundJudge.under(col[2], col[3], y -> kind(level, m, x, y, z));
			if (u == null) continue;
			n++;
			for (int y = u.bottom(); y <= u.bottom() + 2; y++) {
				if (kind(level, m, x, y, z) == GroundJudge.Kind.WATER) baseWat++;
				String bid = TestWorld.blockId(level.getBlockState(m.set(x, y, z)));
				if (earth.contains(bid)) baseEarth.merge(bid, 1, Integer::sum);
			}
			if (u.first() == GroundJudge.Kind.SOLID) continue;
			if (u.first() == GroundJudge.Kind.AIR) {
				airB++;
				gapMax = Math.max(gapMax, u.gap());
			} else {
				watB++;
				watMax = Math.max(watMax, u.water());
			}
			if (u.deep()) deep++;
		}
		BoundingBox bb = st.getBoundingBox();
		Map<String, Integer> ring = new TreeMap<>();
		for (int[] p : GroundJudge.ring(bb.minX(), bb.minZ(), bb.maxX(), bb.maxZ())) {
			if (!level.hasChunk(p[0] >> 4, p[1] >> 4)) continue;
			int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, p[0], p[1]) - 1;
			ring.merge(TestWorld.blockId(level.getBlockState(m.set(p[0], y, p[1]))), 1, Integer::sum);
		}
		var o = st.getChunkPos();
		return c.line().kv("id", id).kv("chunk", o.x + "," + o.z)
				.kv("box", bb.minX() + "," + bb.minY() + "," + bb.minZ() + "," + bb.maxX() + "," + bb.maxY() + "," + bb.maxZ())
				.kv("pieces", st.getPieces().size()).kv("cols", n).kv("unloaded_cols", unloaded)
				.kv("air_below", airB).kv("air_max", gapMax).kv("over6", deep)
				.kv("water_below", watB).kv("water_max", watMax).kv("base_water", baseWat)
				.map("base_earth", GroundJudge.top(baseEarth, 6)).map("ring", GroundJudge.top(ring, 6))
				.kv("foreign", GroundJudge.foreign(baseEarth, ring.keySet()));
	}

	// ---- nbt

	/**
	 * nbt ITEM: the stack's NBT as the game holds it (has_tag, tag, share_tag = what goes to the client, potion). A bare
	 * id gets /zq nbt's fixed tag {Potion:"minecraft:water",CustomPotionColor:1}; {@code id{snbt}} uses its own. The
	 * KubeJS side of the old verb (typeof getTag vs .nbt in Rhino) has no Java equivalent; a per-feature probe keeps it.
	 */
	private static void nbt(TestCtx c) throws Exception {
		String spec = c.args.word("item");
		c.args.end();
		c.args.checkOpts();
		ItemStack st = TestCtx.stack(spec);
		if (st.isEmpty()) throw new TestArgs.Bad("empty stack " + spec);
		if (spec.indexOf('{') < 0) st.setTag(TagParser.parseTag("{Potion:\"minecraft:water\",CustomPotionColor:1}"));
		var share = st.getItem().getShareTag(st);
		ResourceLocation potion = ForgeRegistries.POTIONS.getKey(net.minecraft.world.item.alchemy.PotionUtils.getPotion(st));
		c.emit(c.line().kv("item", TestCtx.key(st.getItem())).kv("has_tag", st.hasTag())
				.kv("tag", st.hasTag() ? st.getTag().toString() : "-")
				.kv("share_tag", share == null ? "-" : share.toString())
				.kv("potion", potion));
	}
}
