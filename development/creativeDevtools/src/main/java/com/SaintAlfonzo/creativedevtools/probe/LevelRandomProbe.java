package com.SaintAlfonzo.creativedevtools.probe;

import com.SaintAlfonzo.creativecommon.util.LevelRandomHook;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Debug stand-in for a Level's shared random (config debug.level_random.probe). It is still
 * thread-safe, and it logs the stack of the first few distinct callers that touch it from a
 * thread other than the one that built the Level, which names the off-thread code that
 * {@code LevelThreadSafeRandomMixin} otherwise hides (fixes_compat.md F4).
 */
public final class LevelRandomProbe extends ThreadSafeLegacyRandomSource {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_CALLERS = 20;
	private static final Set<String> SEEN = ConcurrentHashMap.newKeySet();

	private final Thread owner = Thread.currentThread();

	private LevelRandomProbe() {
		super(RandomSupport.generateUniqueSeed());
	}

	/** Registers {@link #create} on patcherMod's util/LevelRandomHook; called from the CreativeDevtools constructor. */
	public static void register() {
		LevelRandomHook.register(LevelRandomProbe::create);
	}

	/** The random for a new Level: the probe when enabled, else null (the hook then uses the plain thread-safe one). */
	public static net.minecraft.util.RandomSource create() {
		boolean on;
		try {
			on = DevtoolsConfig.DEBUG.levelRandomProbe.get();
		} catch (Throwable t) {
			on = false;
		}
		return on ? new LevelRandomProbe() : null;
	}

	@Override
	public int next(int bits) {
		Thread t = Thread.currentThread();
		if (t != owner && SEEN.size() < MAX_CALLERS) {
			StackTraceElement[] st = t.getStackTrace();
			StringBuilder key = new StringBuilder();
			// frame 0 is getStackTrace, 1 is this method; the next few identify the caller
			for (int i = 2; i < Math.min(st.length, 8); i++) key.append(st[i]).append('\n');
			if (SEEN.add(t.getName().replaceAll("\\d+", "#") + "|" + key)) {
				LOGGER.warn("[LevelRandomProbe] off-thread Level.random use from thread '{}' (owner '{}'):\n{}",
						t.getName(), owner.getName(), key);
			}
		}
		return super.next(bits);
	}
}
