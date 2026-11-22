// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.ladder;

import com.SaintAlfonzo.solraspberrypie.client.LangOverrides;
import com.SaintAlfonzo.solraspberrypie.logic.Diversity;
import com.SaintAlfonzo.solraspberrypie.logic.FoodWindow;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Headless checks for Raspberry Pie standing alone (feature_writeup/plan_rp_standalone.md P2, tools/test_rp_ladder.sh):
 * the built-in default ladder parses on vanilla registries with no file warning (3.3 V1 to V10; V12 is reported), and has the ruled shape
 * (D2, D3, D10: ten unlocks at 10, 18, 26, 34, 43, 53, 63, 74, 86, 100; four slots at 10, 30, 55, 80; Apple Pie's
 * thresholds); P3: the diversity tables merge (a later file wins), ids of absent mods are skipped, no table leaves
 * Apple Pie's values, and the pack's Creative Delight table loads; P4: the lang name overrides; P5: ageing out on Apple
 * Pie's default settings. Prints "ok ..." / "FAIL ..." lines and "ALL n OK" at the end.
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

		// P3: table merge and optional ids.
		JsonObject a = JsonParser.parseString("{\"values\":{\"minecraft:apple\":1.0,\"minecraft:carrot\":2.0,\"nomod:x\":3.0},"
				+ "\"families\":{\"beef\":[\"minecraft:cooked_beef\",\"minecraft:beef\",\"nomod:y\"],\"gone\":[\"nomod:z\",\"minecraft:bread\"]}}").getAsJsonObject();
		JsonObject b = JsonParser.parseString("{\"values\":{\"minecraft:apple\":5.0}}").getAsJsonObject();
		Diversity.use(List.of(new Diversity.Source("a:a.json", a), new Diversity.Source("b:b.json", b)), ForgeRegistries.ITEMS::containsKey);
		check(Double.valueOf(5.0).equals(Diversity.value(Items.APPLE)), "a later table wins an id (apple " + Diversity.value(Items.APPLE) + ")");
		check(Double.valueOf(2.0).equals(Diversity.value(Items.CARROT)), "an id only the first table lists stays (carrot)");
		check(Diversity.value(Items.BREAD) == null, "an unlisted food keeps Apple Pie's value (bread)");
		check(Diversity.head(Items.BEEF) == Items.COOKED_BEEF, "a family member counts as its head (beef -> cooked beef)");
		check(Diversity.head(Items.BREAD) == Items.BREAD, "a family with an absent head is skipped (bread is its own entry)");
		Diversity.use(List.of(), ForgeRegistries.ITEMS::containsKey);
		check(Diversity.value(Items.APPLE) == null && Diversity.head(Items.BEEF) == Items.BEEF, "no table: nothing overridden");
		Path cd = root.resolve("development/creativeDelight/src/main/resources/solraspberrypie/diversity/creativedelight.json");
		JsonObject cdJson = JsonParser.parseString(Files.readString(cd, StandardCharsets.UTF_8)).getAsJsonObject();
		Diversity.use(List.of(new Diversity.Source("creativedelight:creativedelight.json", cdJson)), ForgeRegistries.ITEMS::containsKey);
		double bread = cdJson.getAsJsonObject("values").get("minecraft:bread").getAsDouble();
		check(Double.valueOf(bread).equals(Diversity.value(Items.BREAD)), "the pack table (Creative Delight) loads on vanilla ids only, modded ids skipped (bread " + bread + ")");

		// P4: the name overrides in the lang file win over Apple Pie's and Carrot's own keys in any merge order.
		JsonObject lang = JsonParser.parseString(Files.readString(root.resolve(
				"development/solRaspberryPie/src/main/resources/assets/solraspberrypie/lang/en_us.json"), StandardCharsets.UTF_8)).getAsJsonObject();
		Map<String, String> merged = new HashMap<>();
		lang.entrySet().forEach(e -> merged.put(e.getKey(), e.getValue().getAsString()));
		merged.put("item.solapplepie.food_book", "Food Book");   // Apple Pie's file merged after ours
		merged.put("item.solcarrot.food_book", "Food Book");
		merged.put("key.solapplepie.open_food_book", "Open Food Book");
		LangOverrides.apply(merged);
		check("Diet Log".equals(merged.get("item.solapplepie.food_book")), "Apple Pie's book is the Diet Log");
		check("Food Dictionary".equals(merged.get("item.solcarrot.food_book")), "Carrot's book is the Food Dictionary");
		check("Open Diet Log".equals(merged.get("key.solapplepie.open_food_book")), "the open-book key says Open Diet Log");

		// P5: ageing out on Apple Pie's own defaults (queueSize 32 = endDecay 32, minContribution 0; the headless config
		// reads its defaults). A respec of the two oldest window foods and a death (the whole window) cost what they
		// contributed: the moved foods count nothing.
		check(SOLSweetPotatoConfig.size() == 32 && SOLSweetPotatoConfig.endDecay() == 32 && SOLSweetPotatoConfig.minContribution() == 0.0,
				"Apple Pie defaults read: queueSize " + SOLSweetPotatoConfig.size() + ", endDecay " + SOLSweetPotatoConfig.endDecay());
		Map<FoodInstance, Integer> memory = new HashMap<>();
		FoodInstance apple = new FoodInstance(Items.APPLE), breadF = new FoodInstance(Items.BREAD), carrot = new FoodInstance(Items.CARROT);
		memory.put(apple, 0);
		memory.put(breadF, 5);
		memory.put(carrot, 20);
		double before = FoodWindow.diversity(memory);
		double appleOnly = FoodWindow.diversity(Map.of(apple, 0));
		List<FoodInstance> oldest = FoodWindow.windowOldestFirst(memory).subList(0, 2);
		check(oldest.equals(List.of(carrot, breadF)), "oldest window foods first (carrot, bread)");
		FoodWindow.moveOut(memory, oldest);
		check(Math.abs(FoodWindow.diversity(memory) - appleOnly) < 1e-9 && before > appleOnly,
				"respec: the two moved foods count nothing (" + String.format(java.util.Locale.ROOT, "%.3f -> %.3f", before, FoodWindow.diversity(memory)) + ")");
		check(memory.get(carrot) >= 32 && memory.get(breadF) >= 32 && memory.get(apple) == 0, "moved foods sit at endDecay or past it " + memory.values());
		FoodWindow.moveOut(memory, FoodWindow.windowOldestFirst(memory));
		check(FoodWindow.diversity(memory) == 0.0 && FoodWindow.windowOldestFirst(memory).isEmpty(), "death: the whole window moves out, diversity 0");

		System.out.println(fail == 0 ? "ALL " + ok + " OK" : fail + " FAILED");
	}
}
