package com.SaintAlfonzo.creativeengineering.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.List;
import java.util.function.Predicate;

/**
 * Biome tests of the locate prediction that must hold for every biome generation could see at a point.
 *
 * MultiNoiseBiomeSource#getNoiseBiome returns the nearest entry of its parameter list, but Climate.RTree#search starts
 * from the calling thread's previous result and keeps it on a tie. Where entries overlap at the same offset
 * (Terralith's cave/deep_caves and vanilla's deep_dark at depth 1.05-1.2 and erosion below -0.375) the biome depends on
 * what that thread looked up before, so a /locate or census thread can get deep_dark where the worldgen thread got
 * deep_caves (TODO structures/testing #4). A "no" is only certain when no tied entry passes.
 */
final class TiedBiomes {

	private TiedBiomes() {}

	/** Some biome a lookup at this quart could return passes {@code test}. The full list is only walked on a fail. */
	static boolean any(BiomeSource source, Climate.Sampler sampler, int qx, int qy, int qz, Predicate<Holder<Biome>> test) {
		if (test.test(source.getNoiseBiome(qx, qy, qz, sampler))) return true;
		if (!(source instanceof MultiNoiseBiomeSource multi)) return false;
		Climate.TargetPoint t = sampler.sample(qx, qy, qz);
		List<Pair<Climate.ParameterPoint, Holder<Biome>>> entries = LocateReflect.parameters(multi).values();
		long best = Long.MAX_VALUE;
		for (Pair<Climate.ParameterPoint, Holder<Biome>> e : entries) best = Math.min(best, fitness(e.getFirst(), t));
		for (Pair<Climate.ParameterPoint, Holder<Biome>> e : entries) {
			if (fitness(e.getFirst(), t) == best && test.test(e.getSecond())) return true;
		}
		return false;
	}

	/** Climate.ParameterPoint#fitness (package-private): the distance Climate.RTree compares. */
	private static long fitness(Climate.ParameterPoint p, Climate.TargetPoint t) {
		return Mth.square(p.temperature().distance(t.temperature())) + Mth.square(p.humidity().distance(t.humidity()))
				+ Mth.square(p.continentalness().distance(t.continentalness())) + Mth.square(p.erosion().distance(t.erosion()))
				+ Mth.square(p.depth().distance(t.depth())) + Mth.square(p.weirdness().distance(t.weirdness())) + Mth.square(p.offset());
	}
}
