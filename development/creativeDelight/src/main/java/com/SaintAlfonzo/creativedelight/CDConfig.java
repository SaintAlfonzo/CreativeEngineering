package com.SaintAlfonzo.creativedelight;

import net.minecraftforge.common.ForgeConfigSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creative Delight's config, {@code config/creative_delight-common.toml} (creative_delight.md 8.1):
 * <ul>
 * <li>{@code [members]}: one switch per nested member, default on; off makes CD's scripts skip that member's rows
 * ({@code global.CD.on(id)}), the member's own data stays. The base members ({@link #BASE_MEMBERS}, Farmer's
 * Delight) get no switch: every other member needs them, so they are always on.</li>
 * <li>{@code [qol]}: one switch per QoL feature of section 8 ({@link #QOL}), default on; scripts read it through
 * {@code global.CD.qol(id)}, Java through {@link #qol}.</li>
 * <li>{@code [worldgen]}: one switch per worldgen system CD owns ({@link #WORLDGEN}, creative_delight.md 24), default on;
 * scripts read it through {@code global.CD.worldgen(id)}, Java through {@link #worldgen}.</li>
 * </ul>
 * The {@code integrations} and {@code swappers} sections of 8.1 are added with the first feature that uses them.
 *
 * Scripts read it through {@code global.CD} (kubejs_startup/creative_delight/cd_core.js). COMMON configs load after
 * mod construction, so a startup script must read it inside an event callback, not at top level; before the file
 * is loaded every switch reads as its default.
 */
public final class CDConfig {

	/** Mod ids of the nested members, from creative_delight_members.txt (generated from bundle_members). */
	public static final List<String> MEMBERS = readMembers();

	/** Members every other member depends on: no switch, always on while loaded. */
	public static final List<String> BASE_MEMBERS = List.of("farmersdelight");

	/** QoL switch ids (creative_delight.md 8). QoL-3 ({@code foodTypes}) gets its key when it moves. */
	public static final String REMAINDER_BUFFER = "remainderBuffer";
	public static final String PLACEABLE_TOOLTIPS = "placeableTooltips";
	public static final String COMPOST = "compost";
	public static final String MANUAL_ONLY_ROUTES = "manualOnlyRoutes";
	public static final String WTHIT_CROP_LINE = "wthitCropLine";
	public static final String JEI_INFO_PAGES = "jeiInfoPages";

	/** Worldgen switch ids (creative_delight.md 24). */
	public static final String WILD_CROPS = "wildCrops";

	public static final ForgeConfigSpec SPEC;
	private static final Map<String, ForgeConfigSpec.BooleanValue> MEMBER_SWITCHES = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> QOL = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> WORLDGEN = new LinkedHashMap<>();

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment("One switch per Creative Delight member. Off: Creative Delight's scripts skip that member's recipe,",
				"tag and loot rows (the member's own data is untouched). Server scripts re-read it on /reload.").push("members");
		for (String id : MEMBERS) {
			if (BASE_MEMBERS.contains(id)) continue;
			MEMBER_SWITCHES.put(id, b.define(id, true));
		}
		b.pop();

		b.comment("One switch per Creative Delight QoL feature (feature_writeup/creative_delight.md 8). Off: that feature",
				"is skipped entirely. Startup and Java parts need a restart, server scripts re-read on /reload, client",
				"scripts on F3+T.").push("qol");
		qolSwitch(b, REMAINDER_BUFFER, "QoL-1: ingredient leftovers (bottles, bowls, buckets) stay in the Farmer's Delight cooking",
				"pot and the Farmer's Respite kettle (two take-only slots). Off: they are thrown out as in the original mods.",
				"The stall and recycle dials are creativeengineering-common.toml [cooking]; the oven is its own block there.");
		qolSwitch(b, PLACEABLE_TOOLTIPS, "QoL-2: the 'Placeable' / 'Placeable on plate' tooltip lines, in place of the mods' own.");
		qolSwitch(b, COMPOST, "QoL-4: Creative Delight's composter chances for member foods. Off: those foods fall to the pack's",
				"compost rules.");
		qolSwitch(b, MANUAL_ONLY_ROUTES, "QoL-5: grid meals on cooked food re-made as manual-only crafts, heatless routes for pot",
				"meals, tortilla wraps (Veggies, Vintage and Fungi Delight). Off: the mods' own recipes stay.");
		qolSwitch(b, WTHIT_CROP_LINE, "QoL-6: the WTHIT 'Growth' line for plants WTHIT doesn't cover (only with WTHIT).",
				"Dials: creativeengineering-common.toml [crop_growth].");
		qolSwitch(b, JEI_INFO_PAGES, "QoL-6: JEI information pages for member mechanics (only with JEI).");
		b.pop();

		b.comment("One switch per Creative Delight worldgen system (feature_writeup/creative_delight.md 24). Rejoin the world",
				"to apply; only chunks generated afterwards change.").push("worldgen");
		WORLDGEN.put(WILD_CROPS, b.comment("Which biomes each wild crop and fruit tree spawns in (_worldgen/wild_crops.json).",
				"Off: every crop as its mod ships it.").define(WILD_CROPS, true));
		b.pop();
		SPEC = b.build();
	}

	private CDConfig() {}

	private static void qolSwitch(ForgeConfigSpec.Builder b, String id, String... comment) {
		QOL.put(id, b.comment(comment).define(id, true));
	}

	/** True if QoL switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
	public static boolean qol(String id) {
		ForgeConfigSpec.BooleanValue v = QOL.get(id);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	/** True if worldgen switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
	public static boolean worldgen(String id) {
		ForgeConfigSpec.BooleanValue v = WORLDGEN.get(id);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	/** True if {@code modId} is one of the nested members. */
	public static boolean isMember(String modId) {
		return MEMBERS.contains(modId);
	}

	/** True if {@code modId} is a member and its switch is on (or the config is not loaded yet, or it is a base member). */
	public static boolean member(String modId) {
		if (BASE_MEMBERS.contains(modId)) return MEMBERS.contains(modId);
		ForgeConfigSpec.BooleanValue v = MEMBER_SWITCHES.get(modId);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	public static List<String> members() {
		return MEMBERS;
	}

	private static List<String> readMembers() {
		List<String> ids = new ArrayList<>();
		try (InputStream in = CDConfig.class.getResourceAsStream("/creative_delight_members.txt")) {
			if (in == null) return List.of();
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String line; (line = r.readLine()) != null; ) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) ids.add(line);
			}
		} catch (IOException e) {
			throw new IllegalStateException("creative_delight_members.txt unreadable", e);
		}
		return Collections.unmodifiableList(ids);
	}
}
