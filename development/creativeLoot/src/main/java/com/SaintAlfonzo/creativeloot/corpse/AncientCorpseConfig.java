package com.SaintAlfonzo.creativeloot.corpse;

import com.SaintAlfonzo.creativeloot.CLResources;
import com.SaintAlfonzo.creativeloot.CreativeLoot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code "ancient_corpses"} block of {@code loot/loot_config.json} (feature_writeup/plan_corpses.md 3.2 and 3.4,
 * rulings 6, 11 to 17, 21; every value configurable, item 19). Read from the pack folder copy first, then the jar copy,
 * on server start and on each /reload; a key left out keeps the default below. Immutable once built: worldgen threads
 * read {@link #get()} while a reload swaps in a new one.
 */
public final class AncientCorpseConfig {
	static final String FILE = "data/creativeloot/kubejs/loot/loot_config.json";

	private static volatile AncientCorpseConfig current;

	public final boolean enabled;
	public final int footprint;
	public final int headroom;
	public final Cave cave;
	public final Ocean ocean;
	public final Remains remains;
	public final Armor armor;
	public final Hands hands;

	public record Cave(double chunkChance, int attempts, int search, int maxY, List<int[]> curveY, List<Double> curveWeight,
			String lootTable) {}

	public record Ocean(double baseChunkChance, Map<String, Double> temperatureFactor, Map<String, Double> depthFactor,
			List<String> coldBiomes, List<String> warmBiomes, int attempts, String lootTable) {}

	/** Remains drop rows: {@code min} to {@code max} of the item, or one at {@code chance} (when chance is below 1). */
	public record Drop(String item, int min, int max, double chance) {}

	public record Remains(boolean enabled, double chunkChance, List<String> biomes, int attempts, int search, List<Drop> drops) {}

	public record Armor(List<List<String>> chains, List<Double> chances, Map<String, Integer> materialWeights,
			Map<String, Map<String, String>> items, double damageMin, double damageMax, double enchantChance,
			int enchantLevelMin, int enchantLevelMax, double copperWaxChance, Map<String, Double> waxedStateWeights,
			double waxedSameStateChance, double dyeSameColorChance, double dyeMinDistance) {}

	/** An offhand supply stack: {@code min} to {@code max} of the item. */
	public record Supply(String item, int min, int max) {}

	/**
	 * Held items (rulings 24 to 26, 28; TODO `corpses #8`). {@code items} = material -> tool type -> item id;
	 * {@code supplies} = corpse kind ({@code cave}, {@code ocean}) -> offhand supply.
	 */
	public record Hands(double mainChance, double offhandChance, double offhandSupplyChance, Map<String, Supply> supplies,
			boolean guaranteeTool, Map<String, Integer> toolWeights, Map<String, Integer> materialWeights,
			Map<String, Map<String, String>> items, double sameMaterialChance, double damageMin, double damageMax,
			double enchantChance, int enchantLevelMin, int enchantLevelMax) {}

	private AncientCorpseConfig(JsonObject o) {
		enabled = bool(o, "enabled", true);
		footprint = integer(o, "footprint", 3);
		headroom = integer(o, "headroom", 3);

		JsonObject c = obj(o, "cave");
		List<int[]> ys = new ArrayList<>();
		List<Double> ws = new ArrayList<>();
		JsonArray curve = c.has("y_curve") ? c.getAsJsonArray("y_curve") : null;
		if (curve == null || curve.isEmpty()) {
			int[][] def = {{-64}, {-32}, {0}, {16}, {24}, {32}, {48}};
			double[] dw = {1.0, 0.85, 0.65, 0.5, 0.3, 0.04, 0.01};
			for (int i = 0; i < def.length; i++) {
				ys.add(def[i]);
				ws.add(dw[i]);
			}
		} else {
			for (JsonElement e : curve) {
				JsonArray p = e.getAsJsonArray();
				ys.add(new int[]{p.get(0).getAsInt()});
				ws.add(p.get(1).getAsDouble());
			}
		}
		cave = new Cave(dbl(c, "chunk_chance", 1.0 / 32.0), integer(c, "attempts", 12), integer(c, "search", 8),
				integer(c, "max_y", 48), ys, ws, str(c, "loot_table", "creativeloot:corpse/cave"));

		JsonObject oc = obj(o, "ocean");
		ocean = new Ocean(dbl(oc, "base_chunk_chance", 1.0 / 288.0),
				dblMap(oc, "temperature_factor", Map.of("warm", 1.0, "neutral", 2.0, "cold", 3.0)),
				dblMap(oc, "depth_factor", Map.of("shallow", 1.0, "deep", 6.0)),
				strList(oc, "cold_biomes", List.of("minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:frozen_ocean",
						"minecraft:deep_frozen_ocean")),
				strList(oc, "warm_biomes", List.of("minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:warm_ocean")),
				integer(oc, "attempts", 8), str(oc, "loot_table", "creativeloot:corpse/ocean"));

		JsonObject r = obj(o, "remains");
		List<Drop> drops = new ArrayList<>();
		if (r.has("drops")) {
			for (JsonElement e : r.getAsJsonArray("drops")) {
				JsonObject d = e.getAsJsonObject();
				if (!present(d)) continue;
				drops.add(new Drop(str(d, "item", "minecraft:bone"), integer(d, "min", 1), integer(d, "max", integer(d, "min", 1)),
						dbl(d, "chance", 1.0)));
			}
		} else {
			drops.add(new Drop("minecraft:bone", 1, 3, 1.0));
			drops.add(new Drop("minecraft:skeleton_skull", 1, 1, 0.25));
		}
		remains = new Remains(present(r), dbl(r, "chunk_chance", 1.0 / 8.0), strList(r, "biomes", List.of("biomesoplenty:visceral_heap")),
				integer(r, "attempts", 12), integer(r, "search", 24), drops);

		JsonObject a = obj(o, "armor");
		List<List<String>> chains = new ArrayList<>();
		if (a.has("chains")) {
			for (JsonElement e : a.getAsJsonArray("chains")) {
				List<String> chain = new ArrayList<>();
				for (JsonElement s : e.getAsJsonArray()) chain.add(s.getAsString());
				chains.add(chain);
			}
		} else {
			chains.add(List.of("feet", "legs", "chest", "head"));
			chains.add(List.of("head", "chest", "feet", "legs"));
			chains.add(List.of("chest", "feet", "legs", "head"));
		}
		List<Double> chances = new ArrayList<>();
		if (a.has("chances")) for (JsonElement e : a.getAsJsonArray("chances")) chances.add(e.getAsDouble());
		else chances.addAll(List.of(0.56, 0.35, 0.15, 0.05));
		Map<String, Integer> weights = new LinkedHashMap<>();
		if (a.has("material_weights")) {
			for (Map.Entry<String, JsonElement> e : a.getAsJsonObject("material_weights").entrySet()) weights.put(e.getKey(), e.getValue().getAsInt());
		} else {
			weights.put("wood", 25);
			weights.put("leather", 25);
			weights.put("dyed_leather", 25);
			weights.put("copper", 25);
			weights.put("chain", 8);
			weights.put("iron", 8);
		}
		Map<String, Map<String, String>> items = new LinkedHashMap<>();
		if (a.has("items")) {
			for (Map.Entry<String, JsonElement> e : a.getAsJsonObject("items").entrySet()) {
				JsonObject set = e.getValue().getAsJsonObject();
				if (!present(set)) continue; // the material's mod is not loaded: it never rolls
				Map<String, String> slots = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> s : set.entrySet()) if (!s.getKey().equals("requires")) slots.put(s.getKey(), s.getValue().getAsString());
				items.put(e.getKey(), slots);
			}
		} else {
			items.put("wood", set("immersive_armors:wooden_"));
			items.put("leather", set("minecraft:leather_"));
			items.put("dyed_leather", set("minecraft:leather_"));
			items.put("copper", set("everythingcopper:copper_"));
			items.put("chain", set("minecraft:chainmail_"));
			items.put("iron", set("minecraft:iron_"));
		}
		JsonObject cu = a.has("copper") ? a.getAsJsonObject("copper") : new JsonObject();
		JsonObject dy = a.has("dye") ? a.getAsJsonObject("dye") : new JsonObject();
		JsonArray dmg = a.has("damage") ? a.getAsJsonArray("damage") : null;
		JsonArray lv = a.has("enchant_levels") ? a.getAsJsonArray("enchant_levels") : null;
		armor = new Armor(chains, chances, weights, items,
				dmg != null ? dmg.get(0).getAsDouble() : 0.3, dmg != null ? dmg.get(1).getAsDouble() : 0.9,
				dbl(a, "enchant_chance", 0.05), lv != null ? lv.get(0).getAsInt() : 5, lv != null ? lv.get(1).getAsInt() : 15,
				dbl(cu, "wax_chance", 0.1),
				dblMap(cu, "waxed_state_weights", Map.of("unaffected", 0.5, "exposed", 1.0, "weathered", 1.0)),
				dbl(cu, "waxed_same_state_chance", 0.5),
				dbl(dy, "same_color_chance", 0.5), dbl(dy, "min_distance", 200.0));

		hands = hands(obj(o, "hands"));
	}

	private static Hands hands(JsonObject h) {
		Map<String, Supply> supplies = new LinkedHashMap<>();
		if (h.has("offhand_supplies")) {
			for (Map.Entry<String, JsonElement> e : h.getAsJsonObject("offhand_supplies").entrySet()) {
				JsonObject d = e.getValue().getAsJsonObject();
				if (!present(d)) continue;
				supplies.put(e.getKey(), new Supply(str(d, "item", "minecraft:torch"), integer(d, "min", 1), integer(d, "max", integer(d, "min", 1))));
			}
		} else {
			supplies.put("cave", new Supply("minecraft:torch", 1, 6));
			supplies.put("ocean", new Supply("minecraft:bread", 1, 3));
		}
		Map<String, Integer> types = intMap(h, "tool_weights", Map.of("sword", 1, "pickaxe", 1));
		Map<String, Integer> weights = new LinkedHashMap<>();
		if (h.has("material_weights")) {
			weights = intMap(h, "material_weights", Map.of());
		} else {
			weights.put("wood", 25);
			weights.put("stone", 25);
			weights.put("copper", 25);
			weights.put("iron", 8);
		}
		Map<String, Map<String, String>> items = new LinkedHashMap<>();
		if (h.has("items")) {
			for (Map.Entry<String, JsonElement> e : h.getAsJsonObject("items").entrySet()) {
				JsonObject set = e.getValue().getAsJsonObject();
				if (!present(set)) continue; // the material's mod is not loaded: it never rolls
				Map<String, String> m = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> t : set.entrySet()) if (!t.getKey().equals("requires")) m.put(t.getKey(), t.getValue().getAsString());
				items.put(e.getKey(), m);
			}
		} else {
			items.put("wood", Map.of("sword", "minecraft:wooden_sword", "pickaxe", "minecraft:wooden_pickaxe"));
			items.put("stone", Map.of("sword", "minecraft:stone_sword", "pickaxe", "minecraft:stone_pickaxe"));
			if (loaded("everythingcopper")) {
				items.put("copper", Map.of("sword", "everythingcopper:copper_sword", "pickaxe", "everythingcopper:copper_pickaxe"));
			}
			items.put("iron", Map.of("sword", "minecraft:iron_sword", "pickaxe", "minecraft:iron_pickaxe"));
		}
		JsonArray dmg = h.has("damage") ? h.getAsJsonArray("damage") : null;
		JsonArray lv = h.has("enchant_levels") ? h.getAsJsonArray("enchant_levels") : null;
		return new Hands(dbl(h, "main_chance", 0.45), dbl(h, "offhand_chance", 0.2), dbl(h, "offhand_supply_chance", 0.5), supplies,
				bool(h, "guarantee_tool", true), types, weights, items, dbl(h, "same_material_chance", 0.5),
				dmg != null ? dmg.get(0).getAsDouble() : 0.3, dmg != null ? dmg.get(1).getAsDouble() : 0.9,
				dbl(h, "enchant_chance", 0.05), lv != null ? lv.get(0).getAsInt() : 5, lv != null ? lv.get(1).getAsInt() : 15);
	}

	private static Map<String, Integer> intMap(JsonObject o, String k, Map<String, Integer> d) {
		if (!o.has(k)) return d;
		Map<String, Integer> out = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> e : o.getAsJsonObject(k).entrySet()) out.put(e.getKey(), e.getValue().getAsInt());
		return out;
	}

	private static Map<String, String> set(String prefix) {
		Map<String, String> m = new LinkedHashMap<>();
		m.put("head", prefix + "helmet");
		m.put("chest", prefix + "chestplate");
		m.put("legs", prefix + "leggings");
		m.put("feet", prefix + "boots");
		return m;
	}

	public static AncientCorpseConfig get() {
		AncientCorpseConfig c = current;
		if (c == null) {
			reload();
			c = current;
		}
		return c;
	}

	/** Re-reads the file (pack folder first, then the jar); a missing or broken block falls back to the defaults. */
	public static void reload() {
		String text = null;
		Path pack = FMLPaths.GAMEDIR.get().resolve("kubejs").resolve(FILE);
		try {
			if (Files.isRegularFile(pack)) text = Files.readString(pack, StandardCharsets.UTF_8);
		} catch (Exception e) {
			CreativeLoot.LOGGER.warn("[creativeloot] ancient corpses: could not read {}: {}", pack, e.toString());
		}
		if (text == null) text = CLResources.text(FILE);
		JsonObject block = new JsonObject();
		try {
			if (text != null) {
				JsonObject root = JsonParser.parseString(text).getAsJsonObject();
				if (root.has("ancient_corpses")) block = root.getAsJsonObject("ancient_corpses");
			}
			current = new AncientCorpseConfig(block);
		} catch (Exception e) {
			CreativeLoot.LOGGER.error("[creativeloot] ancient corpses: bad \"ancient_corpses\" block in loot_config.json, using defaults: {}", e.toString());
			current = new AncientCorpseConfig(new JsonObject());
		}
	}

	/** False when the object has {@code "requires": <mod id>} (or a list of ids) and one of them is not loaded. */
	static boolean present(JsonObject o) {
		if (!o.has("requires")) return true;
		JsonElement req = o.get("requires");
		if (req.isJsonArray()) {
			for (JsonElement e : req.getAsJsonArray()) if (!loaded(e.getAsString())) return false;
			return true;
		}
		return loaded(req.getAsString());
	}

	private static boolean loaded(String mod) {
		return ModList.get() == null || ModList.get().isLoaded(mod);
	}

	private static JsonObject obj(JsonObject o, String k) {
		return o.has(k) && o.get(k).isJsonObject() ? o.getAsJsonObject(k) : new JsonObject();
	}

	private static boolean bool(JsonObject o, String k, boolean d) {
		return o.has(k) ? o.get(k).getAsBoolean() : d;
	}

	private static int integer(JsonObject o, String k, int d) {
		return o.has(k) ? o.get(k).getAsInt() : d;
	}

	private static double dbl(JsonObject o, String k, double d) {
		return o.has(k) ? o.get(k).getAsDouble() : d;
	}

	private static String str(JsonObject o, String k, String d) {
		return o.has(k) ? o.get(k).getAsString() : d;
	}

	private static List<String> strList(JsonObject o, String k, List<String> d) {
		if (!o.has(k)) return d;
		List<String> out = new ArrayList<>();
		for (JsonElement e : o.getAsJsonArray(k)) out.add(e.getAsString());
		return out;
	}

	private static Map<String, Double> dblMap(JsonObject o, String k, Map<String, Double> d) {
		if (!o.has(k)) return d;
		Map<String, Double> out = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> e : o.getAsJsonObject(k).entrySet()) out.put(e.getKey(), e.getValue().getAsDouble());
		return out;
	}
}
