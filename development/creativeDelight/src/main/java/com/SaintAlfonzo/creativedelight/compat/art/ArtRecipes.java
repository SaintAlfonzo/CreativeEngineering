package com.SaintAlfonzo.creativedelight.compat.art;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What {@link CompartmentArt} builds and from what. Plain data (texture ids as {@code namespace:path} strings, without
 * {@code textures/} and {@code .png}), no Minecraft classes. Output paths are the ones the compartment packs' models name, so
 * nothing else changed when the copied PNGs left the jar (2026-11-05); kubejs/assets can still override any of them.
 */
public final class ArtRecipes {

	private ArtRecipes() {}

	// ------------------------------------------------------------------ Cooking for Blockheads racks and fruit baskets

	/** Wood -> its planks texture (the owner's rack art, textureswapper/rack/, was made from exactly these files). */
	public static final Map<String, String> PLANKS = new LinkedHashMap<>();

	static {
		for (String w : List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "crimson", "warped"))
			PLANKS.put(w, "minecraft:block/" + w + "_planks");
		PLANKS.put("twisted", "architects_palette:block/twisted_planks");
		PLANKS.put("dreamwood", "botania:block/dreamwood_planks");
		PLANKS.put("livingwood", "botania:block/livingwood_planks");
		PLANKS.put("smoked", "create_dd:block/palettes/wood_types/smoked/smoked_planks");
		PLANKS.put("treated_wood", "immersiveengineering:block/wooden_decoration/treated_wood_horizontal");
		PLANKS.put("vertical_treated_wood", "immersiveengineering:block/wooden_decoration/treated_wood_vertical");
		PLANKS.put("rubberwood", "myrtrees:block/rubberwood_planks");
		PLANKS.put("ancient", "quark:block/ancient_planks");
		PLANKS.put("bamboo", "quark:block/bamboo_planks");
		PLANKS.put("driftwood", "upgrade_aquatic:block/driftwood_planks");
	}

	/** Used when a wood's mod is not installed (the racks and baskets are registered either way). */
	public static final String FALLBACK_PLANKS = "minecraft:block/oak_planks";

	public static final String CFB_SPICE_RACK = "cookingforblockheads:block/spicerack";
	public static final String CFB_TOOL_RACK = "cookingforblockheads:block/tool_rack";

	/** The tool rack's hooks ({x, y}): kept as Cooking for Blockheads draws them, the board around them takes the wood. */
	public static final int[][] TOOL_RACK_HOOKS = {{1, 3}, {2, 3}, {0, 4}, {1, 4}, {2, 4}, {3, 4}};

	public static String fruitBasket(String wood) {
		return "creativedelight:block/fruit_basket/" + wood;
	}

	public static String rack(String wood, String kind) {
		return "creativedelight:block/rack/" + wood + "_" + kind;
	}

	// ------------------------------------------------------------------ Incubation nests, eggs and crates

	/** One Incubation-based texture: output, source, and a colour map (empty = the source as it is). */
	public record Derived(String out, String source, String mod, Map<Integer, Integer> colours) {}

	private static Map<Integer, Integer> map(int... pairs) {
		Map<Integer, Integer> m = new LinkedHashMap<>();
		for (int i = 0; i < pairs.length; i += 2) m.put(pairs[i], pairs[i + 1]);
		return m;
	}

	/** Colours as ARGB. Duck shades and goose shades are the owner's picks of 2026-10-21 (nests.md 4). */
	public static final List<Derived> INCUBATION = List.of(
			new Derived("creativedelight:block/emu_eggs", "incubation:block/turkey_eggs", "alexsmobs",
					map(0xFFFFEFF3, 0xFF1DB2B8, 0xFFE8DAD0, 0xFF108E9C, 0xFFCCBBA7, 0xFF077082)),
			new Derived("creativedelight:block/duck_eggs", "incubation:block/duck_eggs", "untitledduckmod", Map.of()),
			new Derived("creativedelight:block/goose_eggs", "incubation:block/turkey_eggs", "untitledduckmod", Map.of()),
			new Derived("creativedelight:block/duck_nest", "incubation:block/hay_nest", "untitledduckmod", Map.of()),
			new Derived("creativedelight:block/goose_nest", "incubation:block/hay_nest", "untitledduckmod", Map.of()),
			new Derived("creativedelight:item/duck_nest", "incubation:item/hay_nest", "untitledduckmod", Map.of()),
			new Derived("creativedelight:item/goose_nest", "incubation:item/hay_nest", "untitledduckmod", Map.of()),
			new Derived("creativedelight:block/duck_egg_crate_top", "incubation:block/chicken_egg_crate_top", "untitledduckmod",
					map(0xFFE4D3A2, 0xFFC6EAD4, 0xFFCBB988, 0xFFA9D4C1, 0xFFF0E6C6, 0xFFC6EAD4, 0xFFA99861, 0xFFA9D4C1,
							0xFF7F724F, 0xFF95BCA9)),
			new Derived("creativedelight:block/duck_egg_crate_side", "incubation:block/chicken_egg_crate_side", "untitledduckmod",
					map(0xFFE4D3A2, 0xFFC6EAD4, 0xFFCBB988, 0xFFA9D4C1, 0xFFF0E6C6, 0xFFC6EAD4, 0xFFA99861, 0xFFA9D4C1,
							0xFF513D24, 0xFF95BCA9, 0xFF7F724F, 0xFFA9D4C1)),
			new Derived("creativedelight:block/goose_egg_crate_top", "incubation:block/chicken_egg_crate_top", "untitledduckmod",
					map(0xFFE4D3A2, 0xFFFFEFF3, 0xFFCBB988, 0xFFE8DAD0, 0xFFF0E6C6, 0xFFFFEFF3, 0xFFA99861, 0xFFE8DAD0,
							0xFF7F724F, 0xFFCCBBA7)),
			new Derived("creativedelight:block/goose_egg_crate_side", "incubation:block/chicken_egg_crate_side", "untitledduckmod",
					map(0xFFE4D3A2, 0xFFFFEFF3, 0xFFCBB988, 0xFFE8DAD0, 0xFFF0E6C6, 0xFFFFEFF3, 0xFFA99861, 0xFFE8DAD0,
							0xFF513D24, 0xFFCCBBA7, 0xFF7F724F, 0xFFE8DAD0)));

	/** {@code ns:path} -> {@code assets/ns/textures/path.png}. */
	public static String file(String id) {
		int c = id.indexOf(':');
		return "assets/" + id.substring(0, c) + "/textures/" + id.substring(c + 1) + ".png";
	}
}
