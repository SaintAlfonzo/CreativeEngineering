package com.SaintAlfonzo.creativedevtools.cdevplace;

import com.SaintAlfonzo.creativeworldgen.CWGConfig.GroundMode;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig.VerticalAlign;
import com.SaintAlfonzo.creativeworldgen.worldgen.WorldgenConfig;
import com.SaintAlfonzo.creativecommon.util.WorldgenFiles;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Headless checks of /cdevplace's pure parts (filters, rows, signs, alignment) and of WorldgenConfig's and DevtoolsConfig's
 * defaults. /cdevplace is Creative Devtools' since the Creative Split (feature_writeup/creative_devtools.md 2).
 */
public class MassPlacerTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	public static void main(String[] args) throws Exception {
		Path root = Path.of(args[0]);
		// WorldgenFiles.rootOverride is a package-private test seam in patcherMod's worldgen package; this test lives in
		// Devtools' cdevplace package since the Creative Split, so set it by reflection.
		java.lang.reflect.Field rootOverride = WorldgenFiles.class.getDeclaredField("rootOverride");
		rootOverride.setAccessible(true);
		rootOverride.set(null, root);

		// 1. sign text
		List<String> s = MassPlacer.signLines("towns_and_towers:exclusives/pillager_outpost_classic", 4);
		check(s.size() <= 4 && s.stream().allMatch(l -> l.length() <= MassPlacer.SIGN_WIDTH), "sign: at most 4 lines of 16 chars " + s);
		check(s.get(0).equals("towns_and_towers"), "sign: namespace first");
		check(String.join("", s.subList(1, s.size())).equals("exclusives/pillager_outpost_classic"), "sign: the whole path kept when it fits");
		List<String> longOne = MassPlacer.signLines("a:" + "very_long_structure_name_that_does_not_fit_on_any_sign_at_all", 4);
		check(longOne.size() == 4 && longOne.get(3).endsWith(".."), "sign: overflow marked with .. " + longOne);
		check(MassPlacer.signLines("minecraft:village_plains", 4).equals(List.of("minecraft", "village_plains")), "sign: short id on 2 lines");
		List<String> row = MassPlacer.rowSignLines("type/village", 24);
		check(row.get(0).equals("row") && row.get(row.size() - 1).equals("24 items"), "row sign: 'row', name, count " + row);

		// 2. filters
		List<String> all = List.of("a:one", "a:two", "a:three", "b:one", "b:tower_x");
		Map<String, Set<String>> tags = Map.of("type/tower", Set.of("b:tower_x", "a:two"));
		List<String> problems = new ArrayList<>();
		check(MassPlacer.select("@a", all, tags, t -> null, problems).equals(new TreeSet<>(List.of("a:one", "a:two", "a:three"))), "filter: @namespace");
		check(MassPlacer.select("@a !a:two", all, tags, t -> null, problems).equals(new TreeSet<>(List.of("a:one", "a:three"))), "filter: ! excludes");
		check(MassPlacer.select("*:one", all, tags, t -> null, problems).equals(new TreeSet<>(List.of("a:one", "b:one"))), "filter: glob");
		check(MassPlacer.select("#type/tower", all, tags, t -> null, problems).equals(new TreeSet<>(List.of("a:two", "b:tower_x"))), "filter: #pack tag");
		check(MassPlacer.select("#minecraft:village", all, tags, t -> t.equals("minecraft:village") ? List.of("b:one") : null, problems)
				.equals(Set.of("b:one")), "filter: #ns:tag goes to the registry");
		check(problems.isEmpty(), "filter: no problems so far " + problems);
		MassPlacer.select("a:nope #nope", all, tags, t -> null, problems);
		check(problems.size() == 3, "filter: unknown id and unknown tag reported " + problems);

