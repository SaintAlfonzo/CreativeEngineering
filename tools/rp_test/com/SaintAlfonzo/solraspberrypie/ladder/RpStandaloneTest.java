// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.ladder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless checks for Raspberry Pie standing alone (feature_writeup/plan_rp_standalone.md P2, tools/test_rp_ladder.sh):
 * the built-in default ladder parses on vanilla registries with no file warning (3.3 V1 to V10; V12 is reported), and has the ruled shape
 * (D2, D3, D10: ten unlocks at 10, 18, 26, 34, 43, 53, 63, 74, 86, 100; four slots at 10, 30, 55, 80; Apple Pie's
 * thresholds). Prints "ok ..." / "FAIL ..." lines and "ALL n OK" at the end.
 */
public class RpStandaloneTest {
	private static int ok, fail;

	private static void check(boolean cond, String what) {
		if (cond) {
			ok++;
			System.out.println("ok   " + what);
		} else {
			fail++;
			System.out.println("FAIL " + what);
		}
	}

	public static void main(String[] args) throws Exception {
		Path root = Path.of(args[0]);
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		Path file = root.resolve("development/solRaspberryPie/src/main/resources/solraspberrypie/default_ladder.json");
		JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
		List<String> warnings = new ArrayList<>();
		Ladder ladder = LadderLoader.parse(json, "built-in default", warnings);
		// V12 (a rung above the reachable bound) depends on the registered foods and Apple Pie's settings, not on the
		// file: with vanilla foods only and Apple Pie's defaults (queueSize 32, endDecay 32) the bound is about 56, so
		// the upper rungs on Apple Pie's own 2-to-100 scale warn there (Apple Pie's own default ladder does too). It is
		// reported, not failed; every file check (V1 to V10) must stay silent.
		List<String> fileWarnings = warnings.stream().filter(w -> !w.startsWith("V12:")).toList();
		for (String w : fileWarnings) System.out.println("     warning: " + w);
		long v12 = warnings.size() - fileWarnings.size();
		if (v12 > 0) System.out.println("note " + v12 + " V12 lines on vanilla foods: " + warnings.stream().filter(w -> w.startsWith("V12:")).findFirst().orElse(""));
		check(fileWarnings.isEmpty(), "default ladder parses with no file warning (" + fileWarnings.size() + ")");
		check(ladder.rungs.size() == json.getAsJsonArray("rungs").size(), "no rung skipped (" + ladder.rungs.size() + ")");

		List<Double> unlocks = new ArrayList<>(), slots = new ArrayList<>(), health = new ArrayList<>();
		for (Rung r : ladder.rungs) {
			if (r.kind == Rung.Kind.UNLOCK) unlocks.add(r.threshold);
			if (r.kind == Rung.Kind.SLOT) slots.add(r.threshold);
			if (r.kind == Rung.Kind.HEALTH) health.add(r.threshold);
		}
		check(unlocks.equals(List.of(10.0, 18.0, 26.0, 34.0, 43.0, 53.0, 63.0, 74.0, 86.0, 100.0)), "unlocks at the D10 positions " + unlocks);
		check(slots.equals(List.of(10.0, 30.0, 55.0, 80.0)), "slots at 10, 30, 55, 80 " + slots);
		check(health.equals(List.of(5.0, 7.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)),
				"health at Apple Pie's thresholds " + health);
		check(ladder.pool.size() == 10, "pool of 10 effects (" + ladder.pool.size() + ")");
		check(ladder.pool.keySet().stream().allMatch(id -> id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)), "pool is vanilla only");
		check(ladder.rungs.stream().noneMatch(r -> r.kind == Rung.Kind.FLAT), "no flat effects");

		// The checks still fire: a ladder naming an unknown effect warns once.
		JsonObject bad = JsonParser.parseString("{\"pool\":[{\"effect\":\"minecraft:speed\"}],\"rungs\":[{\"threshold\":1,\"type\":\"unlock\",\"effect\":\"nomod:nothing\"}]}").getAsJsonObject();
		List<String> badWarnings = new ArrayList<>();
		LadderLoader.parse(bad, "test", badWarnings);
		check(badWarnings.size() == 2 && badWarnings.get(0).startsWith("skipped 1 "), "a bad ladder still warns (skipped + V4): " + badWarnings);

		System.out.println(fail == 0 ? "ALL " + ok + " OK" : fail + " FAILED");
	}
}
