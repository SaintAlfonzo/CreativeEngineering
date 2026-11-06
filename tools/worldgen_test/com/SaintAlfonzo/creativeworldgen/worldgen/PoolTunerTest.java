package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless checks of PoolTuner's canonical pool order (testing/structures/05_phase_5.md 3.3) and of the processor-list
 * rebind for pieces added in code (05_phase_5.md 3.2). The weight tuning itself runs in game (structure_tuning.md 3.5).
 */
public class PoolTunerTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static StructurePoolElement single(String id) {
		return StructurePoolElement.single(id).apply(StructureTemplatePool.Projection.RIGID);
	}

	static Holder<?> processors(SinglePoolElement el) throws IllegalAccessException {
		for (Field f : SinglePoolElement.class.getDeclaredFields()) {
			if (f.getType() == Holder.class) {
				f.setAccessible(true);
				return (Holder<?>) f.get(el);
			}
		}
		throw new IllegalStateException("no Holder field");
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		net.minecraft.SharedConstants.tryDetectVersion();
		net.minecraft.server.Bootstrap.bootStrap();

		// 1. canonical order: by key, then weight; the same whatever order the pieces came in
		StructurePoolElement a = single("ns:a"), b = single("ns:b"), c = single("other:c"), b2 = single("ns:b");
		List<Pair<StructurePoolElement, Integer>> one = List.of(Pair.of(c, 1), Pair.of(b, 3), Pair.of(a, 2), Pair.of(b2, 1));
		List<Pair<StructurePoolElement, Integer>> two = List.of(Pair.of(b2, 1), Pair.of(a, 2), Pair.of(c, 1), Pair.of(b, 3));
		List<Pair<StructurePoolElement, Integer>> s1 = PoolTuner.canonical(one), s2 = PoolTuner.canonical(two);
		List<String> keys = new ArrayList<>();
		for (Pair<StructurePoolElement, Integer> p : s1) keys.add(PoolTuner.elementKey(p.getFirst()) + "=" + p.getSecond());
		check(keys.equals(List.of("ns:a=2", "ns:b=1", "ns:b=3", "other:c=1")), "sorted by key, then weight: " + keys);
		boolean same = s1.size() == s2.size();
		for (int i = 0; same && i < s1.size(); i++) same = s1.get(i).getFirst() == s2.get(i).getFirst() && s1.get(i).getSecond().equals(s2.get(i).getSecond());
		check(same, "two input orders give the same list");
		check(one.get(0).getFirst() == c, "the input list is left alone");

		// 2. processor rebind. A copied registry set (as a world loads it) re-decodes every pool, so its pieces hold its own
		// processor lists. A piece made in code with StructurePoolElement.single holds the builtin ProcessorLists.EMPTY,
		// which the copy's codec rejects: Cloud Storage's and Domestication Innovation's houses in a world.
		RegistryAccess access = RegistryAccess.builtinCopy();
		Registry<StructureProcessorList> lists = access.registryOrThrow(Registry.PROCESSOR_LIST_REGISTRY);
		Registry<StructureTemplatePool> pools = access.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
		check(invalid(pools, lists)[0] == 0, "a copied registry set's own pieces are all valid in it");
		StructureTemplatePool houses = pools.get(new net.minecraft.resources.ResourceLocation("minecraft:village/plains/houses"));
		Field templates = StructureTemplatePool.class.getDeclaredField("templates");
		templates.setAccessible(true);
		SinglePoolElement modHouse = (SinglePoolElement) single("cloudstorage:test_house");
		((List<StructurePoolElement>) templates.get(houses)).add(modHouse);
		var ops = net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, access);
		check(StructurePoolElement.CODEC.encodeStart(ops, modHouse).error().isPresent(),
				"before: saving the piece fails (\"is not valid in current registry set\", written as {})");
		int[] before = invalid(pools, lists);
		check(before[0] == 1, "a piece added in code: its builtin processor list is foreign to the copy (" + before[0] + " of " + before[1] + ")");
		PoolTuner.rebindProcessors(access);
		int[] after = invalid(pools, lists);
		check(after[0] == 0 && after[1] == before[1], "after the rebind every piece's processor list is the registry set's own (" + after[0] + " foreign)");
		check(processors(modHouse) == lists.getHolderOrThrow(net.minecraft.data.worldgen.ProcessorLists.EMPTY.unwrapKey().orElseThrow()),
				"the added piece now holds the copy's minecraft:empty");
		check(StructurePoolElement.CODEC.encodeStart(ops, modHouse).result().isPresent(), "after: the piece saves");
		PoolTuner.rebindProcessors(access);
		check(invalid(pools, lists)[0] == 0, "a second run changes nothing");

		System.out.println(fails == 0 ? "ALL " + checks + " CHECKS PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}

	/** {foreign, total} single pieces across every pool, by their processor holder. */
	@SuppressWarnings("unchecked")
	static int[] invalid(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> lists) throws Exception {
		int bad = 0, total = 0;
		for (StructureTemplatePool pool : pools) {
			for (StructurePoolElement el : pool.getShuffledTemplates(net.minecraft.util.RandomSource.create(0))) {
				if (!(el instanceof SinglePoolElement single)) continue;
				total++;
				Holder<StructureProcessorList> h = (Holder<StructureProcessorList>) processors(single);
				if (h.kind() == Holder.Kind.REFERENCE && !h.isValidInRegistry(lists)) bad++;
			}
		}
		return new int[]{bad, total};
	}
}
