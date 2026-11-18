package com.SaintAlfonzo.creativecommon.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Reloads one mod config the way Forge's own file watcher does (ConfigFileTypeHandler$ConfigWatcher, Forge 43.3.9):
 * re-read the file, back it up and correct it if a value is out of spec (then save), reset the spec's value caches and
 * dispatch {@code ModConfigEvent.Reloading} to the owning mod. Generalizes Creative Compat's
 * {@code CompatConfigReload} (plan_post_scratch_1115b.md E2) to every mod; used by {@link OwnConfigWatcher}, open to
 * commands ({@link #reloadMod}).
 */
public final class ConfigReload {

	private ConfigReload() {}

	private static final Logger LOGGER = LogUtils.getLogger();

	/** Reload one config file now. Returns a one-line result. Thread: any (Forge's watcher also runs off-thread). */
	public static synchronized String reload(ModConfig config) {
		String name = config.getFileName();
		if (!(config.getConfigData() instanceof CommentedFileConfig file)) return name + " is not loaded from a file";
		IConfigSpec<?> spec = config.getSpec();
		if (spec.isCorrecting()) return name + " is being corrected, skipped";
		try {
			file.load();
			int fixed = 0;
			if (!spec.isCorrect(file)) {
				ConfigFileTypeHandler.backUpConfig(file);
				fixed = spec.correct(file);
				file.save();
			}
			spec.afterReload();
			// After a correction save, the file holds what was applied: the watcher's guard skips the save's own event.
			OwnConfigWatcher.noteApplied(config);
			ModList.get().getModContainerById(config.getModId())
					.ifPresent(c -> c.dispatchConfigEvent(IConfigEvent.reloading(config)));
			String msg = name + " reloaded" + (fixed > 0 ? " (" + fixed + " value(s) corrected)" : "");
			LOGGER.info("[creativecommon] {}", msg);
			return msg;
		} catch (Exception e) {
			LOGGER.error("[creativecommon] reloading {} failed, old values kept", name, e);
			return name + " reload failed: " + e;
		}
	}

	/** Reload every config file of one mod that lives under config/ (not world server configs). */
	public static List<String> reloadMod(String modId) {
		List<String> out = new ArrayList<>();
		for (ModConfig c : ConfigTracker.INSTANCE.fileMap().values()) {
			if (modId.equals(c.getModId()) && OwnConfigWatcher.inConfigDir(c) != null) out.add(reload(c));
		}
		if (out.isEmpty()) out.add("no config file of " + modId + " in config/");
		return out;
	}

	/** Path of a loaded file config, or null. */
	static Path pathOf(ModConfig c) {
		return c.getConfigData() instanceof CommentedFileConfig f ? f.getNioPath() : null;
	}
}
