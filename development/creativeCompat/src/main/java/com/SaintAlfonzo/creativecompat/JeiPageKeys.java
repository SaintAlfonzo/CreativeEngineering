package com.SaintAlfonzo.creativecompat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.loading.FMLPaths;

/**
 * One-time carry-over of the four {@code [jei]} page switches ({@code seasoningCategory}, {@code canCategories},
 * {@code potionBucketCategory}, {@code tippedArrowCategory}) into {@code [jei] pagesOff} (publish_readiness.md PR-CP-11):
 * each old key set to false puts its page id in the list, then the old keys are removed. Runs at construction, before
 * the config is registered (Forge reads COMMON configs after every constructor), and only when the file still has an
 * old key and no {@code pagesOff}.
 */
final class JeiPageKeys {

	private static final Logger LOGGER = LogUtils.getLogger();

	/** Old key -> page id. */
	private static final Map<String, String> OLD_KEYS = new LinkedHashMap<>();

	static {
		OLD_KEYS.put("seasoningCategory", "seasoning");
		OLD_KEYS.put("canCategories", "cans");
		OLD_KEYS.put("potionBucketCategory", "potion_buckets");
		OLD_KEYS.put("tippedArrowCategory", "tipped_arrows");
	}

	private JeiPageKeys() {}

	static void carryOver(String fileName) {
		Path file = FMLPaths.CONFIGDIR.get().resolve(fileName);
		if (!Files.isRegularFile(file)) return;
		try (CommentedFileConfig cfg = CommentedFileConfig.builder(file, TomlFormat.instance()).build()) {
			cfg.load();
			if (!(cfg.get("jei") instanceof CommentedConfig jei) || jei.contains("pagesOff")) return;
			List<String> off = new ArrayList<>();
			boolean found = false;
			for (Map.Entry<String, String> e : OLD_KEYS.entrySet()) {
				if (!jei.contains(e.getKey())) continue;
				found = true;
				if (Boolean.FALSE.equals(jei.get(e.getKey()))) off.add(e.getValue());
				jei.remove(e.getKey());
			}
			if (!found) return;
			jei.set("pagesOff", off);
			cfg.save();
			LOGGER.info("[creativecompat] [jei] page switches carried over to pagesOff = {}", off);
		} catch (Exception e) {
			LOGGER.error("[creativecompat] could not carry the [jei] page switches over: {}", e.toString());
		}
	}
}
