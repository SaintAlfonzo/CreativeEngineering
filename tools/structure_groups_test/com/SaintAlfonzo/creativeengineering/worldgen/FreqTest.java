package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Headless checks of StructureFrequencies: the roll, table resolution, the slot veto, prediction parity. */
public class FreqTest {
	static int fails = 0, checks = 0, quietFails = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static void quiet(boolean ok, String what) {
		if (!ok && quietFails++ < 3) System.out.println("FAIL " + what);
	}

	static final class Fake extends Structure {
		final String id;
		Fake(String id) { super(null); this.id = id; }
		public Optional<GenerationStub> findGenerationPoint(GenerationContext c) { return Optional.empty(); }
		public StructureType<?> type() { return null; }
	}

	static ResourceKey<Registry<Structure>> registryKey;

	static StructureSet.StructureSelectionEntry entry(Fake s, int weight) {
		if (registryKey == null) registryKey = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/structure"));
		ResourceKey<Structure> key = ResourceKey.create(registryKey, new ResourceLocation(s.id));
		Holder.Reference<Structure> h = Holder.Reference.createStandAlone(null, key);
		h.bind(key, s);
		return new StructureSet.StructureSelectionEntry(h, weight);
	}

	public static void main(String[] args) {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		long seed = 7992144288592822000L;

		// 1. the roll: uniform, deterministic, independent per structure and per chunk
		int n = 400000, below = 0;
		double sum = 0;
		for (int i = 0; i < n; i++) {
			double r = StructureFrequencies.roll(seed, i % 631 - 300, i / 631 - 300, "minecraft:village_plains".hashCode());
			sum += r;
			if (r < 0.3) below++;
		}
		check(Math.abs(sum / n - 0.5) < 0.005, "roll mean " + sum / n);
		check(Math.abs(below / (double) n - 0.3) < 0.005, "roll P(<0.3) " + below / (double) n);
		check(StructureFrequencies.roll(seed, 5, -9, 42) == StructureFrequencies.roll(seed, 5, -9, 42), "roll deterministic");
		int same = 0, corr = 0;
		for (int i = 0; i < 10000; i++) {
			if ((StructureFrequencies.roll(seed, i, 0, 1) < 0.5) == (StructureFrequencies.roll(seed, i, 0, 2) < 0.5)) same++;
			if ((StructureFrequencies.roll(seed, i, 0, 1) < 0.5) == (StructureFrequencies.roll(seed, i + 1, 0, 1) < 0.5)) corr++;
		}
		check(Math.abs(same / 10000.0 - 0.5) < 0.03, "two structures' rolls independent (" + same + "/10000 agree)");
		check(Math.abs(corr / 10000.0 - 0.5) < 0.03, "neighbouring chunks independent (" + corr + "/10000 agree)");

		// 2. resolving a staged table against loaded sets
		Fake a = new Fake("ns:a"), b = new Fake("ns:b"), c = new Fake("ns:c"), x = new Fake("ns:x");
		StructureSet set = new StructureSet(List.of(entry(a, 3), entry(b, 1)), null);
		StructureSet other = new StructureSet(List.of(entry(c, 1)), null);
		Map<String, Map<String, Float>> table = new LinkedHashMap<>();
		table.put("ns:set", Map.of("ns:a", 0.25F));
		table.put("ns:other", Map.of("ns:c", 0.5F, "ns:ghost", 0.1F));
		table.put("ns:missing", Map.of("ns:x", 0.5F));
		Map<String, StructureSet> sets = Map.of("ns:set", set, "ns:other", other);
		StructureFrequencies.Active act = StructureFrequencies.resolve(table, id -> Optional.ofNullable(sets.get(id)));
		check(act.sets == 2 && act.rolled == 2, "resolve: 2 sets, 2 rolled members (unloaded set + unknown member skipped)");
		check(act.members.get(b) != null && act.members.get(b).frequency() == 1.0F, "resolve: B (no frequency) registered at 1, so a veto reaches it");
		check(act.members.get(x) == null, "resolve: structure of an unloaded set ignored");
		StructureFrequencies.setActiveForTest(act);

		// 3. the roll + the veto on one slot
		int kept = 0, trials = 20000;
		for (int i = 0; i < trials; i++) {
			Object chunk = new Object();
			if (!StructureFrequencies.reject(a, seed, i, 7, chunk)) {
				kept++;
				continue;
			}
			quiet(StructureFrequencies.vetoed(b, chunk), "B vetoed after A was rejected in the same slot");
			quiet(!StructureFrequencies.vetoed(c, chunk), "C (another set) not vetoed");
			quiet(!StructureFrequencies.vetoed(b, new Object()), "B not vetoed in another chunk");
			quiet(!StructureFrequencies.vetoed(x, chunk), "a structure without a table entry is never vetoed");
		}
		check(Math.abs(kept / (double) trials - 0.25) < 0.01, "A kept " + kept / (double) trials + " of its slots (frequency 0.25)");
		check(quietFails == 0, "veto scoping over " + trials + " slots (" + quietFails + " failures)");
		boolean bRejected = false;
		for (int i = 0; i < 5000; i++) if (StructureFrequencies.reject(b, seed, i, 1, new Object())) bRejected = true;
		check(!bRejected, "a frequency-1 member is never rejected by its own roll");
		int agree = 0;
		for (int i = 0; i < 2000; i++) {
			boolean predicted = !StructureFrequencies.keeps(act.members.get(a), seed, i, 3);
			if (predicted == StructureFrequencies.reject(a, seed, i, 3, new Object())) agree++;
		}
		check(agree == 2000, "StructureCheck prediction == generation roll (" + agree + "/2000)");

		// 4. bad staging input is logged, never thrown into KubeJS
		StructureFrequencies.stage("{\"ns:set\":{\"ns:a\":1.5}}");
		StructureFrequencies.stage("not json");
		check(true, "bad staging input logged, not thrown");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
