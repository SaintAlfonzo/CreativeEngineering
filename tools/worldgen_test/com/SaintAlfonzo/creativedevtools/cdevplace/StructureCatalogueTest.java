package com.SaintAlfonzo.creativedevtools.cdevplace;

import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueLayout.Placement;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueLayout.Size;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Column;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Home;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Kind;
import com.SaintAlfonzo.creativedevtools.cdevplace.CatalogueRules.Plan;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Structure Catalogue logic tests SCAT-01 to SCAT-04 (feature_writeup/plan_structure_catalogue.md 7) on the real rule file. */
public class StructureCatalogueTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	static void eq(Object got, Object want, String what) {
		check(java.util.Objects.equals(got, want), what + (java.util.Objects.equals(got, want) ? "" : ": got " + got + ", want " + want));
	}

	static CatalogueRules rules;

	/** Tags of a one-off structure id holding exactly these type tags. */
	static Map<String, Set<String>> tagsFor(String id, String... types) {
		Map<String, Set<String>> tags = new LinkedHashMap<>();
		for (String t : types) tags.put("type/" + t, Set.of(id));
		return tags;
	}

	static void whole(String id, String category, String subfamily, String function, String... types) {
		Map<String, Set<String>> tags = tagsFor(id, types);
		Home h = rules.whole(id, CatalogueRules.typesOf(id, tags), tags);
		eq(Arrays.asList(h.category(), h.subfamily(), h.function()), Arrays.asList(category, subfamily, function), "SCAT-01 " + id + " " + Arrays.toString(types));
	}

	static void piece(String id, String subfamily, String function, String biome) {
		Home h = rules.piece(id);
		eq(Arrays.asList(h.subfamily(), h.function(), h.biome()), Arrays.asList(subfamily, function, biome), "SCAT-01 piece " + id);
	}

	public static void main(String[] args) throws Exception {
		Path root = Path.of(args[0]);
		rules = CatalogueRules.parse(Files.readString(root.resolve("kubejs/generation_scripts/worldgen/structure_world.json")));
		scat01();
		scat02();
		scat03();
		scat04();
		snapshot(root);
		System.out.println(fails == 0 ? "ALL " + checks + " OK" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}

	static void scat01() {
		whole("creativeworldgen:meta/x", "Composite", "Pillager", null, "enemy_building", "tower");
		whole("a:balloon", "Sky", "Balloons and temples", null, "sky");
		whole("a:spire", "End", "Constructed", "Towers", "end_constructed", "tower");
		whole("a:rock", "End", "Environmental", null, "end_environmental");
		whole("a:keep", "Nether", "Constructed", "Camps", "nether_constructed", "camp");
		whole("a:reef", "Underwater", "Ruins", null, "underwater_ruin");
		whole("a:wreck", "Underwater", "Shipwrecks", null, "shipwreck");
		whole("a:underwater_fossil", "Underwater", "Fossils", null, "environmental");
		whole("a:mine", "Underground", "Mineshafts", null, "mineshaft");
		whole("a:deep_cabin", "Underground", "Cabins", null, "underground");
		whole("a:hole", "Underground", "Other", null, "underground");
		whole("a:fort", "Pillager", "Outposts", null, "enemy_building", "fort");
		whole("a:wagon", "Pillager", "Siege and wagons", null, "enemy_building", "vehicle");
		whole("a:hut", "Pillager", "Witches and mansions", null, "enemy_building");
		whole("a:portal", "Ruins", "Portals", null, "ruined_portal");
		whole("a:spire_ruin", "Ruins", "Towers", null, "ruin", "tower");
		whole("a:rubble", "Ruins", "Surface ruins", null, "ruin");
		whole("a:boat", "Buildings", "Ships", null, "ship");
		whole("a:keep2", "Buildings", "Towers and castles", null, "castle");
		whole("a:tents", "Buildings", "Camps", null, "camp");
		whole("a:butcher_shop", "Buildings", "Houses and workplaces", "Butcher", "house");
		whole("a:cottage", "Buildings", "Houses and workplaces", "Homes", "house");
		whole("a:statue", "Nature and Decor", "Statues and markers", null, "decoration");
		whole("a:spring", "Nature and Decor", "Environmental", null, "environmental");
		// multi-type ids land by rule priority
		whole("a:m1", "Underground", "Other", null, "house", "underground");
		whole("a:m2", "Pillager", "Towers", null, "ruin", "enemy_building", "tower");
		whole("a:m3", "Buildings", "Ships", null, "ship", "house");

		piece("minecraft:village/plains/streets/straight_01", "Decor and Infrastructure", "Streets", "Plains");
		piece("minecraft:village/common/animals/cows_1", "Decor and Infrastructure", "Animals", "Any");
		piece("minecraft:village/plains/plains_lamp_1", "Decor and Infrastructure", "Lamps", "Plains");
		piece("minecraft:village/plains/houses/plains_small_house_1", "Villager Houses", "Small homes", "Plains");
		piece("minecraft:village/plains/houses/plains_butcher_shop_1", "Villager Houses", "Butcher", "Plains");
		piece("minecraft:village/desert/houses/desert_temple_1", "Villager Structures", "Temples", "Desert");
		piece("minecraft:village/desert/town_centers/desert_fountain_01", "Villager Structures", "Town centres", "Desert");
		piece("kaisyn:village/exclusives/iberian/houses/iberian_temple_1", "Villager Structures", "Temples", "Iberian");
		piece("kaisyn:village/flower_forest_japanese/houses/flower_forest_stable_1", "Villager Structures", "Animal pens", "Flower Forest Japanese");
		piece("wares:village/houses/plains_warehouse", "Villager Structures", "Workplaces", "Plains");
		eq(rules.piece("minecraft:village/common/iron_golem").skip(), "entity", "SCAT-01 golem piece skipped");
		Home sky = rules.piece("skyvillages:bridge_1");
		eq(Arrays.asList(sky.category(), sky.subfamily(), sky.biome()), Arrays.asList("Sky", "Sky village: Decor and Infrastructure", "Sky"), "SCAT-01 sky village piece");
	}

	static void scat02() {
		Map<String, Set<String>> tags = new LinkedHashMap<>();
		tags.put("type/house", Set.of("a:typed", "skip:never"));
		Plan p = rules.build(List.of("a:typed", "b:untagged", "terralith:zpointer/plains"), List.of(), tags);
		eq(p.unmapped().size(), 1, "SCAT-02 one unmapped column");
		eq(p.unmapped().get(0).items().get(0).id(), "b:untagged", "SCAT-02 no type tag is unmapped");
		eq(p.unmapped().get(0).path(), Arrays.asList("Unmapped", null, null, "b"), "SCAT-02 unmapped column named by mod");
		eq(p.markers(), List.of("terralith:zpointer/plains"), "SCAT-02 zpointer skipped");
		eq(p.count(p.mapped(), Kind.WHOLE), 1, "SCAT-02 typed id stays mapped");

		Map<String, Set<String>> dungeonOnly = new LinkedHashMap<>();
		dungeonOnly.put("dungeon/x", Set.of("a:d"));
		eq(rules.build(List.of("a:d"), List.of(), dungeonOnly).unmapped().size(), 1, "SCAT-02 dungeon tag alone is still unmapped");

		// no matching pieces: placed whole and reported
		Map<String, Set<String>> v = new LinkedHashMap<>();
		v.put("type/village", Set.of("towns_and_towers:village_ocean", "minecraft:village_plains"));
		Plan pv = rules.build(List.of("towns_and_towers:village_ocean", "minecraft:village_plains"), List.of("minecraft:village/plains/houses/plains_small_house_1"), v);
		eq(pv.noPieces(), List.of("towns_and_towers:village_ocean"), "SCAT-02 village with no pieces reported");
		eq(pv.replaced(), List.of("minecraft:village_plains"), "SCAT-02 village with pieces replaced");
		eq(pv.count(pv.mapped(), Kind.WHOLE), 1, "SCAT-02 village with no pieces placed whole");
	}

	static List<String> p(String a, String b, String c, String d) {
		return Arrays.asList(a, b, c, d);
	}

	static void scat03() {
		int[] g = rules.levelGaps;
		eq(Arrays.toString(g), "[50, 25, 10, 5]", "SCAT-03 level gaps from the rule file");
		eq(rules.variationGap, 5, "SCAT-03 variation gap");
		CatalogueLayout l = new CatalogueLayout(g, rules.variationGap, rules.originX, rules.originZ, false);
		int[][] steps = {{0, 0}, {5, 3}, {10, 2}, {25, 1}, {50, 0}};
		List<List<String>> paths = List.of(p("A", "s", "f", "b1"), p("A", "s", "f", "b2"), p("A", "s", "g", "b1"), p("A", "t", "f", "b1"), p("B", "s", "f", "b1"));
		int prevMax = 0;
		for (int i = 0; i < paths.size(); i++) {
			Placement at = l.place(paths.get(i), List.of(new Size(7, 4), new Size(3, 2)));
			if (i == 0) eq(at.minX(), 16, "SCAT-03 first mapped column at x=16");
			else eq(at.minX() - prevMax - 1, steps[i][0], "SCAT-03 gap at boundary " + i);
			eq(at.minZ()[0], 8, "SCAT-03 column starts at z=8");
			eq(at.minZ()[1] - at.minZ()[0] - 4, 5, "SCAT-03 variation gap in column");
			prevMax = at.maxX();
		}
		CatalogueLayout un = new CatalogueLayout(g, rules.variationGap, rules.originX, rules.originZ, true);
		Placement a = un.place(p("Unmapped", null, null, "a"), List.of(new Size(9, 3)));
		Placement b = un.place(p("Unmapped", null, null, "b"), List.of(new Size(4, 3)));
		eq(a.maxX(), -16, "SCAT-03 first unmapped column ends at x=-16");
		eq(a.minX() - b.maxX() - 1, 5, "SCAT-03 mirrored side keeps the gap");

		Random r = new Random(1234);
		for (int side = 0; side < 2; side++) {
			CatalogueLayout t = new CatalogueLayout(g, rules.variationGap, rules.originX, rules.originZ, side == 1);
			List<int[]> boxes = new ArrayList<>();
			for (int c = 0; c < 300; c++) {
				List<String> path = p("C" + r.nextInt(3), r.nextBoolean() ? "s" + r.nextInt(3) : null, r.nextBoolean() ? "f" + r.nextInt(3) : null, "b" + c);
				List<Size> sizes = new ArrayList<>();
				int n = 1 + r.nextInt(5);
				for (int i = 0; i < n; i++) sizes.add(new Size(1 + r.nextInt(40), 1 + r.nextInt(40)));
				Placement at = t.place(path, sizes);
				for (int i = 0; i < n; i++) boxes.add(new int[]{at.minX(), at.minZ()[i], at.minX() + sizes.get(i).x() - 1, at.minZ()[i] + sizes.get(i).z() - 1});
			}
			boolean overlap = false;
			for (int i = 0; i < boxes.size() && !overlap; i++)
				for (int j = i + 1; j < boxes.size(); j++) {
					int[] x = boxes.get(i), y = boxes.get(j);
					if (x[0] <= y[2] && y[0] <= x[2] && x[1] <= y[3] && y[1] <= x[3]) { overlap = true; break; }
				}
			check(!overlap, "SCAT-03 no overlapping boxes on a random tree, side " + side);
		}
	}

	static void scat04() {
		eq(CatalogueLayout.gapBetween(rules.levelGaps, p("A", "s", null, "b1"), p("A", "s", null, "b2")), 5, "SCAT-04 no function level: biome gap");
		eq(CatalogueLayout.gapBetween(rules.levelGaps, p("A", "s", null, "b1"), p("A", "s", "f", "b1")), 10, "SCAT-04 skipped function vs present");
		eq(CatalogueLayout.signLevels(null, p("A", null, null, "b")), List.of(0, 3), "SCAT-04 skipped levels get no sign");
		eq(CatalogueLayout.signLevels(p("A", "s", "f", "b"), p("A", "s", "g", "b")), List.of(2, 3), "SCAT-04 a change re-signs deeper levels");
		eq(CatalogueLayout.signZ(0, 4), -8, "SCAT-04 category sign furthest out");
		eq(CatalogueLayout.signZ(3, 4), -2, "SCAT-04 biome sign nearest");

		Home street = rules.piece("minecraft:village/plains/zombie/streets/straight_01");
		eq(street.function(), "Streets", "SCAT-04 zombie street is a street");
		eq(street.biome(), "Plains", "SCAT-04 zombie street merged into its base biome");
		eq(rules.piece("minecraft:village/plains/zombie/houses/plains_small_house_1").biome(), "Plains zombie", "SCAT-04 other zombie pieces keep their own column");

		eq(CatalogueRules.natural("a_2", "a_10") < 0, true, "SCAT-04 natural order");
		eq(CatalogueRules.title("flower_forest"), "Flower Forest", "SCAT-04 title");
		eq(CatalogueLayout.levelSign("Function", "Decor and Infrastructure", 12), List.of("Function", "Decor and", "Infrastructure", "12 items"), "SCAT-04 level sign wraps on words");
	}

	/** The real exports: the plan's snapshot counts, when the dump is present. */
	static void snapshot(Path root) throws Exception {
		Path dump = root.resolve("kubejs/exported/structures.json");
		if (!Files.isRegularFile(dump)) { System.out.println("ok   snapshot skipped: no dump"); return; }
		JsonObject d = CatalogueRules.parseLenient(Files.readString(dump));
		List<String> structures = ids(d.getAsJsonObject("worldgen")), templates = ids(d.getAsJsonObject("templates"));
		JsonObject tagFile = CatalogueRules.parseLenient(Files.readString(root.resolve("kubejs/generation_scripts/worldgen/structure_tags.json")));
		Plan p = rules.build(structures, templates, CatalogueRules.resolveTags(tagFile.getAsJsonObject("tags"), structures));
		long sky = p.mapped().stream().filter(c -> "Sky".equals(c.path().get(0))).flatMap(c -> c.items().stream()).filter(e -> e.kind() == Kind.PIECE).count();
		eq(p.count(p.mapped(), Kind.PIECE) - sky, 1292L, "snapshot village pieces");
		eq(sky, 36L, "snapshot sky village pieces");
		eq(p.count(p.mapped(), Kind.WHOLE), 279, "snapshot whole structures (278 + T&T ocean village, which has no village/ pieces)");
		eq(p.count(p.unmapped(), Kind.WHOLE), 44, "snapshot unmapped");
		eq(p.markers().size(), 133, "snapshot biome markers skipped");
		eq(p.entityPieces().size(), 46, "snapshot entity pieces skipped");
		eq(p.noPieces(), List.of("towns_and_towers:village_ocean"), "snapshot villages with no pieces");
		eq(p.replaced().size(), 32, "snapshot villages replaced by pieces (32 less the ocean village, plus sky villages)");
		List<Column> streets = p.mapped().stream().filter(c -> "Village".equals(c.path().get(0)) && "Streets".equals(c.path().get(2))).toList();
		eq(streets.size(), 25, "snapshot street columns");
		eq(streets.stream().mapToInt(c -> c.items().size()).max().orElse(0), 41, "snapshot longest street column");
		long dry = p.columns().stream().flatMap(c -> c.items().stream()).filter(CatalogueRules.Entry::dry).count();
		eq(dry, 35L, "snapshot dried structures (the map's 33, the T&T ocean village, Terralith's sunken tower)");
		eq(p.mapped().get(0).path().get(0), "Village", "snapshot Village first");
	}

	static List<String> ids(JsonObject byNamespace) {
		List<String> out = new ArrayList<>();
		for (Map.Entry<String, JsonElement> e : byNamespace.entrySet())
			for (JsonElement p : e.getValue().getAsJsonArray()) out.add(e.getKey() + ":" + p.getAsString());
		return out;
	}
}
