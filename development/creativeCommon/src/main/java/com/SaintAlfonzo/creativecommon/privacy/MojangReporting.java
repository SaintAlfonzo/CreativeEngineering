// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.privacy;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The client switch {@code [privacy] blockMojangReporting} of {@code creativecommon-client.toml}
 * (feature_writeup/privacy.md 4.2, TODO privacy #2). When on, the game talks to Mojang's user API through authlib's
 * offline service ({@code mixin/MojangUserApiOfflineMixin}), never holds a chat signing key
 * ({@code mixin/ProfileKeyNoSigningMixin}), and hides the markers vanilla adds for unsigned chat
 * ({@code mixin/ChatNotSecureTagMixin}, {@code mixin/UnsecureServerToastMixin}).
 *
 * <p>Read straight from the file, once: the user API service is built in {@code Minecraft}'s constructor, before Forge
 * loads mod configs, so {@link com.SaintAlfonzo.creativecommon.CommonClientConfig} has no value yet. The spec entry
 * there writes the key and its comment into the file; a change applies on the next game start.
 */
public final class MojangReporting {

	private static final Logger LOGGER = LogUtils.getLogger();
	static final String FILE = "creativecommon-client.toml";
	static final String TABLE = "privacy";
	static final String KEY = "blockMojangReporting";

	private static Boolean blocked;

	private MojangReporting() {}

	/** True unless the player's client config sets the switch to false. */
	public static boolean blocked() {
		if (blocked == null) {
			blocked = read(FMLPaths.CONFIGDIR.get().resolve(FILE));
		}
		return blocked;
	}

	static boolean read(Path file) {
		if (!Files.isRegularFile(file)) {
			return true;
		}
		try {
			return parse(Files.readAllLines(file));
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Could not read {} ({}); Mojang reporting stays blocked", FILE, e.toString());
			return true;
		}
	}

	/** The value of {@code KEY} in table {@code [TABLE]}; true when absent or not a boolean. */
	static boolean parse(List<String> lines) {
		String table = "";
		for (String raw : lines) {
			String line = raw.strip();
			if (line.startsWith("[")) {
				table = line.replaceAll("^\\[+|\\]+$", "").strip();
				continue;
			}
			if (!TABLE.equals(table) || !line.startsWith(KEY)) {
				continue;
			}
			String rest = line.substring(KEY.length()).strip();
			if (!rest.startsWith("=")) {
				continue;
			}
			String value = rest.substring(1).split("#", 2)[0].strip();
			if (value.equals("false")) {
				return false;
			}
			if (value.equals("true")) {
				return true;
			}
		}
		return true;
	}

	/** One startup line saying which way the switch is set (no player data). */
	public static void logState() {
		if (blocked()) {
			LOGGER.info("Mojang user API offline ([{}] {} = true): no chat signing key, no abuse reports, no telemetry",
					TABLE, KEY);
		} else {
			LOGGER.info("Mojang user API online ([{}] {} = false): vanilla chat signing and reporting", TABLE, KEY);
		}
	}
}
