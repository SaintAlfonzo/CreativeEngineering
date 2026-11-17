package com.SaintAlfonzo.creativeloot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Text files of the Creative Loot jar for its KubeJS scripts (a copy of Creative Delight's CDResources). KubeJS's
 * {@code JsonIO} only reads files under the game folder, so a script that ships a JSON next to it (the loot tuning
 * tables, {@code data/creativeloot/kubejs/loot/}) reads the pack folder copy first (dev) and falls back to the jar
 * copy through {@link #text}.
 */
public final class CLResources {

	private CLResources() {}

	/** The jar resource at {@code path} (no leading slash) as UTF-8 text, or null if the jar has no such file. */
	public static String text(String path) {
		try (InputStream in = CLResources.class.getResourceAsStream("/" + path)) {
			if (in == null) return null;
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}
}