		// 3. rows on the real pack: structure_tags.json over every structure in structure_config.json
		JsonObject cfg = JsonParser.parseString(Files.readString(root.resolve("kubejs/generation_scripts/worldgen/structure_config.json"))).getAsJsonObject();
		List<String> ids = new ArrayList<>();
		for (Map.Entry<String, JsonElement> set : cfg.getAsJsonObject("structure_sets").entrySet())
			for (JsonElement m : set.getValue().getAsJsonObject().getAsJsonArray("structures")) ids.add(m.getAsJsonObject().get("structure").getAsString());
		ids.sort(null);
		Map<String, Set<String>> pack = MassPlacer.packTags(ids);
		check(pack.containsKey("type/village") && pack.containsKey("group/settlements"), "pack tags read: " + pack.size() + " tags");
		Set<String> tt = MassPlacer.select("@towns_and_towers", ids, pack, t -> null, new ArrayList<>());
		List<MassPlacer.Row> rows = MassPlacer.rowsFor(tt, MassPlacer.RowBy.TYPE, pack);
		int placed = rows.stream().mapToInt(r -> r.ids().size()).sum();
		check(placed == tt.size(), "T&T by type: every structure in exactly one row (" + placed + "/" + tt.size() + ")");
		boolean smallest = true;
		for (MassPlacer.Row r : rows) {
			if (!r.name().startsWith("type/")) continue;
			for (ResourceLocation id : r.ids()) {
				int size = pack.get(r.name()).size();
				for (Map.Entry<String, Set<String>> e : pack.entrySet())
					if (e.getKey().startsWith("type/") && e.getValue().contains(id.toString()) && e.getValue().size() < size) smallest = false;
				if (!pack.get(r.name()).contains(id.toString())) smallest = false;
			}
		}
		check(smallest, "T&T by type: each structure is in its smallest type/* tag");
		check(rows.stream().noneMatch(r -> r.name().equals("type/enemy_building")), "T&T by type: no enemy_building union row (outposts split into camp/fort/tower)");
		StringBuilder sb = new StringBuilder();
		rows.forEach(r -> sb.append(r.name()).append('=').append(r.ids().size()).append(' '));
		System.out.println("     T&T rows: " + sb.toString().trim());
		List<MassPlacer.Row> byGroup = MassPlacer.rowsFor(new TreeSet<>(ids), MassPlacer.RowBy.GROUP, pack);
		check(byGroup.stream().mapToInt(r -> r.ids().size()).sum() == new TreeSet<>(ids).size(), "whole pack by group: nothing lost (" + byGroup.size() + " rows)");
		List<MassPlacer.Row> byMod = MassPlacer.rowsFor(tt, MassPlacer.RowBy.MOD, pack);
		check(byMod.size() == 1 && byMod.get(0).name().equals("towns_and_towers"), "by mod: one row per namespace");

		// 4. template rows = folders
		List<MassPlacer.Row> trows = MassPlacer.templateRows(new TreeSet<>(List.of(
				"t:village/plains/houses/a", "t:village/plains/houses/b", "t:village/plains/streets/s", "t:top")));
		check(trows.size() == 3 && trows.get(0).name().equals("t:") && trows.get(1).ids().size() == 2, "templates: one row per folder");

		// 5. vertical alignment (goal: box bottom at ground - 1)
		check(MassPlacer.verticalShift(VerticalAlign.KEEP, -40, -60, 8) == 0, "align KEEP never moves");
		check(MassPlacer.verticalShift(VerticalAlign.GROUND, -62, -60, 8) == 1, "align GROUND puts the bottom at ground - 1");
		check(MassPlacer.verticalShift(VerticalAlign.AUTO, -62, -60, 8) == 0, "align AUTO keeps a surface structure");
		check(MassPlacer.verticalShift(VerticalAlign.AUTO, -40, -60, 8) == -21, "align AUTO drops a floating one to the ground");
		check(MassPlacer.verticalShift(VerticalAlign.AUTO, -120, -60, 8) == 59, "align AUTO lifts a buried one to the ground");

		// 6. WorldgenConfig defaults with no loaded config (what every worldgen path sees before the file loads)
		check(WorldgenConfig.structureGroupsEnabled() && WorldgenConfig.structureFrequenciesEnabled() && WorldgenConfig.poolTuningEnabled()
				&& WorldgenConfig.lootContainerSwapsEnabled() && WorldgenConfig.biomeLayoutEnabled()
				&& WorldgenConfig.deadPlacementFilterEnabled() && WorldgenConfig.groundMatchEnabled(), "every system on by default");
		check(WorldgenConfig.structureDensity() == 1.0, "structureDensity default 1");
		check(WorldgenConfig.poolMaxFractionScale() == 10 && WorldgenConfig.biomeMaxCandidates() == 24 && WorldgenConfig.biomeRounds() == 20,
				"solver constants keep their old values");
		check(WorldgenConfig.groundCenterStride() == 2 && WorldgenConfig.groundNeighbourStride() == 4 && WorldgenConfig.groundSubDepth() == 3
				&& WorldgenConfig.groundMaxDig() == 24 && WorldgenConfig.groundMode() == GroundMode.TERRAIN, "ground sampling keeps its old values");
		check(DevtoolsConfig.placeGap() == 12 && DevtoolsConfig.placeRowGap() == 16 && DevtoolsConfig.placeLabels()
				&& DevtoolsConfig.placeVerticalAlign() == VerticalAlign.AUTO && DevtoolsConfig.placeMaxItems() == 1000 && DevtoolsConfig.placeTickBudgetMs() == 40,
				"/cdevplace defaults");

		System.out.println(fails == 0 ? "ALL " + checks + " PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
