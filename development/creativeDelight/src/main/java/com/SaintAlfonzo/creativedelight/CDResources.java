package com.SaintAlfonzo.creativedelight;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Text files of the Creative Delight jar for its KubeJS scripts. KubeJS's {@code JsonIO} only reads files under the
 * game folder, so a script that ships a JSON next to it (the wild crop rules, creative_delight.md 24) reads the pack
 * folder copy first (dev) and falls back to the jar copy through {@link #text}.
 */
public final class CDResources {

	private CDResources() {}

	/** The jar resource at {@code path} (no leading slash) as UTF-8 text, or null if the jar has no such file. */
	public static String text(String path) {
		try (InputStream in = CDResources.class.getResourceAsStream("/" + path)) {
			if (in == null) return null;
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}
}
