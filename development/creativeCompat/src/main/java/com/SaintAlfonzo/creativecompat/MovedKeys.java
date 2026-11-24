// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativecommon.swap.ConfigRename;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.loading.FMLPaths;

/**
 * One-time carry-over of the config keys that moved into this jar in the wave 2 carve (plan_wave2_publish.md W2-8,
 * W2-3), so a player's values survive the update. Runs at construction, before this jar registers its config (Forge
 * reads the configs after every constructor):
 * <ul>
 * <li>{@code [melting]} (endNoFire included) from {@code creativecommon-common.toml}: copied whole when this jar's file
 * has no {@code [melting]} yet ({@link ConfigRename#carrySections} for a new file, {@link ConfigRename#mergeSections} for
 * an existing one). Creative Common then drops the section from its own file when Forge corrects it. When nothing was
 * copied (this file already had the section) and Common's file still has one, a warning names it, since Forge is about
 * to drop it there.</li>
 * <li>Raspberry Pie's {@code [effects] fleetFootedFov} from {@code solraspberrypie-client.toml} into
 * {@code [fleet_footed] fov} here, when this file has no {@code [fleet_footed]} yet. Raspberry Pie drops its key the
 * same way.</li>
 * </ul>
 */
final class MovedKeys {

	private static final Logger LOGGER = LogUtils.getLogger();

	static final String COMMON_FILE = "creativecommon-common.toml";
	static final List<String> COMMON_SECTIONS = List.of("melting");
	static final String RP_FILE = "solraspberrypie-client.toml";

	private MovedKeys() {}

	static void carryOver(String file) {
		boolean carried = ConfigRename.carrySections(COMMON_FILE, file, COMMON_SECTIONS)
				|| !ConfigRename.mergeSections(COMMON_FILE, file, COMMON_SECTIONS).isEmpty();
		if (!carried) warnLeftover(file);
		carryFleetFooted(file);
	}

	/** Common's file still holding [melting] that was not copied: Forge drops it there on its next correction. */
	private static void warnLeftover(String file) {
		Path old = FMLPaths.CONFIGDIR.get().resolve(COMMON_FILE);
		if (!Files.isRegularFile(old)) return;
		try (CommentedFileConfig src = CommentedFileConfig.builder(old, TomlFormat.instance()).build()) {
			src.load();
			List<String> left = COMMON_SECTIONS.stream().filter(s -> src.get(s) instanceof CommentedConfig).toList();
			if (left.isEmpty()) return;
			LOGGER.warn("[creativecompat] config/{} still has sections that moved to config/{}: {}. "
					+ "Creative Compat reads only its own file; copy any edited value over (Creative Common drops these from its file).",
					COMMON_FILE, file, left);
		} catch (Exception e) {
			LOGGER.error("[creativecompat] could not read config/{}: {}", COMMON_FILE, e.toString());
		}
	}

	private static void carryFleetFooted(String file) {
		Path dir = FMLPaths.CONFIGDIR.get();
		Path from = dir.resolve(RP_FILE);
		Path to = dir.resolve(file);
		if (!Files.isRegularFile(from)) return;
		try (CommentedFileConfig src = CommentedFileConfig.builder(from, TomlFormat.instance()).build()) {
			src.load();
			Object value = src.get(List.of("effects", "fleetFootedFov"));
			if (!(value instanceof Number number)) return;
			try (CommentedFileConfig dst = CommentedFileConfig.builder(to, TomlFormat.instance()).build()) {
				if (Files.isRegularFile(to)) dst.load();
				if (dst.contains("fleet_footed")) {
					LOGGER.warn("[creativecompat] config/{} still has [effects] fleetFootedFov = {}; config/{} [fleet_footed] fov is "
							+ "the one read now (Raspberry Pie drops its key)", RP_FILE, number, file);
					return;
				}
				dst.set(List.of("fleet_footed", "fov"), number.doubleValue());
				dst.save();
			}
			LOGGER.info("[swaps] config [effects] fleetFootedFov = {} carried over from {} to {} [fleet_footed] fov", number, RP_FILE, file);
		} catch (Exception e) {
			LOGGER.error("[creativecompat] could not carry fleetFootedFov over from config/{}: {}", RP_FILE, e.toString());
		}
	}
}
