package com.SaintAlfonzo.creativeworldgen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Text files of the Creative Worldgen jar for its KubeJS scripts (a copy of Creative Delight's CDResources). KubeJS's
 * {@code JsonIO} only reads files under the game folder, so a script that ships a JSON next to it (the loot tuning
 * tables, {@code data/creativeworldgen/kubejs/loot/}) reads the pack folder copy first (dev) and falls back to the jar
 * copy through {@link #text}.
 */
public final class CWGResources {

	private CWGResources() {}

	/** The jar resource at {@code path} (no leading slash) as UTF-8 text, or null if the jar has no such file. */
	public static String text(String path) {
		try (InputStream in = CWGResources.class.getResourceAsStream("/" + path)) {
			if (in == null) return null;
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}
}
