package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Headless checks of worldgen/StructureBiomeFits: staging (the real fits structure_overrides.js stages, read from the
 * node sim's fits.json, and bad input), the union of a structure's fits, and the fitted biome list (membership by
 * biome value). The mixin itself (Structure#biomes) only runs in game.
 * Usage: FitTest <fits.json written by tools/structure_tuning_sim.js>
 */
public class FitTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static final class Fake extends Structure {
		Fake() { super(null); }
		public Optional<GenerationStub> findGenerationPoint(GenerationContext c) { return Optional.empty(); }
		public StructureType<?> type() { return null; }
	}

	static Holder.Reference<Biome> holder(String id, Biome value) {
		ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(id));
		Holder.Reference<Biome> h = Holder.Reference.createStandAlone(null, key);
		h.bind(key, value);
		return h;
	}

	public static void main(String[] args) throws Exception {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		sun.misc.Unsafe unsafe;
		var f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		unsafe = (sun.misc.Unsafe) f.get(null);

		// 1. the real staged fits parse, and every structure keeps its fit chain
		String real = Files.readString(Path.of(args[0]));
		StructureBiomeFits.stage(real);
		var st = StructureBiomeFits.stagedForTest();
		check(st.structures().size() == 181 && st.fits().size() == 30, "real fits staged: " + st.structures().size() + " structures, " + st.fits().size() + " fits");
		check(st.structures().get("towns_and_towers:village_mushroom_fields").equals(List.of("land/mushroom", "land")),
				"mushroom village -> land/mushroom + land (was biome_merges.json)");
		check(st.fits().get("land").isEmpty(), "land lists no biome (parent of the land/* families)");
		check(st.structures().get("mysticrift_pillagers_towers_aris:oak_wood_pillager_tower").equals(List.of("land/oak", "land")),
				"oak tower -> land/oak + land (was tower/oak)");
		check(st.structures().get("mysticrift_pillagers_towers_aris:end_pillager_tower").equals(List.of("land/end")),
				"End tower -> land/end only (inherit false)");
		check(st.structures().get("desert_watchtower:watchtower").equals(List.of("land/sand", "land")),
				"desert watchtower -> land/sand + land (was tower/desert)");
		check(st.structures().get("forest_watchtower:forest_watchtower").containsAll(List.of("land/spruce", "land/dark_forest", "land")),
				"forest watchtower -> both its fits + land");
		check(st.structures().get("minecraft:pillager_outpost").equals(List.of("land/dark_forest", "land")),
				"vanilla pillager outpost -> land/dark_forest + land (by its dark oak)");
		var plainsFits = st.structures().get("minecraft:village_plains");
		check(plainsFits.size() == 3 && plainsFits.containsAll(List.of("land/plains", "land/highlands", "land")),
				"plains village -> land/plains + land/highlands + land: " + plainsFits);
		check(st.structures().get("skyvillages:skyvillage").equals(List.of("land/sky", "land"))
				&& st.fits().get("land/sky").contains("#minecraft:is_ocean"), "sky village -> land/sky (land + the oceans)");
		check(!st.structures().get("mysticrift_pillagers_towers_aris:oak_wood_pillager_tower").contains("land/spruce"),
				"no structure gets a sibling fit it isn't tagged with (oak tower has no land/spruce)");
		check(!st.structures().containsKey("philipsruins:start_nether_ruin"), "Philip's nether ruin is held at its own list");

		// 2. bad input stages nothing; "{}" clears
		StructureBiomeFits.stage("{\"fits\":{\"a\":[\"minecraft:plains\"]},\"structures\":{\"x:y\":[\"b\"]}}");
		check(StructureBiomeFits.stagedForTest().structures().isEmpty(), "a structure naming an unknown fit stages nothing");
		StructureBiomeFits.stage("{\"fits\":{\"a\":[\"minecraft:plains\"]},\"structures\":{\"x:y\":[]}}");
		check(StructureBiomeFits.stagedForTest().structures().isEmpty(), "a structure with no fit stages nothing");
		StructureBiomeFits.stage("not json");
		check(StructureBiomeFits.stagedForTest().fits().isEmpty(), "unparsable input stages nothing");
		StructureBiomeFits.stage(real);
		StructureBiomeFits.stage("{}");
		check(StructureBiomeFits.stagedForTest().structures().isEmpty(), "{} clears");

		// 3. resolve: union in fit order, unregistered ids and empty fits logged, empty lists still applied
		Map<String, List<String>> fits = new LinkedHashMap<>();
		fits.put("land", List.of("plains", "meadow"));
		fits.put("land/oak", List.of("forest", "plains"));
		fits.put("land/end", List.of());
		Map<String, List<String>> structures = new LinkedHashMap<>();
		structures.put("a:oak", List.of("land/oak", "land"));
		structures.put("a:end", List.of("land/end"));
		structures.put("a:gone", List.of("land"));
		Map<String, String> registered = Map.of("a:oak", "OAK", "a:end", "END");
		List<String> log = new ArrayList<>();
		Map<String, Set<String>> out = StructureBiomeFits.resolve(structures, fits, registered, s -> 5, log);
		check(List.copyOf(out.get("OAK")).equals(List.of("forest", "plains", "meadow")), "union in fit order, no duplicates: " + out.get("OAK"));
		check(out.containsKey("END") && out.get("END").isEmpty(), "an empty fit is applied (never spawns)");
		check(out.size() == 2, "an unregistered structure is skipped");
		check(log.stream().anyMatch(l -> l.contains("a:end") && l.contains("never spawns")), "empty result logged");
		check(log.stream().anyMatch(l -> l.contains("not registered") && l.contains("a:gone")), "unregistered id logged");
		check(log.stream().anyMatch(l -> l.contains("a:oak 5 -> 3")), "old and new sizes logged");

		// 4. the fitted list compares biome values, not holder instances
		Biome plains = (Biome) unsafe.allocateInstance(Biome.class);
		Biome forest = (Biome) unsafe.allocateInstance(Biome.class);
		Biome ocean = (Biome) unsafe.allocateInstance(Biome.class);
		var list = new StructureBiomeFits.FitBiomes(List.of(holder("minecraft:plains", plains), holder("minecraft:forest", forest)));
		check(list.size() == 2 && list.get(1).value() == forest, "size and order");
		check(list.contains(holder("minecraft:plains", plains)), "another holder of a listed biome is in the list");
		check(!list.contains(holder("minecraft:ocean", ocean)), "an unlisted biome is not");
		ResourceKey<Biome> unboundKey = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation("minecraft:plains"));
		check(!list.contains(Holder.Reference.createStandAlone(null, unboundKey)), "an unbound holder is not (no throw)");
		check(list.unwrap().right().map(l -> l.size() == 2).orElse(false) && list.unwrap().left().isEmpty(), "unwraps to its list, no tag key");

		// 5. biomesFor: the table's list for a fitted structure, null for the rest and when nothing is active
		Structure fitted = new Fake(), other = new Fake();
		Map<Structure, HolderSet<Biome>> table = new IdentityHashMap<>();
		table.put(fitted, list);
		StructureBiomeFits.setActiveForTest(table);
		check(StructureBiomeFits.biomesFor(fitted) == list, "a fitted structure answers with its list");
		check(StructureBiomeFits.biomesFor(other) == null, "an unfitted structure keeps its own");
		StructureBiomeFits.setActiveForTest(Map.of());
		check(StructureBiomeFits.biomesFor(fitted) == null, "nothing active: every structure keeps its own");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
