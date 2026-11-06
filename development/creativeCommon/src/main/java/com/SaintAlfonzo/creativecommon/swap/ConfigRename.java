package com.SaintAlfonzo.creativecommon.swap;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Config half of the swap lists: a jar whose config file was renamed (a mod id change) calls {@link #carryOver} before
 * registering its config, so the player's values move to the new file instead of being replaced by defaults. Runs only
 * when the old file exists and the new one does not; the old file is renamed, not copied, so it cannot be read twice.
 * A jar carved out of another (its sections moved to a new file) calls {@link #carrySections} the same way.
 */
public final class ConfigRename {
	private static final Logger LOGGER = LogUtils.getLogger();

	private ConfigRename() {}

	public static void carryOver(String oldName, String newName) {
		Path dir = FMLPaths.CONFIGDIR.get();
		Path from = dir.resolve(oldName);
		Path to = dir.resolve(newName);
		if (!Files.isRegularFile(from) || Files.exists(to)) return;
		try {
			Files.move(from, to);
			LOGGER.info("[swaps] config {} carried over to {}", oldName, newName);
		} catch (Exception e) {
			LOGGER.error("[swaps] could not carry config {} over to {}: {}", oldName, newName, e.toString());
		}
	}

	/**
	 * Copies the listed top-level tables (values and comments) of {@code fromFile} into a new {@code toFile}, for a
	 * jar that took over those sections. Runs only when {@code toFile} does not exist yet and {@code fromFile} does;
	 * call it at construction, before registering the new config (Forge reads COMMON configs after every constructor).
	 * {@code fromFile} is left as it is: its own mod drops the moved sections when it corrects the file. Returns true
	 * when it wrote {@code toFile}.
	 */
	public static boolean carrySections(String fromFile, String toFile, List<String> sections) {
		Path dir = FMLPaths.CONFIGDIR.get();
		Path from = dir.resolve(fromFile);
		Path to = dir.resolve(toFile);
		if (!Files.isRegularFile(from) || Files.exists(to)) return false;
		List<String> moved = new ArrayList<>();
		try (CommentedFileConfig src = CommentedFileConfig.builder(from, TomlFormat.instance()).build()) {
			src.load();
			CommentedConfig out = TomlFormat.newConfig();
			for (String section : sections) {
				Object table = src.get(section);
				if (!(table instanceof CommentedConfig)) continue;
				out.set(section, table);
				String comment = src.getComment(section);
				if (comment != null) out.setComment(section, comment);
				moved.add(section);
			}
			if (moved.isEmpty()) return false;
			try (CommentedFileConfig dst = CommentedFileConfig.builder(to, TomlFormat.instance()).build()) {
				dst.putAll(out);
				dst.putAllComments(out);
				dst.save();
			}
			LOGGER.info("[swaps] config sections {} carried over from {} to {}", moved, fromFile, toFile);
			return true;
		} catch (Exception e) {
			LOGGER.error("[swaps] could not carry config sections {} from {} to {}: {}", sections, fromFile, toFile, e.toString());
			return false;
		}
	}

	/**
	 * Like {@link #carrySections}, for a jar whose own config file already exists (it took sections over after its
	 * first release): each listed top-level table of {@code fromFile} that {@code toFile} does not have yet is copied in
	 * with its values and comments. {@code toFile} must exist; a missing one is left to {@link #carrySections}. Call it
	 * at construction, before registering the config. {@code fromFile} is left as it is. Returns the sections copied.
	 */
	public static List<String> mergeSections(String fromFile, String toFile, List<String> sections) {
		Path dir = FMLPaths.CONFIGDIR.get();
		Path from = dir.resolve(fromFile);
		Path to = dir.resolve(toFile);
		if (!Files.isRegularFile(from) || !Files.isRegularFile(to)) return List.of();
		List<String> moved = new ArrayList<>();
		try (CommentedFileConfig src = CommentedFileConfig.builder(from, TomlFormat.instance()).build();
			 CommentedFileConfig dst = CommentedFileConfig.builder(to, TomlFormat.instance()).build()) {
			src.load();
			dst.load();
			for (String section : sections) {
				Object table = src.get(section);
				if (!(table instanceof CommentedConfig) || dst.contains(section)) continue;
				dst.set(section, table);
				String comment = src.getComment(section);
				if (comment != null) dst.setComment(section, comment);
				moved.add(section);
			}
			if (moved.isEmpty()) return List.of();
			dst.save();
			LOGGER.info("[swaps] config sections {} merged from {} into {}", moved, fromFile, toFile);
			return moved;
		} catch (Exception e) {
			LOGGER.error("[swaps] could not merge config sections {} from {} into {}: {}", sections, fromFile, toFile, e.toString());
			return List.of();
		}
	}
}
