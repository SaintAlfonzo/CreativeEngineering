package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Headless checks of worldgen/MetaStructure: the codec on the real meta_structures.json and on bad input, the
 * anchor's frequency roll (StructureFrequencies#anchorChosen), and the layout geometry (reach, clearance).
 * Usage: MetaTest <instance root>
 */
public class MetaTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static final class Fake extends Structure {
		final String id;
		Fake(String id) { super(null); this.id = id; }
		Fake(String id, StructureSettings settings) { super(settings); this.id = id; }
		public Optional<GenerationStub> findGenerationPoint(GenerationContext c) { return Optional.empty(); }
		public StructureType<?> type() { return null; }
	}

	static final class Box extends StructurePiece {
		Box(int x0, int z0, int x1, int z1) { super(StructurePieceType.JIGSAW, 0, new BoundingBox(x0, 60, z0, x1, 70, z1)); }
		protected void addAdditionalSaveData(StructurePieceSerializationContext c, net.minecraft.nbt.CompoundTag t) {}
		public void postProcess(WorldGenLevel l, StructureManager m, ChunkGenerator g, RandomSource r, BoundingBox b, ChunkPos c, BlockPos p) {}
	}

	static StructureSet.StructureSelectionEntry entry(Structure s, String id, int weight) {
		ResourceKey<Registry<Structure>> rk = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/structure"));
		ResourceKey<Structure> key = ResourceKey.create(rk, new ResourceLocation(id));
		Holder.Reference<Structure> h = Holder.Reference.createStandAlone(null, key);
		h.bind(key, s);
		return new StructureSet.StructureSelectionEntry(h, weight);
	}

	static MetaStructure.Placed placed(Structure s, StructurePiece... pieces) {
		return new MetaStructure.Placed(s, BlockPos.ZERO, List.of(pieces), StructurePiece.createBoundingBox(java.util.Arrays.stream(pieces)));
	}

	public static void main(String[] args) throws Exception {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		Path root = Path.of(args[0]);
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.BUILTIN.get());

		// 1. the real definitions parse, as the game will read them (our "note" dropped by structure_overrides.js)
		JsonObject defs = JsonParser.parseString(Files.readString(root.resolve("kubejs/generation_scripts/worldgen/meta_structures.json")))
				.getAsJsonObject().getAsJsonObject("structures");
		Map<String, MetaStructure> parsed = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> e : defs.entrySet()) {
			JsonObject j = e.getValue().getAsJsonObject().deepCopy();
			j.remove("note");
			DataResult<MetaStructure> r = MetaStructure.CODEC.parse(ops, j);
			check(r.result().isPresent(), "parses: " + e.getKey() + r.error().map(err -> " (" + err.message() + ")").orElse(""));
			r.result().ifPresent(m -> parsed.put(e.getKey(), m));
		}
		MetaStructure villages = parsed.get("creativeengineering:meta/village_towers");
		if (villages != null) {
			MetaStructure.Part p = partsOf(villages).get(0);
			check(p.structures().orElseThrow().equals(List.of("#creativeengineering:meta/peaceful_towers")) && p.count().min() == 1 && p.count().max() == 3
					&& p.distance().min() == 40 && p.distance().max() == 104 && p.spacing() == 48 && p.margin() == 6 && p.tries() == 16,
					"village_towers: towers part read with its fields and defaults");
			check(villages.terrainAdaptation() == TerrainAdjustment.BEARD_THIN && villages.step() == GenerationStep.Decoration.SURFACE_STRUCTURES,
					"village_towers: beard_thin, surface_structures");
		}
		MetaStructure flotilla = parsed.get("creativeengineering:meta/driftwood_flotilla");
		if (flotilla != null) {
			List<MetaStructure.Part> parts = partsOf(flotilla);
			check(parts.size() == 2 && parts.get(0).chance() == 0.25f && parts.get(0).count().min() == 1
					&& parts.get(1).features().orElseThrow().equals(List.of("creativeengineering:meta/flotilla_driftwood"))
					&& parts.get(1).count().min() == 8 && parts.get(1).waterDepth() == 2 && parts.get(1).radius() == 6,
					"driftwood_flotilla: optional raft + 8-16 driftwood over 2+ deep water");
		}
		MetaStructure struck = parsed.get("creativeengineering:meta/meteorite_village");
		if (struck != null) {
			MetaStructure.Part p = partsOf(struck).get(0);
			check(p.overlap() && p.structures().orElseThrow().equals(List.of("#creativeengineering:meta/meteorites")) && p.distance().max() == 16,
					"meteorite_village: the meteorite part overlaps the village");
		}
		check(villages == null || !partsOf(villages).get(0).overlap(), "overlap defaults to false");
		MetaStructure siege = parsed.get("creativeengineering:meta/village_siege_camp");
		if (siege != null) check(partsOf(siege).get(0).cluster() == 40 && partsOf(siege).get(0).spacing() == 18, "village_siege_camp: siege weapons clustered within 40");
		check(villages == null || partsOf(villages).get(0).cluster() == 0, "cluster defaults to 0 (off)");

		// 2. bad input is a codec error (structure_overrides.js checks the same, so it never reaches the game)
		String base = "{\"biomes\":[],\"step\":\"surface_structures\",\"spawn_overrides\":{},\"parts\":[%s]}";
		check(MetaStructure.CODEC.parse(ops, JsonParser.parseString(String.format(base, "{\"structures\":\"a:b\"}"))).result().isPresent(),
				"the minimal valid definition parses (so the rejections below are about the part)");
		String[][] bad = {
			{"{\"structures\":\"a:b\",\"features\":\"c:d\"}", "structures and features"},
			{"{\"count\":2}", "neither structures nor features"},
			{"{\"structures\":\"a:b\",\"distance\":[0,200]}", "distance past the reach"},
			{"{\"structures\":\"a:b\",\"count\":[3,1]}", "count [3, 1]"},
			{"{\"structures\":\"#Not An Id\"}", "a bad tag"},
			{"{\"structures\":\"a:b\",\"chance\":1.5}", "chance 1.5"},
			{"{\"structures\":\"a:b\",\"overlap\":\"yes\"}", "overlap \"yes\""},
			{"{\"features\":\"c:d\",\"cluster\":20}", "cluster on a feature part"},
			{"{\"structures\":\"a:b\",\"cluster\":200}", "cluster past the reach"},
		};
		for (String[] b : bad) {
			DataResult<MetaStructure> r = MetaStructure.CODEC.parse(ops, JsonParser.parseString(String.format(base, b[0])));
			check(r.result().isEmpty(), "rejects " + b[1]);
		}
		DataResult<MetaStructure> one = MetaStructure.CODEC.parse(ops, JsonParser.parseString(String.format(base, "{\"structures\":[\"a:b\",\"#c:d\"],\"count\":2}")));
		one.error().ifPresent(err -> System.out.println("     (" + err.message() + ")"));
		check(one.result().map(m -> partsOf(m).get(0).count().min() == 2 && partsOf(m).get(0).count().max() == 2
				&& partsOf(m).get(0).structures().orElseThrow().size() == 2).orElse(false), "count 2 = [2, 2]; a list of entries");
		check(one.result().flatMap(m -> MetaStructure.CODEC.encodeStart(ops, m).result()).flatMap(j -> MetaStructure.CODEC.parse(ops, j).result())
				.map(m -> partsOf(m).get(0).count().max() == 2).orElse(false), "encodes and reads back");

		// 3. the anchor's frequency: a meta start rolls its anchor's own roll, and a miss vetoes the slot
		Fake anchor = new Fake("ns:village"), other = new Fake("ns:other_village");
		MetaStructure meta = new MetaStructure(new Structure.StructureSettings(HolderSet.direct(), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.NONE), Optional.of(List.of("ns:village")), 0.5F, List.of());
		StructureSet group = new StructureSet(List.of(entry(anchor, "ns:village", 100), entry(other, "ns:other_village", 100),
				entry(meta, "creativeengineering:meta/test", 50)), null);
		Map<String, Map<String, Float>> table = Map.of("ns:group", Map.of("ns:village", 0.25F));
		StructureFrequencies.setActiveForTest(StructureFrequencies.resolve(table, id -> Optional.of(group)));
		long seed = 123456789L;
		int kept = 0, same = 0, n = 20000, plain = 0, stale = 0, vetoOk = 0;
		for (int i = 0; i < n; i++) {
			int x = i % 211 - 100, z = i / 211 - 50;
			Object chunk = new Object();
			if (!StructureFrequencies.reject(meta, seed, x, z, chunk)) plain++;   // no anchor chosen yet on this thread
			StructureFrequencies.anchorChosen(meta, anchor, x, z);
			boolean metaKept = !StructureFrequencies.reject(meta, seed, x, z, chunk);
			if (!metaKept && StructureFrequencies.vetoed(other, chunk)) vetoOk++;
			boolean anchorKept = !StructureFrequencies.reject(anchor, seed, x, z, new Object());
			if (metaKept) kept++;
			if (metaKept == anchorKept) same++;
			if (!StructureFrequencies.reject(meta, seed, x + 1, z, new Object())) stale++;   // another chunk: the choice doesn't apply
			StructureFrequencies.anchorChosen(meta, null, 0, 0);
		}
		check(plain == n, "a meta start with no anchor chosen rolls only its own frequency (1 here): " + plain + "/" + n);
		check(Math.abs(kept / (double) n - 0.25) < 0.015, "with the anchor chosen it keeps " + kept / (double) n + " of its slots (anchor frequency 0.25)");
		check(same == n, "the meta's roll is exactly its anchor's roll at that chunk (" + same + "/" + n + ")");
		check(vetoOk == n - kept, "a missed anchor roll vetoes the rest of the group's picks in that slot (" + vetoOk + "/" + (n - kept) + ")");
		check(stale == n, "an anchor chosen for one chunk doesn't roll at another (" + stale + "/" + n + ")");
		StructureFrequencies.setActiveForTest(StructureFrequencies.Active.EMPTY);

		// 4. layout: reach (7 chunks around the start chunk), clearance between parts, feature spots
		MetaStructure.Layout layout = new MetaStructure.Layout(new ChunkPos(0, 0));
		check(layout.inReach(7, -7) && !layout.inReach(8, 0) && !layout.inReach(0, -8), "reach: 7 chunks each way, not 8");
		check(layout.inReach(placed(anchor, new Box(-112, -112, 127, 127))) && !layout.inReach(placed(anchor, new Box(-113, 0, 0, 0)))
				&& !layout.inReach(placed(anchor, new Box(0, 0, 128, 0))), "reach of a placed structure: every piece within blocks -112..127");
		layout.add(placed(anchor, new Box(-20, -20, 20, 20), new Box(21, -3, 60, 3)));
		check(layout.entries.size() == 1 && layout.entries.get(0).from() == 0 && layout.entries.get(0).to() == 2 && layout.pieces.size() == 2,
				"index entry covers the anchor's piece range");
		check(layout.collides(placed(other, new Box(64, -4, 72, 4)), 6) && !layout.collides(placed(other, new Box(67, -4, 75, 4)), 6),
				"collision: a tower 4 blocks from the road collides at margin 6, 7 blocks clears it");
		check(!layout.collides(placed(other, new Box(30, 10, 40, 20)), 0) && layout.collides(placed(other, new Box(30, 3, 40, 20)), 0),
				"collision is per piece, not the anchor's whole box (the corner beside the road is free)");
		check(layout.covers(62, 0, 2) && !layout.covers(63, 0, 2) && layout.covers(0, 0, 0), "feature spots keep clear of structure pieces by margin");
		layout.add(placed(other, new Box(70, -4, 78, 4)));
		check(layout.entries.get(1).from() == 2 && layout.entries.get(1).to() == 3, "a satellite's entry follows the anchor's");

		// 5. slot conversion (MetaSlots): a meta only takes slots its anchors won, so anchor rates and group slots stay put
		Fake ruinA = new Fake("ns:ruin_a"), ruinB = new Fake("ns:ruin_b"), camp = new Fake("ns:camp");
		MetaStructure m1 = new MetaStructure(new Structure.StructureSettings(HolderSet.direct(), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.NONE), Optional.of(List.of("ns:ruin_a", "ns:ruin_b")), 0.3F, List.of());
		MetaStructure m2 = new MetaStructure(new Structure.StructureSettings(HolderSet.direct(), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.NONE), Optional.of(List.of("ns:ruin_a")), 0.9F, List.of());
		MetaStructure lonely = new MetaStructure(new Structure.StructureSettings(HolderSet.direct(), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.NONE), Optional.of(List.of("ns:elsewhere")), 0.5F, List.of());
		StructureSet ruins = new StructureSet(List.of(entry(ruinA, "ns:ruin_a", 100), entry(ruinB, "ns:ruin_b", 100), entry(camp, "ns:camp", 100),
				entry(m1, "creativeengineering:meta/m1", 1), entry(m2, "creativeengineering:meta/m2", 1), entry(lonely, "creativeengineering:meta/lonely", 1)), null);
		Map<Structure, String> ids = new java.util.IdentityHashMap<>();
		ids.put(ruinA, "ns:ruin_a"); ids.put(ruinB, "ns:ruin_b"); ids.put(camp, "ns:camp");
		ids.put(m1, "creativeengineering:meta/m1"); ids.put(m2, "creativeengineering:meta/m2"); ids.put(lonely, "creativeengineering:meta/lonely");
		Map<MetaStructure, List<Structure>> cands = new java.util.IdentityHashMap<>();
		cands.put(m1, List.of(ruinA, ruinB)); cands.put(m2, List.of(ruinA)); cands.put(lonely, List.of(new Fake("ns:elsewhere")));
		MetaSlots.Index idx = MetaSlots.build(List.of(ruins), ids::get, cands::get);
		List<MetaSlots.Conversion> forA = idx.byAnchor.get(ruinA);
		check(forA.size() == 2 && forA.get(0).target() == m1 && forA.get(0).from() == 0 && Math.abs(forA.get(0).to() - 0.3) < 1e-6
				&& forA.get(1).target() == m2 && Math.abs(forA.get(1).from() - 0.3) < 1e-6 && forA.get(1).to() == 1.0,
				"shares stack per anchor in id order and are cut at 100% (m1 0..0.3, m2 0.3..1)");
		check(idx.warnings.stream().anyMatch(w -> w.contains("ns:ruin_a") && w.contains("120%")) && idx.warnings.stream().anyMatch(w -> w.contains("lonely")),
				"warns about the over-full anchor and the meta with no anchor in its set: " + idx.warnings);
		check(!idx.byAnchor.containsKey(camp) && idx.anchorsOf.get(lonely).isEmpty(), "a non-anchor and an anchor outside the set convert nothing");
		Map<String, Map<String, Float>> ruinFreq = Map.of("ns:ruins", Map.of("ns:ruin_a", 0.5F));
		StructureFrequencies.setActiveForTest(StructureFrequencies.resolve(ruinFreq, id -> Optional.of(ruins)));
		int aKept = 0, aConv = 0, aConvWhileRejected = 0, bConv = 0, campConv = 0, direct = 0, m1FromA = 0, nn = 40000;
		for (int i = 0; i < nn; i++) {
			int x = i % 199 - 99, z = i / 199 - 100;
			boolean aKeeps = StructureFrequencies.keepsAt(ruinA, seed, x, z);
			MetaSlots.Decision dA = MetaSlots.decide(idx, ruinA, seed, x, z);
			if (aKeeps) aKept++;
			if (dA.convert()) { aConv++; if (!aKeeps) aConvWhileRejected++; if (dA.targetEntry().structure().value() == m1) m1FromA++; }
			if (MetaSlots.decide(idx, ruinB, seed, x, z).convert()) bConv++;
			if (MetaSlots.decide(idx, camp, seed, x, z).convert()) campConv++;
			if (MetaSlots.decide(idx, m1, seed, x, z).skip() && MetaSlots.decide(idx, m2, seed, x, z).skip()) direct++;
		}
		check(aConvWhileRejected == 0 && aConv == aKept, "an anchor converts only slots it keeps; ruin_a's shares fill 100%, so every kept slot converts ("
				+ aConv + "/" + aKept + ")");
		check(Math.abs(m1FromA / (double) aConv - 0.3) < 0.015, "m1 gets " + m1FromA / (double) aConv + " of ruin_a's slots (share 0.3)");
		check(Math.abs(bConv / (double) nn - 0.3) < 0.015 && campConv == 0, "ruin_b converts " + bConv / (double) nn + " of its slots, the camp none");
		check(direct == nn, "a converted meta picked directly by its group never generates (" + direct + "/" + nn + ")");
		MetaSlots.Decision conv = null;
		int cx = 0;
		for (; cx < 1000 && (conv == null || !conv.convert()); cx++) conv = MetaSlots.decide(idx, ruinB, seed, cx, 7);
		cx--;
		MetaSlots.beginConversion(conv.targetEntry(), entry(ruinB, "ns:ruin_b", 100), new ChunkPos(cx, 7));
		check(!MetaSlots.decide(idx, m1, seed, cx, 7).skip() && MetaSlots.decide(idx, m1, seed, cx + 1, 7).skip()
				&& MetaSlots.forcedAnchor(m1, new ChunkPos(cx, 7)) == ruinB && MetaSlots.forcedAnchor(m2, new ChunkPos(cx, 7)) == null,
				"inside a conversion the meta generates there, on that anchor, and nowhere else");
		MetaSlots.endConversion();
		check(MetaSlots.forcedAnchor(m1, new ChunkPos(cx, 7)) == null, "after it, no forced anchor is left on the thread");
		StructureFrequencies.setActiveForTest(StructureFrequencies.Active.EMPTY);

		// 6. replacements (MetaSlots): a plain structure takes "share" of another member's kept slots, never its own
		ResourceKey<Registry<net.minecraft.world.level.biome.Biome>> bk = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/biome"));
		Holder.Reference<net.minecraft.world.level.biome.Biome> swampB = Holder.Reference.createStandAlone(null, ResourceKey.create(bk, new ResourceLocation("ns:swamp")));
		Holder.Reference<net.minecraft.world.level.biome.Biome> plainsB = Holder.Reference.createStandAlone(null, ResourceKey.create(bk, new ResourceLocation("ns:plains")));
		Fake hut = new Fake("ns:hut", new Structure.StructureSettings(HolderSet.direct(List.of(swampB)), Map.of(),
				GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE));
		Fake circle = new Fake("ns:circle"), vanillaHut = new Fake("ns:swamp_hut"), orphan = new Fake("ns:orphan");
		MetaStructure hutMeta = new MetaStructure(new Structure.StructureSettings(HolderSet.direct(), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.NONE), Optional.of(List.of("ns:hut")), 0.95F, List.of());
		StructureSet witch = new StructureSet(List.of(entry(hut, "ns:hut", 172), entry(circle, "ns:circle", 62), entry(vanillaHut, "ns:swamp_hut", 1),
				entry(orphan, "ns:orphan", 1)), null);
		Map<Structure, String> wIds = new java.util.IdentityHashMap<>();
		wIds.put(hut, "ns:hut"); wIds.put(circle, "ns:circle"); wIds.put(vanillaHut, "ns:swamp_hut"); wIds.put(orphan, "ns:orphan");
		wIds.put(hutMeta, "creativeengineering:meta/hut_meta");
		Map<String, Structure> byId = Map.of("ns:hut", hut, "ns:circle", circle, "ns:swamp_hut", vanillaHut, "ns:orphan", orphan);
		Map<Structure, MetaSlots.Replacement> repl = new java.util.IdentityHashMap<>();
		repl.put(vanillaHut, new MetaSlots.Replacement("ns:hut", 0.1));
		repl.put(orphan, new MetaSlots.Replacement("ns:elsewhere", 0.5));
		MetaSlots.Index wIdx = MetaSlots.build(List.of(witch), wIds::get, m -> List.of(), repl::get, byId::get);
		List<MetaSlots.Conversion> forHut = wIdx.byAnchor.get(hut);
		check(forHut.size() == 1 && forHut.get(0).target() == vanillaHut && forHut.get(0).from() == 0 && Math.abs(forHut.get(0).to() - 0.1) < 1e-9
				&& wIdx.replacements == 2 && !wIdx.byAnchor.containsKey(circle),
				"the swamp hut takes 0..0.1 of the YUNG hut's roll, nothing of the circle's");
		check(wIdx.anchorsOf.get(orphan).isEmpty() && wIdx.warnings.stream().anyMatch(w -> w.contains("ns:orphan") && w.contains("ns:elsewhere")),
				"a replacement whose structure is not in its set warns and never generates: " + wIdx.warnings);
		Map<String, Map<String, Float>> hutFreq = Map.of("ns:witch", Map.of("ns:hut", 0.8F));
		StructureFrequencies.setActiveForTest(StructureFrequencies.resolve(hutFreq, id -> Optional.of(witch)));
		int hutKept = 0, hutConv = 0, convRejected = 0, circleConv = 0, directVanilla = 0;
		for (int i = 0; i < nn; i++) {
			int x = i % 199 - 99, z = i / 199 - 100;
			boolean keeps = StructureFrequencies.keepsAt(hut, seed, x, z);
			MetaSlots.Decision d = MetaSlots.decide(wIdx, hut, seed, x, z);
			if (keeps) hutKept++;
			if (d.convert()) { hutConv++; if (!keeps || d.targetEntry().structure().value() != vanillaHut) convRejected++; }
			if (MetaSlots.decide(wIdx, circle, seed, x, z).convert()) circleConv++;
			if (MetaSlots.decide(wIdx, vanillaHut, seed, x, z).skip() && MetaSlots.decide(wIdx, orphan, seed, x, z).skip()) directVanilla++;
		}
		check(convRejected == 0 && Math.abs(hutConv / (double) hutKept - 0.1) < 0.01 && circleConv == 0,
				"the swamp hut takes " + hutConv / (double) hutKept + " of the YUNG hut's kept slots (share 0.1), none it rejected, none of the circle's");
		check(directVanilla == nn, "the swamp hut picked by its own weight never generates (" + directVanilla + "/" + nn + ")");
		Predicate<Holder<net.minecraft.world.level.biome.Biome>> any = b -> true;
		check(MetaSlots.narrowBiomes(vanillaHut, any) == any, "outside a conversion a replacement keeps its own biome check");
		cx = 0;
		for (conv = null; cx < 1000 && (conv == null || !conv.convert()); cx++) conv = MetaSlots.decide(wIdx, hut, seed, cx, 3);
		cx--;
		MetaSlots.beginConversion(conv.targetEntry(), entry(hut, "ns:hut", 172), new ChunkPos(cx, 3));
		Predicate<Holder<net.minecraft.world.level.biome.Biome>> narrowed = MetaSlots.narrowBiomes(vanillaHut, any);
		check(!MetaSlots.decide(wIdx, vanillaHut, seed, cx, 3).skip() && narrowed.test(swampB) && !narrowed.test(plainsB)
				&& MetaSlots.narrowBiomes(circle, any) == any,
				"in the YUNG hut's slot the swamp hut generates, only where the YUNG hut's biomes allow it");
		MetaSlots.endConversion();
		MetaSlots.Index both = MetaSlots.build(List.of(new StructureSet(List.of(entry(hut, "ns:hut", 172), entry(vanillaHut, "ns:swamp_hut", 1),
				entry(hutMeta, "creativeengineering:meta/hut_meta", 1)), null)), wIds::get, m -> List.of(hut), repl::get, byId::get);
		List<MetaSlots.Conversion> forBoth = both.byAnchor.get(hut);
		check(forBoth.size() == 2 && forBoth.get(0).target() == hutMeta && forBoth.get(1).target() == vanillaHut
				&& Math.abs(forBoth.get(1).from() - 0.95) < 1e-6 && forBoth.get(1).to() == 1.0
				&& both.warnings.stream().anyMatch(w -> w.contains("metas and replacements") && w.contains("105%")),
				"a meta and a replacement on one anchor stack in id order and are cut at 100%: " + both.warnings);
		StructureFrequencies.setActiveForTest(StructureFrequencies.Active.EMPTY);

		// 7. companions (PlacementPriority): 1 to 3 chunks 7 to 10 out around a town, spread, deterministic
		PlacementPriority.Companion rule = new PlacementPriority.Companion(null, null, List.of(),
				new PlacementPriority.CompanionSpec(List.of("ns:village"), 1, 3, 7, 10, 94));
		int[] counts = new int[4];
		boolean inRange = true, spread = true, stable = true;
		for (int i = 0; i < 2000; i++) {
			ChunkPos town = new ChunkPos(i % 61 - 30, i / 61 - 16);
			List<ChunkPos> out = PlacementPriority.companionChunks(rule, seed, town);
			stable &= out.equals(PlacementPriority.companionChunks(rule, seed, town));
			counts[Math.min(3, out.size())]++;
			for (ChunkPos p : out) {
				double d = Math.hypot(p.x - town.x, p.z - town.z);
				inRange &= d >= 6.25 && d <= 10.75;   // both coordinates rounded: up to 0.71 off
				for (ChunkPos q : out) if (q != p) spread &= Math.hypot(p.x - q.x, p.z - q.z) >= 6;
			}
		}
		check(counts[0] == 0 && counts[1] > 500 && counts[2] > 500 && counts[3] > 500, "companion counts 1..3 about evenly: " + java.util.Arrays.toString(counts));
		check(inRange, "companions 7 to 10 chunks out (rounding aside)");
		check(spread && stable, "companions at least 6 chunks apart, and the same every time");

		// 8. peaceful radius (PlacementPriority): square in XZ around the peaceful pieces, height ignored
		List<BoundingBox> campBoxes = List.of(new BoundingBox(0, 60, 0, 15, 70, 15));
		check(PlacementPriority.withinRadius(campBoxes, 3, List.of(new BoundingBox(63, 60, 0, 70, 80, 8))), "a tower 48 blocks off a camp's edge is within 3 chunks");
		check(!PlacementPriority.withinRadius(campBoxes, 3, List.of(new BoundingBox(64, 60, 0, 70, 80, 8))), "a tower 49 blocks off is not");
		check(PlacementPriority.withinRadius(campBoxes, 3, List.of(new BoundingBox(60, -40, 60, 62, -30, 62))), "the square's corner counts, and depth does not matter");
		check(!PlacementPriority.withinRadius(campBoxes, 3, List.of(new BoundingBox(-80, 60, 5, -50, 70, 9))), "west side: 50 blocks off is outside");
		check(PlacementPriority.withinRadius(List.of(new BoundingBox(500, 60, 500, 510, 70, 510), campBoxes.get(0)), 1,
				List.of(new BoundingBox(20, 60, 20, 30, 70, 30))), "any peaceful piece counts, not only the first");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}

	@SuppressWarnings("unchecked")
	static List<MetaStructure.Part> partsOf(MetaStructure m) {
		try {
			java.lang.reflect.Field f = MetaStructure.class.getDeclaredField("parts");
			f.setAccessible(true);
			return (List<MetaStructure.Part>) f.get(m);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
