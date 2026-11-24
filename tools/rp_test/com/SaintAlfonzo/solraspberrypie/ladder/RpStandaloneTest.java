// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.ladder;

import com.SaintAlfonzo.solraspberrypie.client.LangOverrides;
import com.SaintAlfonzo.solraspberrypie.logic.Diversity;
import com.SaintAlfonzo.solraspberrypie.logic.FoodWindow;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
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
 * the built-in default ladder parses on vanilla registries with no warning (every 3.3 check, V12 included), and has the ruled shape
 * (owner ruling, plan_rp_standalone.md P2: Apple Pie's ladder with its flat effects, scaled to 50; 13 unlocks with falling density; four
 * slots at 5, 15, 27.5, 40); P3: the diversity tables merge (a later file wins), ids of absent mods are skipped, no table leaves
 * Apple Pie's values, and the pack's Creative Delight table loads; P4: the lang name overrides; P5: ageing out on Apple
 * Pie's default settings; P7: the toggle and tagline fields (parse, sync round trip, an off toggle is granted but not applied)
 * and the generated pack ladder (defaultconfigs/solraspberrypie/ladder.json) on vanilla registries. Prints "ok ..." / "FAIL ..." lines and "ALL n OK" at the end.
 */
public class RpStandaloneTest {
	private static int ok, fail;

	/** {@code n} values, strictly increasing, whole numbers, the k-th (from 1) within 1 of {@code base + k * step}. */
	private static boolean near(List<Double> vs, double base, double step, int n) {
		if (vs.size() != n) return false;
		for (int k = 0; k < n; k++) {
			double v = vs.get(k);
			if (v != Math.rint(v) || Math.abs(v - (base + (k + 1) * step)) > 1.0 || (k > 0 && v <= vs.get(k - 1))) return false;
		}
		return true;
	}

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
		// Scaled to a top of 50 (plan_rp_standalone.md P2): with vanilla foods and Apple Pie's defaults (queueSize 32, endDecay 32)
		// the reachable bound is about 56, so not even V12 may warn.
		for (String w : warnings) System.out.println("     warning: " + w);
		check(warnings.isEmpty(), "default ladder parses with no warning, V12 included (" + warnings.size() + ")");
		check(ladder.rungs.size() == json.getAsJsonArray("rungs").size(), "no rung skipped (" + ladder.rungs.size() + ")");

