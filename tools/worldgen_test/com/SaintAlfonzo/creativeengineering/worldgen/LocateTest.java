package com.SaintAlfonzo.creativeengineering.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Headless checks of the locate counters (worldgen/LocateStats: candidates, loads, misses before a hit, stages, batch
 * capture, a stale search replaced) and of Distant Horizons' fake seabed roll and region detection (worldgen/DistantClip).
 * The mixins, chunk loads, census and the clipped noise only run in game (feature_writeup/locate.md 7).
 */
public class LocateTest {
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

	/** Its name is what DistantClip recognises Distant Horizons' WorldGenRegion by. */
	static class DhLitWorldGenRegionLike {}
	static class SubRegion extends DhLitWorldGenRegionLike {}

	static Holder<Structure> holder(String id, Structure s) {
		ResourceKey<Structure> key = ResourceKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(id));
		Holder.Reference<Structure> h = Holder.Reference.createStandAlone(null, key);
		h.bind(key, s);
		return h;
	}

	public static void main(String[] args) throws Exception {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();
		sun.misc.Unsafe unsafe;
		var f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		f.setAccessible(true);
		unsafe = (sun.misc.Unsafe) f.get(null);
		Structure tower = (Structure) unsafe.allocateInstance(Fake.class);
		Holder<Structure> h = holder("ns:tower", tower);

		// 1. one search: two misses, then a hit on the third candidate's only load
		HolderSet<Structure> set = HolderSet.direct(h);
		LocateStats.Search[] slot = LocateStats.capture(set);
		LocateStats.begin(set, new BlockPos(10, 64, 20));
		LocateStats.beginCall();                                 // candidate 1: predicted no
		LocateStats.predictedNo(LocateStats.Stage.BIOME);
		LocateStats.endCall(null);
		LocateStats.beginCall();                                 // candidate 2: loaded, nothing
		LocateStats.load();
		LocateStats.endCall(null);
		LocateStats.beginCall();                                 // candidate 3: loaded, nothing
		LocateStats.load();
		LocateStats.endCall(null);
		LocateStats.beginCall();                                 // candidate 4: loaded, hit
		LocateStats.load();
		Pair<BlockPos, Holder<Structure>> hit = Pair.of(new BlockPos(500, 0, -300), h);
		LocateStats.endCall(hit);
		LocateStats.end(hit);
		LocateStats.Search s = slot[0];
		check(s != null, "the batch's HolderSet captured its search");
		check(s != null && s.candidates == 4, "4 candidates counted");
		check(s != null && s.loads == 3, "3 loads counted");
		check(s != null && s.misses == 2, "2 misses before the hit (the hit's own load is not a miss)");
		check(s != null && s.saved[LocateStats.Stage.BIOME.ordinal()] == 1, "the biome stage's \"no\" counted");
		check(s != null && "ns:tower".equals(s.label), "labelled by the structure id");
		check(s != null && s.found != null && s.found.getX() == 500 && "ns:tower".equals(s.foundId), "the hit's position and id kept");
		LocateStats.release(set);

		// 2. a search with no hit: every load is a miss
		HolderSet<Structure> set2 = HolderSet.direct(h);
		LocateStats.Search[] slot2 = LocateStats.capture(set2);
		LocateStats.begin(set2, BlockPos.ZERO);
		for (int i = 0; i < 5; i++) {
			LocateStats.beginCall();
			LocateStats.load();
			LocateStats.endCall(null);
		}
		LocateStats.end(null);
		check(slot2[0] != null && slot2[0].misses == 5 && slot2[0].found == null, "no hit: 5 loads = 5 misses, nothing found");

		// 3. totals add up per structure
		var rows = LocateStats.totals();
		check(rows.size() == 1 && (Long) rows.get(0).get("searches") == 2 && (Long) rows.get(0).get("misses") == 7
				&& (Long) rows.get(0).get("missesBeforeHit") == 2 && (Long) rows.get(0).get("hits") == 1, "totals: 2 searches, 1 hit, 7 misses, 2 before the hit");

		// 4. a search that never ended (threw) is replaced, not nested into
		HolderSet<Structure> set3 = HolderSet.direct(h);
		LocateStats.begin(set3, BlockPos.ZERO);
		LocateStats.beginCall();
		LocateStats.load();
		LocateStats.Search[] slot4 = LocateStats.capture(set3);
		LocateStats.begin(set3, BlockPos.ZERO);                // no end() for the first
		LocateStats.end(null);
		check(slot4[0] != null && slot4[0].loads == 0, "a stale search on the thread is replaced by the next one");

		// 5. calls outside any search are ignored (no throw)
		LocateStats.beginCall();
		LocateStats.load();
		LocateStats.endCall(null);
		LocateStats.end(null);
		check(true, "counters outside a search are no-ops");
		LocateStats.resetTotals();
		check(LocateStats.totals().isEmpty(), "stats reset clears the totals");

		// 5b. search deadline and stop (structure_tuning.md 7.3.2)
		HolderSet<Structure> set5 = HolderSet.direct(holder("ns:end_tower", new Fake()));
		LocateStats.Search[] slot5 = LocateStats.capture(set5);
		LocateStats.begin(set5, BlockPos.ZERO);
		check(!LocateStats.expired(), "a fresh search is not expired");
		LocateStats.stopAll();
		check(LocateStats.expired() && "stopped".equals(slot5[0].ended), "/celocate stop: a running search expires, marked stopped");
		LocateStats.end(null);
		check((Long) LocateStats.totals().get(0).get("cutShort") == 1, "a search cut short is counted in the totals");
		LocateStats.release(set5);
		LocateStats.begin(set5, BlockPos.ZERO);
		check(!LocateStats.expired(), "a search started after the stop runs on");
		Thread.currentThread().interrupt();
		boolean interrupted = LocateStats.expired();
		Thread.interrupted();
		check(interrupted, "an interrupted search thread (Async Locator's cancel) expires");
		LocateStats.end(null);
		check(!LocateStats.expired(), "outside a search nothing expires");
		LocateStats.shutdown();
		check(LocateStats.expired(), "while the server stops, everything expires, outside a search too");
		LocateStats.resume();
		check(!LocateStats.expired(), "the next server start clears it");
		LocateStats.resetTotals();

		// 6. DistantClip: the fake seabed roll
		PositionalRandomFactory random = new XoroshiroRandomSource(8675309L).forkPositional();
		double min = 1, max = -1, maxStep = 0;
		boolean same = true;
		for (int x = -500; x < 500; x += 3) {
			for (int z = -500; z < 500; z += 7) {
				double v = DistantClip.roll(random, x, z, 24);
				min = Math.min(min, v);
				max = Math.max(max, v);
				maxStep = Math.max(maxStep, Math.abs(DistantClip.roll(random, x + 1, z, 24) - v));
				same &= v == DistantClip.roll(new XoroshiroRandomSource(8675309L).forkPositional(), x, z, 24);
			}
		}
		check(min >= -1 && max <= 1, "roll stays in [-1, 1] (" + String.format("%.2f", min) + " .. " + String.format("%.2f", max) + ")");
		check(min < -0.6 && max > 0.6, "roll uses most of its range");
		check(maxStep <= 2.0 * 1.5 / 24, "roll is smooth: one block changes it by at most " + String.format("%.3f", maxStep));
		check(same, "roll is fixed by the seed and position");
		check(DistantClip.roll(new XoroshiroRandomSource(1L).forkPositional(), 37, 41, 24) != DistantClip.roll(random, 37, 41, 24),
				"another seed gives another floor");

		// 7. DistantClip: Distant Horizons' region is recognised by class name, subclasses too; others are not
		check(DistantClip.isDistantLevel(new DhLitWorldGenRegionLike()), "a DhLitWorldGenRegion is recognised");
		check(DistantClip.isDistantLevel(new SubRegion()), "a subclass of it too");
		check(!DistantClip.isDistantLevel(new Object()) && !DistantClip.isDistantLevel(null), "anything else is not");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
