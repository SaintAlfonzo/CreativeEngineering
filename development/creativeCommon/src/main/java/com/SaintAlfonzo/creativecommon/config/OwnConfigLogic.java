package com.SaintAlfonzo.creativecommon.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Forge-free rules of Common's config watcher ({@link OwnConfigWatcher}, plan_post_scratch_1115.md G11, 1115b E3):
 * which mods count as ours, the 1 s debounce, and the content guard that keeps a reload from running twice for one
 * content (our own correction save, a touch without a change). Kept free of Forge and Minecraft classes so
 * tools/test_config_watcher.sh can test it with plain javac.
 */
public final class OwnConfigLogic {

	private OwnConfigLogic() {}

	/** Quiet time after the last event on a file before it is reloaded. */
	public static final long DEBOUNCE_MS = 1000L;

	/**
	 * Mod ids whose config files the watcher owns: the in-tree jars of development/ (the Creative jars, Sol: Raspberry
	 * Pie) and the in-tree ports and bundle members that keep their own config file in config/ (assumed list).
	 * Third-party mods keep Forge's own watcher.
	 */
	public static final Set<String> OWN_MOD_IDS = Set.of(
			// Creative jars
			"creativecommon", "creativecompat", "creativedelight", "creativebrewing", "creativeequipment",
			"creativeengineering", "creativemagic", "creativelabels", "creativeworldgen", "creativecalendar",
			"creativedevtools", "creativeloot",
			// other own jars
			"solraspberrypie", "exposure", "powergrid", "tempad",
			// bundle members with their own config file (Creative Delight, Creative Brewing, Creative Worldgen)
			"farmersdelight", "hearthandharvest", "miners_delight", "rusticdelight", "roll_delight", "delightful",
			"salt", "vinery", "brewinandchewin", "babymobs", "moremooblooms");

	public static boolean isOwn(String modId) {
		return modId != null && OWN_MOD_IDS.contains(modId);
	}

	/** Content fingerprint: CRC32 in the low half, length in the high half. */
	public static long hash(byte[] bytes) {
		CRC32 crc = new CRC32();
		crc.update(bytes, 0, bytes.length);
		return ((long) bytes.length << 32) | crc.getValue();
	}

	/** Per-file debounce: each event restarts the file's quiet time; a file is due once it has been quiet long enough. */
	public static final class Debounce {
		private final long quietMs;
		private final Map<Path, Long> last = new HashMap<>();

		public Debounce(long quietMs) {
			this.quietMs = quietMs;
		}

		public synchronized void event(Path file, long nowMs) {
			last.put(file, nowMs);
		}

		/** Removes and returns the files that have been quiet for the debounce time. */
		public synchronized List<Path> takeDue(long nowMs) {
			List<Path> due = new ArrayList<>();
			for (Iterator<Map.Entry<Path, Long>> it = last.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Path, Long> e = it.next();
				if (nowMs - e.getValue() >= quietMs) {
					due.add(e.getKey());
					it.remove();
				}
			}
			return due;
		}

		/** Milliseconds until the next file is due (0 when one already is), or -1 when nothing is pending. */
		public synchronized long nextDueInMs(long nowMs) {
			long best = -1;
			for (long t : last.values()) {
				long in = Math.max(0, t + quietMs - nowMs);
				if (best < 0 || in < best) best = in;
			}
			return best;
		}

		public synchronized int pending() {
			return last.size();
		}
	}

	/**
	 * Content guard: remembers the content each file had when it was last applied (load, our reload, our correction
	 * save). A due file whose content is unchanged is skipped, so our own save does not loop and a touch does nothing.
	 */
	public static final class ContentGuard {
		private final Map<Path, Long> applied = new HashMap<>();

		public synchronized void applied(Path file, long hash) {
			applied.put(file, hash);
		}

		public synchronized boolean changed(Path file, long hash) {
			Long a = applied.get(file);
			return a == null || a != hash;
		}
	}
}
