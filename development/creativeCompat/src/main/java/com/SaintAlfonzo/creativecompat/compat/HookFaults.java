package com.SaintAlfonzo.creativecompat.compat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

/**
 * Faults in per-tick and per-frame hooks that must never stop the game: each hook keeps its fallback (vanilla or the
 * other mod's own answer) but logs its first fault once, with the stack trace, so a persistent fault is visible
 * (publish_readiness.md PR-CP-09).
 */
public final class HookFaults {

	private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

	private HookFaults() {}

	/** Log {@code t} the first time {@code hook} fails; later faults of the same hook are dropped. */
	public static void once(String hook, Throwable t) {
		if (SEEN.add(hook)) {
			CreativeCompat.LOGGER.error("[creativecompat] {} failed; it falls back from now on and logs no further faults", hook, t);
		}
	}
}
