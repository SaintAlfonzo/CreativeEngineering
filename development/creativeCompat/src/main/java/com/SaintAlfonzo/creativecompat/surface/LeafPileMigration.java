package com.SaintAlfonzo.creativecompat.surface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * One-time config move (plan_passthrough.md 3.1): the old {@code [leaf_piles] slowFromLayers} becomes
 * {@code [surface.passthrough.leaf_pile] minLayers}, shared by IW leaf piles and Twigs bamboo leaves. Runs on the toml
 * text before Forge loads the file, so the value is in place when the spec is read, and the old section is dropped.
 *
 * <p>Which value wins: the old one, unless it is the old default (4) and the new key already exists (a player who
 * edited {@code minLayers} with the phase 1 jar keeps that edit). An unreadable or out-of-range old value is dropped
 * and the new key is left alone.
 *
 * <p>Plain Java (no game classes) so the headless harness can test {@link #migrate}.
 */
public final class LeafPileMigration {

	private LeafPileMigration() {}

	static final String OLD_HEADER = "[leaf_piles]";
	static final String NEW_HEADER = "[surface.passthrough.leaf_pile]";
	static final int OLD_DEFAULT = 4;

	private static final Pattern OLD_KEY = Pattern.compile("^\\s*slowFromLayers\\s*=\\s*(\\S+)\\s*(#.*)?$");
	private static final Pattern NEW_KEY = Pattern.compile("^(\\s*)minLayers\\s*=\\s*(\\S+)\\s*(#.*)?$");

	/** What {@link #migrate} did. {@code carried} is the value written, or null when none was. */
	public record Result(String text, boolean changed, @Nullable Integer oldValue, @Nullable Integer carried) {}

	/** Migrate {@code file} in place; returns null when the file is missing or had no old section. */
	@Nullable
	public static Result run(Path file) throws IOException {
		if (!Files.isRegularFile(file)) return null;
		String text = Files.readString(file, StandardCharsets.UTF_8);
		Result r = migrate(text);
		if (!r.changed()) return null;
		Files.writeString(file, r.text(), StandardCharsets.UTF_8);
		return r;
	}

	/** The toml text with the old section removed and its value carried (see the class comment). */
	public static Result migrate(String text) {
		List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
		int header = find(lines, OLD_HEADER);
		if (header < 0) return new Result(text, false, null, null);

		// the section's body: up to the next line at column 0 that is a comment or a table header
		int end = header + 1;
		Integer oldValue = null;
		boolean oldKey = false;
		while (end < lines.size()) {
			String l = lines.get(end);
			if (!l.isEmpty() && (l.charAt(0) == '#' || l.charAt(0) == '[')) break;
			Matcher m = OLD_KEY.matcher(l);
			if (m.matches()) {
				oldKey = true;
				oldValue = parse(m.group(1));
			}
			end++;
		}
		// its own comment block right above the header
		int start = header;
		while (start > 0 && lines.get(start - 1).startsWith("#")) start--;
		List<String> out = new ArrayList<>(lines.subList(0, start));
		List<String> rest = new ArrayList<>(lines.subList(end, lines.size()));
		// one blank line between what came before and after, not two
		while (!out.isEmpty() && out.get(out.size() - 1).isBlank() && !rest.isEmpty() && rest.get(0).isBlank()) {
			rest.remove(0);
		}
		out.addAll(rest);

		Integer carried = null;
		if (oldKey && oldValue != null) {
			int newHeader = find(out, NEW_HEADER);
			int keyLine = newHeader < 0 ? -1 : findNewKey(out, newHeader);
			boolean keepNew = keyLine >= 0 && oldValue == OLD_DEFAULT;
			if (!keepNew) {
				carried = oldValue;
				if (keyLine >= 0) {
					Matcher m = NEW_KEY.matcher(out.get(keyLine));
					m.matches();
					out.set(keyLine, m.group(1) + "minLayers = " + oldValue);
				} else if (newHeader >= 0) {
					String indent = leading(out.get(newHeader)) + "\t";
					out.add(newHeader + 1, indent + "minLayers = " + oldValue);
				} else {
					// Forge fills the table's other keys (and rewrites the layout) when it corrects the file
					while (!out.isEmpty() && out.get(out.size() - 1).isBlank()) out.remove(out.size() - 1);
					out.add("");
					out.add(NEW_HEADER);
					out.add("\tminLayers = " + oldValue);
					out.add("");
				}
			}
		}
		return new Result(String.join("\n", out), true, oldValue, carried);
	}

	/** The line whose trimmed text is {@code header}, or -1. */
	private static int find(List<String> lines, String header) {
		for (int i = 0; i < lines.size(); i++) {
			String t = lines.get(i).trim();
			int hash = t.indexOf('#');
			if (hash > 0) t = t.substring(0, hash).trim();
			if (t.equals(header)) return i;
		}
		return -1;
	}

	/** {@code minLayers} inside the table that starts at {@code header} (before the next table header), or -1. */
	private static int findNewKey(List<String> lines, int header) {
		for (int i = header + 1; i < lines.size(); i++) {
			String t = lines.get(i).trim();
			if (t.startsWith("[")) return -1;
			if (NEW_KEY.matcher(lines.get(i)).matches()) return i;
		}
		return -1;
	}

	private static String leading(String s) {
		int i = 0;
		while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
		return s.substring(0, i);
	}

	@Nullable
	private static Integer parse(String s) {
		try {
			int v = Integer.parseInt(s.trim());
			return v >= 1 && v <= 9 ? v : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
