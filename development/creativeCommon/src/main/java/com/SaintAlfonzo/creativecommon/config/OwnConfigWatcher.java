package com.SaintAlfonzo.creativecommon.config;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Common's own watcher on config/ for our mods' config files (plan_post_scratch_1115.md G11, cause in
 * plan_post_scratch_1115b.md E3). Forge's watcher (night-config 3.6.x FileWatcher) registers only ENTRY_MODIFY and skips
 * events with a count over 1; Linux folds truncate + write into count 2, and a rename save raises no MODIFY, so a hand
 * save of a config almost never reloads. This watcher registers MODIFY and CREATE, waits for 1 s of quiet per file
 * ({@link OwnConfigLogic.Debounce}) and runs {@link ConfigReload#reload}.
 *
 * <p>No double reloads: at start it removes night-config's watch on each file it takes over, so only this watcher
 * reloads our files. Loop guard: {@link OwnConfigLogic.ContentGuard} keeps the content last applied (at start and after
 * every reload, correction save included); a due file with that content is skipped. Third-party configs and world
 * server configs keep Forge's watcher. Off switch: JVM argument {@code -Dcreativecommon.configWatcher=false}.
 */
public final class OwnConfigWatcher {

	private OwnConfigWatcher() {}

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Map<Path, ModConfig> OWNED = new ConcurrentHashMap<>();
	private static final OwnConfigLogic.ContentGuard GUARD = new OwnConfigLogic.ContentGuard();
	private static final OwnConfigLogic.Debounce DEBOUNCE = new OwnConfigLogic.Debounce(OwnConfigLogic.DEBOUNCE_MS);
	private static volatile boolean started;

	/** Mod bus, FMLLoadCompleteEvent: every COMMON and CLIENT config is loaded by now. */
	public static void onLoadComplete(FMLLoadCompleteEvent event) {
		start();
	}

	static synchronized void start() {
		if (started) return;
		started = true;
		if (!Boolean.parseBoolean(System.getProperty("creativecommon.configWatcher", "true"))) {
			LOGGER.info("[creativecommon] config watcher off (-Dcreativecommon.configWatcher=false), Forge's watcher kept");
			return;
		}
		Set<Path> dirs = new HashSet<>();
		for (ModConfig c : ConfigTracker.INSTANCE.fileMap().values()) {
			if (!OwnConfigLogic.isOwn(c.getModId())) continue;
			Path p = inConfigDir(c);
			if (p == null) continue;
			OWNED.put(p, c);
			dirs.add(p.getParent());
			FileWatcher.defaultInstance().removeWatch(ConfigReload.pathOf(c));
			noteApplied(c);
		}
		if (OWNED.isEmpty()) return;
		WatchService ws;
		try {
			ws = FileSystems.getDefault().newWatchService();
			for (Path d : dirs) d.register(ws, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
		} catch (IOException e) {
			LOGGER.error("[creativecommon] config watcher could not start; own configs reload only on restart", e);
			return;
		}
		Thread t = new Thread(() -> loop(ws), "creativecommon-config-watcher");
		t.setDaemon(true);
		t.setContextClassLoader(OwnConfigWatcher.class.getClassLoader());
		t.start();
		LOGGER.info("[creativecommon] config watcher on: {} own config file(s) in {} folder(s), debounce {} ms",
				OWNED.size(), dirs.size(), OwnConfigLogic.DEBOUNCE_MS);
	}

	/** Normalized absolute path of a file config under config/, or null (not loaded, or a world server config). */
	static Path inConfigDir(ModConfig c) {
		if (c.getType() == ModConfig.Type.SERVER) return null;
		Path p = ConfigReload.pathOf(c);
		if (p == null) return null;
		p = p.toAbsolutePath().normalize();
		Path base = FMLPaths.CONFIGDIR.get().toAbsolutePath().normalize();
		return p.startsWith(base) ? p : null;
	}

	/** Record the file's current content as applied (start, after a reload). */
	static void noteApplied(ModConfig c) {
		Path p = inConfigDir(c);
		if (p == null) return;
		try {
			GUARD.applied(p, OwnConfigLogic.hash(Files.readAllBytes(p)));
		} catch (IOException ignored) {
			// file gone between save steps: the next event records it
		}
	}

	private static void loop(WatchService ws) {
		while (true) {
			try {
				long wait = DEBOUNCE.nextDueInMs(System.currentTimeMillis());
				WatchKey key = ws.poll(wait < 0 ? 60_000L : Math.max(10L, wait), TimeUnit.MILLISECONDS);
				long now = System.currentTimeMillis();
				if (key != null) {
					Path dir = (Path) key.watchable();
					for (WatchEvent<?> ev : key.pollEvents()) {
						if (ev.kind() == StandardWatchEventKinds.OVERFLOW) {
							for (Path p : OWNED.keySet()) if (p.getParent().equals(dir)) DEBOUNCE.event(p, now);
							continue;
						}
						Path p = dir.resolve((Path) ev.context()).toAbsolutePath().normalize();
						if (OWNED.containsKey(p)) DEBOUNCE.event(p, now);
					}
					key.reset();
				}
				for (Path p : DEBOUNCE.takeDue(now)) fire(p);
			} catch (InterruptedException | ClosedWatchServiceException e) {
				return;
			} catch (Throwable e) {
				LOGGER.error("[creativecommon] config watcher pass failed", e);
			}
		}
	}

	private static void fire(Path p) {
		ModConfig c = OWNED.get(p);
		if (c == null) return;
		long h;
		try {
			h = OwnConfigLogic.hash(Files.readAllBytes(p));
		} catch (NoSuchFileException e) {
			return; // deleted or mid-rename; the CREATE that follows queues it again
		} catch (IOException e) {
			LOGGER.warn("[creativecommon] config watcher could not read {}: {}", p.getFileName(), e.toString());
			return;
		}
		if (!GUARD.changed(p, h)) {
			LOGGER.debug("[creativecommon] {} unchanged since last applied, no reload", p.getFileName());
			return;
		}
		ConfigReload.reload(c);
	}
}
