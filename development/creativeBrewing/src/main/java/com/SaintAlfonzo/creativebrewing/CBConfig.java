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
 * Creative Brewing's config, {@code config/creative_brewing-common.toml} (creative_brewing.md 2.3):
 * <ul>
 * <li>{@code [members]}: one switch per nested member, default on; off makes CB's scripts skip that member's rows
 * ({@code global.CB.on(id)}), the member's own data stays.</li>
 * <li>one section per family of moved pack features ({@link #SECTIONS}: {@code brewing}, {@code alcohol},
 * {@code effects}), one switch per feature, default on; scripts read it through {@code global.CB.feature(id)}, Java
 * through {@link #feature}. A section is written only once it has a switch.</li>
 * </ul>
 * Numeric dials that move from creativeengineering-common.toml keep their keys and comments and go in the section of
 * their feature (creative_brewing.md 4.3); add them as their own {@code ForgeConfigSpec} values next to the switch.
 *
 * Scripts read it through {@code global.CB} (kubejs_startup/creative_brewing/cb_core.js). COMMON configs load after
 * mod construction, so a startup script must read it inside an event callback, not at top level; before the file is
 * loaded every switch reads as its default.
 */
public final class CBConfig {

	/** Mod ids of the nested members, from creative_brewing_members.txt (generated from bundle_members). */
	public static final List<String> MEMBERS = readMembers();

	/** Feature sections, in file order. */
	public static final List<String> SECTIONS = List.of("brewing", "alcohol", "effects");

	/**
	 * Feature switches: {section, id, comment lines...}. Add one row per feature as it moves in from patcherMod or the
	 * pack scripts (creative_brewing.md 4); the id is what {@code global.CB.feature(id)} and {@link #feature} take.
	 */
	private static final List<String[]> FEATURE_ROWS = List.of();

	public static final ForgeConfigSpec SPEC;
	private static final Map<String, ForgeConfigSpec.BooleanValue> MEMBER_SWITCHES = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> FEATURES = new LinkedHashMap<>();

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment("One switch per Creative Brewing member. Off: Creative Brewing's scripts skip that member's recipe,",
				"tag and loot rows (the member's own data is untouched). Server scripts re-read it on /reload.").push("members");
		for (String id : MEMBERS) MEMBER_SWITCHES.put(id, b.define(id, true));
		b.pop();

		for (String section : SECTIONS) {
			List<String[]> rows = FEATURE_ROWS.stream().filter(r -> r[0].equals(section)).toList();
			if (rows.isEmpty()) continue;
			b.comment("Creative Brewing " + section + " features (feature_writeup/creative_brewing.md 4). Off: that feature",
					"is skipped entirely. Startup and Java parts need a restart, server scripts re-read on /reload, client",
					"scripts on F3+T.").push(section);
			for (String[] row : rows) {
				String[] comment = new String[row.length - 2];
				System.arraycopy(row, 2, comment, 0, comment.length);
				if (FEATURES.containsKey(row[1])) throw new IllegalStateException("duplicate CB feature id " + row[1]);
				FEATURES.put(row[1], b.comment(comment).define(row[1], true));
			}
			b.pop();
		}
		SPEC = b.build();
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
		try (InputStream in = CBConfig.class.getResourceAsStream("/creative_brewing_members.txt")) {
			if (in == null) return List.of();
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String line; (line = r.readLine()) != null; ) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) ids.add(line);
			}
		} catch (IOException e) {
			throw new IllegalStateException("creative_brewing_members.txt unreadable", e);
		}
		return Collections.unmodifiableList(ids);
	}
}