		List<Double> unlocks = new ArrayList<>(), slots = new ArrayList<>(), health = new ArrayList<>(), flats = new ArrayList<>();
		for (Rung r : ladder.rungs) {
			if (r.kind == Rung.Kind.UNLOCK) unlocks.add(r.threshold);
			if (r.kind == Rung.Kind.SLOT) slots.add(r.threshold);
			if (r.kind == Rung.Kind.HEALTH) health.add(r.threshold);
			if (r.kind == Rung.Kind.FLAT) flats.add(r.threshold);
		}
		check(unlocks.equals(List.of(5.0, 8.8, 12.8, 17.1, 21.5, 26.3, 31.4, 36.9, 43.0, 50.0)),
				"10 unlocks, falling density 2.0 over 5 to 50 " + unlocks);
		check(slots.equals(List.of(5.0, 15.0, 27.5, 40.0)), "slots at 5, 15, 27.5, 40 " + slots);
		check(health.equals(List.of(2.5, 3.5, 5.0, 7.5, 10.0, 12.5, 15.0, 17.5, 20.0, 22.5, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0)),
				"health at Apple Pie's thresholds halved " + health);
		check(flats.equals(List.of(1.0, 3.5, 5.0, 10.0, 12.5, 17.5, 30.0, 35.0, 45.0, 50.0)), "Apple Pie's flat effects halved " + flats);
		check(ladder.rungs.size() == 44 && ladder.slotCount() == 4 && ladder.pool.size() == 8,
				"Diet ladder: 44 rungs, 4 slots, 8 pool effects (" + ladder.rungs.size() + ", " + ladder.slotCount() + ", " + ladder.pool.size() + ")");
		check(ladder.pool.keySet().stream().allMatch(id -> id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)), "pool is vanilla only");
		check(ladder.maxThreshold() == 50.0, "top rung at 50");

		// The checks still fire: a ladder naming an unknown effect warns once.
		JsonObject bad = JsonParser.parseString("{\"pool\":[{\"effect\":\"minecraft:speed\"}],\"rungs\":[{\"threshold\":1,\"type\":\"unlock\",\"effect\":\"nomod:nothing\"}]}").getAsJsonObject();
		List<String> badWarnings = new ArrayList<>();
		LadderLoader.parse(bad, "test", badWarnings);
		check(badWarnings.size() == 2 && badWarnings.get(0).startsWith("skipped 1 "), "a bad ladder still warns (skipped + V4): " + badWarnings);

		// PR-RP-11 / V14: an unlock whose flat rung comes no later than the first slot warns.
		JsonObject hidden = JsonParser.parseString("{\"pool\":[{\"effect\":\"minecraft:speed\"}],\"rungs\":[{\"threshold\":1,\"type\":\"flat\",\"effect\":\"minecraft:speed\"},"
				+ "{\"threshold\":5,\"type\":\"slot\"},{\"threshold\":5,\"type\":\"unlock\",\"effect\":\"minecraft:speed\"}]}").getAsJsonObject();
		List<String> hw = new ArrayList<>();
		LadderLoader.parse(hidden, "test", hw);
		check(hw.size() == 1 && hw.get(0).startsWith("V14:"), "an unlock hidden by an early flat rung warns (V14): " + hw);

		// food-tuning #49, #50: replacements and covered slots.
		JsonObject rj = JsonParser.parseString("{\"pool\":[{\"effect\":\"minecraft:jump_boost\"},{\"effect\":\"minecraft:slow_falling\",\"replaces\":[\"minecraft:jump_boost\",\"nomod:gone\"]},"
				+ "{\"effect\":\"minecraft:luck\"}],\"rungs\":[{\"threshold\":1,\"type\":\"slot\"},{\"threshold\":1,\"type\":\"unlock\",\"effect\":\"minecraft:jump_boost\"},"
				+ "{\"threshold\":1,\"type\":\"unlock\",\"effect\":\"minecraft:luck\"},{\"threshold\":3,\"type\":\"flat\",\"effect\":\"minecraft:luck\"},"
				+ "{\"threshold\":5,\"type\":\"unlock\",\"effect\":\"minecraft:slow_falling\"}]}").getAsJsonObject();
		List<String> rw = new ArrayList<>();
		Ladder rl = LadderLoader.parse(rj, "replace test", rw);
		ResourceLocation jb = new ResourceLocation("minecraft:jump_boost"), sf = new ResourceLocation("minecraft:slow_falling"), luck = new ResourceLocation("minecraft:luck");
		check(rw.isEmpty() && rl.replaces.equals(Map.of(sf, List.of(jb))), "replaces parses; an absent mod's id is dropped quietly " + rw + " " + rl.replaces);
		check(rl.replacedBy(jb, 4) == null && sf.equals(rl.replacedBy(jb, 5)), "replaced only once the replacing effect is unlocked");
		FriendlyByteBuf rbuf = new FriendlyByteBuf(Unpooled.buffer());
		rl.write(rbuf);
		check(Ladder.read(rbuf).replaces.equals(rl.replaces), "replacements survive the login sync");
		List<com.SaintAlfonzo.solraspberrypie.data.SlotChoice> jbPick = List.of(new com.SaintAlfonzo.solraspberrypie.data.SlotChoice(jb, 1));
		check(DietEngine.compute(rl, 2, 99, jbPick, id -> true, 4).effects.containsKey(jb)
				&& !DietEngine.compute(rl, 2, 99, jbPick, id -> true, 5).effects.containsKey(jb), "a replaced effect in a slot is not applied");
		DietEngine.State luckFlat = DietEngine.compute(rl, 3, 99, List.of(new com.SaintAlfonzo.solraspberrypie.data.SlotChoice(luck, 1)), id -> true, 3);
		check(rl.covered(luck, luckFlat.flat.keySet(), 3) && rl.covered(jb, java.util.Set.of(), 5) && !rl.covered(jb, java.util.Set.of(), 4),
				"covered: a flat buff of the effect, or an unlocked replacement");

		// P7: toggle and tagline fields (raspberry_pie.md 3.3).
		check(ladder.toggles.isEmpty(), "the default ladder has no toggle effect");
		JsonObject tj = JsonParser.parseString("{\"pool\":[{\"effect\":\"minecraft:night_vision\",\"toggle\":\"on\"},"
				+ "{\"effect\":\"minecraft:speed\",\"max_level\":2}],\"rungs\":["
				+ "{\"threshold\":1,\"type\":\"slot\"},"
				+ "{\"threshold\":1,\"type\":\"unlock\",\"effect\":\"minecraft:night_vision\",\"tagline\":\"t.nv\"},"
				+ "{\"threshold\":1,\"type\":\"unlock\",\"effect\":\"minecraft:speed\",\"level\":1,\"tagline\":\"t.speed1\"},"
				+ "{\"threshold\":2,\"type\":\"unlock\",\"effect\":\"minecraft:speed\",\"level\":2},"
				+ "{\"threshold\":3,\"type\":\"flat\",\"effect\":\"minecraft:invisibility\",\"toggle\":\"off\"},"
				+ "{\"threshold\":3,\"type\":\"flat\",\"effect\":\"minecraft:luck\",\"tagline\":\"t.luck\"}]}").getAsJsonObject();
		List<String> tw = new ArrayList<>();
		Ladder tl = LadderLoader.parse(tj, "toggle test", tw);
		check(tw.isEmpty(), "toggle and tagline fields parse with no warning " + tw);
		check(tl.isToggle(new ResourceLocation("minecraft:night_vision")) && tl.toggleDefault(new ResourceLocation("minecraft:night_vision")),
				"pool toggle 'on': a toggle, on by default");
		check(tl.isToggle(new ResourceLocation("minecraft:invisibility")) && !tl.toggleDefault(new ResourceLocation("minecraft:invisibility")),
				"flat toggle 'off': a toggle, off by default");
		check(!tl.isToggle(new ResourceLocation("minecraft:speed")), "no toggle field: no toggle");
		check("t.speed1".equals(tl.tagline(new ResourceLocation("minecraft:speed"), 2)), "Speed II without its own tagline uses Speed I's");
		check("t.luck".equals(tl.tagline(new ResourceLocation("minecraft:luck"), 1)), "a flat rung's tagline");
		check(tl.tagline(new ResourceLocation("minecraft:invisibility"), 1) == null, "no tagline field: none");
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		tl.write(buf);
		Ladder synced = Ladder.read(buf);
		check(synced.toggles.equals(tl.toggles) && "t.nv".equals(synced.tagline(new ResourceLocation("minecraft:night_vision"), 1))
				&& synced.rungs.size() == tl.rungs.size(), "toggles and taglines survive the login sync");
		List<com.SaintAlfonzo.solraspberrypie.data.SlotChoice> picks = List.of(new com.SaintAlfonzo.solraspberrypie.data.SlotChoice(new ResourceLocation("minecraft:night_vision"), 1));
		DietEngine.State on = DietEngine.compute(tl, 5, 99, picks, id -> !id.getPath().equals("invisibility"));
		DietEngine.State off = DietEngine.compute(tl, 5, 99, picks, id -> false);
		ResourceLocation nv = new ResourceLocation("minecraft:night_vision"), inv = new ResourceLocation("minecraft:invisibility");
		check(on.effects.containsKey(nv) && !on.effects.containsKey(inv) && on.flat.containsKey(inv) && on.off.equals(java.util.Set.of(inv)),
				"an off toggle is granted (flat) but not applied; an on one is applied");
		check(!off.effects.containsKey(nv) && off.slotEffects.containsKey(nv) && off.effects.containsKey(new ResourceLocation("minecraft:luck")),
				"a slotted toggle switched off stays in its slot, unapplied; a non-toggle is applied whatever the switch says");

		// P7: the generated pack ladder parses on vanilla registries; modded entries are skipped (one warning), the rest is the ruled shape.
		JsonObject pack = JsonParser.parseString(Files.readString(root.resolve("defaultconfigs/solraspberrypie/ladder.json"), StandardCharsets.UTF_8)).getAsJsonObject();
		List<String> pw = new ArrayList<>();
		Ladder pl = LadderLoader.parse(pack, "pack ladder", pw);
		List<String> other = pw.stream().skip(1).filter(w -> !w.startsWith("V12:")).toList();
		check(!pw.isEmpty() && pw.get(0).startsWith("skipped ") && other.isEmpty(), "pack ladder on vanilla: only the skip line ("
				+ (pw.isEmpty() ? "none" : pw.get(0).substring(0, pw.get(0).indexOf(':'))) + ") and V12; other warnings " + other);
		List<Double> hearts = new ArrayList<>(), packSlots = new ArrayList<>(), tough = new ArrayList<>();
		for (Rung r : pl.rungs) {
			if (r.kind == Rung.Kind.HEALTH) hearts.add(r.threshold);
			if (r.kind == Rung.Kind.SLOT) packSlots.add(r.threshold);
			if (r.kind == Rung.Kind.ATTRIBUTE && r.id.getPath().equals("generic.armor_toughness")) tough.add(r.threshold);
		}
		// Whole-number thresholds, each within 1 of its spaced value (a merged pair moves one value to the nearest free step).
		check(near(hearts, 0, 100 / 11.0, 10), "pack: 10 hearts every 100/11, whole numbers " + hearts);
		check(near(tough, 100 / 6.0 - 100 / 3.0, 100 / 3.0, 3), "pack: toughness pairs at 1/6, 1/2, 5/6, whole numbers " + tough);
		check(near(packSlots, 2.5 - 19.5, 19.5, 5), "pack: 5 slots evenly spaced from 2.5, whole numbers " + packSlots);
		java.util.Set<Double> distinct = new java.util.TreeSet<>();
		boolean whole = true;
		for (Rung r : pl.rungs) {
			distinct.add(r.threshold);
			whole &= r.threshold == Math.rint(r.threshold);
		}
		check(whole && distinct.size() >= 40, "pack: every threshold a whole number, " + distinct.size() + " distinct values");
		check(!pl.toggleDefault(inv) && pl.isToggle(inv) && pl.toggleDefault(nv) && pl.isToggle(nv), "pack: Invisibility toggle off, Night Vision on");
		check("solraspberrypie.tagline.minecraft.speed.2".equals(pl.tagline(new ResourceLocation("minecraft:speed"), 2)), "pack: Speed II tagline key");
		JsonObject packLang = JsonParser.parseString(Files.readString(root.resolve("kubejs/assets/solraspberrypie/lang/en_us.json"), StandardCharsets.UTF_8)).getAsJsonObject();
		boolean allKeyed = true;
		for (var el : pack.getAsJsonArray("rungs")) {
			JsonObject o = el.getAsJsonObject();
			if (o.has("tagline") && !packLang.has(o.get("tagline").getAsString())) allKeyed = false;
		}
		check(allKeyed, "pack: every tagline key has its text in the pack lang file");

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
		// PR-RP-04: a table of the wrong shape is skipped whole, bad values one by one; the good table still applies.
		JsonObject wrong = JsonParser.parseString("{\"values\":[1,2],\"families\":{}}").getAsJsonObject();
		JsonObject badVals = JsonParser.parseString("{\"values\":{\"minecraft:bread\":-1,\"minecraft:carrot\":\"NaN\",\"minecraft:beef\":0.5}}").getAsJsonObject();
		Diversity.use(List.of(new Diversity.Source("a:a.json", a), new Diversity.Source("w:wrong.json", wrong), new Diversity.Source("v:vals.json", badVals)),
				ForgeRegistries.ITEMS::containsKey);
		check(Double.valueOf(1.0).equals(Diversity.value(Items.APPLE)) && Double.valueOf(0.5).equals(Diversity.value(Items.BEEF))
				&& Double.valueOf(2.0).equals(Diversity.value(Items.CARROT)) && Diversity.value(Items.BREAD) == null,
				"a wrong-shape table is skipped, negative and NaN values are skipped, the rest applies");
		Diversity.use(List.of(), ForgeRegistries.ITEMS::containsKey);
		check(Diversity.value(Items.APPLE) == null && Diversity.head(Items.BEEF) == Items.BEEF, "no table: nothing overridden");
		Path cd = root.resolve("development/creativeDelight/src/main/resources/solraspberrypie/diversity/creativedelight.json");
		JsonObject cdJson = JsonParser.parseString(Files.readString(cd, StandardCharsets.UTF_8)).getAsJsonObject();
		Diversity.use(List.of(new Diversity.Source("creativedelight:creativedelight.json", cdJson)), ForgeRegistries.ITEMS::containsKey);
		double bread = cdJson.getAsJsonObject("values").get("minecraft:bread").getAsDouble();
		check(Double.valueOf(bread).equals(Diversity.value(Items.BREAD)), "the pack table (Creative Delight) loads on vanilla ids only, modded ids skipped (bread " + bread + ")");

		// PR-RP-17: the server's table round-trips through S2CDiversity and becomes the client's.
		com.SaintAlfonzo.solraspberrypie.net.S2CDiversity dmsg = com.SaintAlfonzo.solraspberrypie.net.S2CDiversity.current();
		FriendlyByteBuf dbuf = new FriendlyByteBuf(Unpooled.buffer());
		com.SaintAlfonzo.solraspberrypie.net.S2CDiversity.encode(dmsg, dbuf);
		int dBytes = dbuf.readableBytes();
		com.SaintAlfonzo.solraspberrypie.net.S2CDiversity back = com.SaintAlfonzo.solraspberrypie.net.S2CDiversity.decode(dbuf);
		Diversity.use(List.of(), ForgeRegistries.ITEMS::containsKey);
		com.SaintAlfonzo.solraspberrypie.net.S2CDiversity.applyForTest(back);
		check(Double.valueOf(bread).equals(Diversity.value(Items.BREAD)) && dBytes < 1_000_000,
				"the table round-trips through S2CDiversity and applies on the client (" + dBytes + " bytes)");

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
		// contributed: aged-out foods leave the memory (raspberry-pie-publish #26).
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
		FoodWindow.ageOut(memory, oldest);
		check(Math.abs(FoodWindow.diversity(memory) - appleOnly) < 1e-9 && before > appleOnly,
				"respec: the two aged-out foods count nothing (" + String.format(java.util.Locale.ROOT, "%.3f -> %.3f", before, FoodWindow.diversity(memory)) + ")");
		check(!memory.containsKey(carrot) && !memory.containsKey(breadF) && memory.get(apple) == 0, "aged-out foods leave Apple Pie's memory " + memory.keySet().size());
		FoodWindow.ageOut(memory, FoodWindow.windowOldestFirst(memory));
		check(FoodWindow.diversity(memory) == 0.0 && memory.isEmpty(), "death: the whole window ages out, memory empty, diversity 0");

		// raspberry-pie-publish #24: a drop loses only the highest active slot. The oldest food (stew) alone would take two
		// slots, so it is kept and the next oldest (apple) goes; thresholds are set from the foods' contributions.
		FoodInstance stew = new FoodInstance(Items.RABBIT_STEW), fresh = new FoodInstance(Items.APPLE), newest = new FoodInstance(Items.BREAD);
		Map<FoodInstance, Integer> dm = new HashMap<>(Map.of(stew, 20, fresh, 10, newest, 0));
		double t = FoodWindow.diversity(dm);
		double cStew = FoodList.calculateDiversityContribution(stew, 20), cApple = FoodList.calculateDiversityContribution(fresh, 10);
		check(cStew > cApple, String.format(java.util.Locale.ROOT, "drop fixture: the stew outweighs the apple (%.3f > %.3f)", cStew, cApple));
		String th = String.format(java.util.Locale.ROOT, "{\"pool\":[{\"effect\":\"minecraft:speed\"}],\"rungs\":[{\"threshold\":0.01,\"type\":\"slot\"},"
				+ "{\"threshold\":%.6f,\"type\":\"slot\"},{\"threshold\":%.6f,\"type\":\"slot\"}]}", t - (cStew + cApple) / 2, t - cApple / 2);
		Ladder dl = LadderLoader.parse(JsonParser.parseString(th).getAsJsonObject(), "drop test", new ArrayList<>());
		check(dl.activeSlots(t) == 3, "drop fixture: three slots active");
		com.SaintAlfonzo.solraspberrypie.logic.SlotActions.DropPlan plan = com.SaintAlfonzo.solraspberrypie.logic.SlotActions.planDrop(
				dl, dm, FoodWindow.windowOldestFirst(dm), 3);
		check(plan.group.equals(List.of(fresh)) && plan.lowered == null && plan.memory.containsKey(stew)
						&& dl.activeSlots(FoodWindow.diversity(plan.memory)) == 2,
				"drop: the apple ages out, the stew stays, two slots stay active (" + plan.group.size() + " aged)");
		Map<FoodInstance, Integer> one = new HashMap<>(Map.of(stew, 20));
		com.SaintAlfonzo.solraspberrypie.logic.SlotActions.DropPlan only = com.SaintAlfonzo.solraspberrypie.logic.SlotActions.planDrop(
				dl, one, FoodWindow.windowOldestFirst(one), dl.activeSlots(FoodWindow.diversity(one)));
		check(only.group.equals(List.of(stew)), "drop: with no other way the oldest food still ages out");

		// PR-RP-13: a state sync carries the drink list only when it changed; the client keeps its last list otherwise.
		com.SaintAlfonzo.solraspberrypie.net.S2CState st = new com.SaintAlfonzo.solraspberrypie.net.S2CState();
		st.hasDrinks = true;
		st.drinks.add(new ResourceLocation("minecraft", "potion"));
		FriendlyByteBuf sbuf = new FriendlyByteBuf(Unpooled.buffer());
		com.SaintAlfonzo.solraspberrypie.net.S2CState.encode(st, sbuf);
		com.SaintAlfonzo.solraspberrypie.net.S2CState sb = com.SaintAlfonzo.solraspberrypie.net.S2CState.decode(sbuf);
		st.hasDrinks = false;
		st.drinks.clear();
		com.SaintAlfonzo.solraspberrypie.net.S2CState.encode(st, sbuf);
		com.SaintAlfonzo.solraspberrypie.net.S2CState sn = com.SaintAlfonzo.solraspberrypie.net.S2CState.decode(sbuf);
		check(sb.hasDrinks && sb.drinks.size() == 1 && !sn.hasDrinks && sn.drinks.isEmpty() && sbuf.readableBytes() == 0,
				"a state sync carries the drink list only when it is flagged");

		// PR-RP-12: the Diet Log ingredient takes any edible item and nothing inedible.
		var edible = new com.SaintAlfonzo.solraspberrypie.recipe.EdibleIngredient(null);
		check(edible.test(new net.minecraft.world.item.ItemStack(Items.APPLE)) && edible.test(new net.minecraft.world.item.ItemStack(Items.ROTTEN_FLESH))
				&& !edible.test(new net.minecraft.world.item.ItemStack(Items.BOOK)) && !edible.test(net.minecraft.world.item.ItemStack.EMPTY),
				"the edible ingredient takes apple and rotten flesh, not a book or nothing");
		check(edible.getItems().length > 30, "the edible ingredient lists every vanilla food (" + edible.getItems().length + ")");

		System.out.println(fail == 0 ? "ALL " + ok + " OK" : fail + " FAILED");
	}
}
