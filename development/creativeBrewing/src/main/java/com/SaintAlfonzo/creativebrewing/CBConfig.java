package com.SaintAlfonzo.creativebrewing;

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
 * Creative Brewing's config, {@code config/creativebrewing-common.toml} (creative_brewing.md 2.3):
 * <ul>
 * <li>{@code [members]}: one switch per nested member, default on; off makes CB's scripts skip that member's rows
 * ({@code global.CB.on(id)}), the member's own data stays.</li>
 * <li>one section per family of moved pack features ({@link #SECTIONS}: {@code brewing}, {@code alcohol},
 * {@code effects}), one switch per feature, default on; scripts read it through {@code global.CB.feature(id)}, Java
 * through {@link #feature}. A section is written only once it has a switch.</li>
 * </ul>
 * Dials moved from creativeengineering-common.toml keep their keys, comments and defaults (creative_brewing.md 4.3):
 * {@code [alcohol]}, {@code [effects]}, {@code [magic_resistance]}, {@code [copper_poisoning]} ({@link #MOVED_SECTIONS},
 * phase 3, 2026-11-01). {@code [alcohol]} and {@code [effects]} also take that family's feature switches.
 *
 * Scripts read it through {@code global.CB} (kubejs_startup/creativebrewing/cb_core.js). COMMON configs load after
 * mod construction, so a startup script must read it inside an event callback, not at top level; before the file is
 * loaded every switch reads as its default.
 */
public final class CBConfig {

	/** Mod ids of the nested members, from creativebrewing_members.txt (generated from bundle_members). */
	public static final List<String> MEMBERS = readMembers();

	/** Feature sections, in file order. */
	public static final List<String> SECTIONS = List.of("brewing", "alcohol");

	/**
	 * Feature switches: {section, id, comment lines...}. Add one row per feature as it moves in from patcherMod or the
	 * pack scripts (creative_brewing.md 4); the id is what {@code global.CB.feature(id)} and {@link #feature} take.
	 */
	private static final List<String[]> FEATURE_ROWS = List.of();

	/** Feature sections that also hold moved dials (built by their dial class). */
	private static final List<String> DIAL_SECTIONS = List.of("alcohol");

	/** Sections moved here from creativeengineering-common.toml (phase 3), for the carry-over and the leftover WARN. */
	public static final List<String> MOVED_SECTIONS = List.of("alcohol");

	public static final ForgeConfigSpec SPEC;
	public static final Alcohol ALCOHOL;
	private static final Map<String, ForgeConfigSpec.BooleanValue> MEMBER_SWITCHES = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> FEATURES = new LinkedHashMap<>();

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment("One switch per Creative Brewing member. Off: Creative Brewing's scripts skip that member's recipe,",
				"tag and loot rows (the member's own data is untouched). Server scripts re-read it on /reload.").push("members");
		for (String id : MEMBERS) MEMBER_SWITCHES.put(id, b.define(id, true));
		b.pop();

		// Sections with only feature switches; alcohol and effects also hold dials moved from patcherMod and write
		// their switches inside their own section (featureRows).
		for (String section : SECTIONS) {
			if (DIAL_SECTIONS.contains(section) || rows(section).isEmpty()) continue;
			b.comment("Creative Brewing " + section + " features (feature_writeup/creative_brewing.md 4). Off: that feature",
					"is skipped entirely. Startup and Java parts need a restart, server scripts re-read on /reload, client",
					"scripts on F3+T.").push(section);
			featureRows(b, section);
			b.pop();
		}
		// Moved from creativeengineering-common.toml on 2026-11-01 (creative_brewing.md 4.2 phase 3): same keys,
		// comments and defaults; ConfigRename.mergeSections carries the player's values over on the first boot.
		ALCOHOL = new Alcohol(b);
		SPEC = b.build();
	}

	private static List<String[]> rows(String section) {
		return FEATURE_ROWS.stream().filter(r -> r[0].equals(section)).toList();
	}

	/** The feature switches of {@code section}, inside the section the caller has pushed. */
	private static void featureRows(ForgeConfigSpec.Builder b, String section) {
		for (String[] row : rows(section)) {
			String[] comment = new String[row.length - 2];
			System.arraycopy(row, 2, comment, 0, comment.length);
			if (FEATURES.containsKey(row[1])) throw new IllegalStateException("duplicate CB feature id " + row[1]);
			FEATURES.put(row[1], b.comment(comment).define(row[1], true));
		}
	}

	private CBConfig() {}

	/** True if feature switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
	public static boolean feature(String id) {
		ForgeConfigSpec.BooleanValue v = FEATURES.get(id);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	/** True if {@code modId} is one of the nested members. */
	public static boolean isMember(String modId) {
		return MEMBERS.contains(modId);
	}

	/** True if {@code modId} is a member and its switch is on (or the config is not loaded yet). */
	public static boolean member(String modId) {
		ForgeConfigSpec.BooleanValue v = MEMBER_SWITCHES.get(modId);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	public static List<String> members() {
		return MEMBERS;
	}

	private static List<String> readMembers() {
		List<String> ids = new ArrayList<>();
		try (InputStream in = CBConfig.class.getResourceAsStream("/creativebrewing_members.txt")) {
			if (in == null) return List.of();
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String line; (line = r.readLine()) != null; ) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) ids.add(line);
			}
		} catch (IOException e) {
			throw new IllegalStateException("creativebrewing_members.txt unreadable", e);
		}
		return Collections.unmodifiableList(ids);
	}

	/** Alcoholic drinks (alcohol.md 11): the pack side lives in KubeJS {@code global.alcohol}; this is the client tooltip. */
	public static final class Alcohol {
		public final ForgeConfigSpec.BooleanValue quarkLine;

		private Alcohol(ForgeConfigSpec.Builder b) {
			b.comment("Alcoholic drinks (client side).").push("alcohol");

			quarkLine = b.comment(
					"Replace Quark's saturation word ('Disgusting Snack') with 'Intoxicating Drink' on every food whose",
					"effects include Brewin' and Chewin's Intoxication (alcohol.IntoxicatingDrinkTooltip)."
			).define("quarkLine", true);

			featureRows(b, "alcohol");
			b.pop(); // alcohol
		}
	}



}
