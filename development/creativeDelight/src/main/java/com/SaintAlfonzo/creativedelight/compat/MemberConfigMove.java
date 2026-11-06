package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativedelight.CDConfig;
import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Carries a member's old config file into Creative Delight's (plan_creative_delight.md A6, R3): the ports registered
 * their own file before they were nested (displaydelight-common.toml, vintagedelight-common.toml, salt-common.toml); their settings are
 * now keys of creativedelight-common.toml ({@link CDConfig#MEMBER_FILES}). At construction, before Forge reads the COMMON
 * configs: each old value is written to its new key (inverted where the map says so) unless CD's file already has that
 * key, then the old file is renamed to {@code <file>.moved}, so it is read once and never again. The same job as
 * Common's {@code ConfigRename.mergeSections}, with renamed keys.
 */
public final class MemberConfigMove {
	private MemberConfigMove() {}

	public static void run() {
		Path dir = FMLPaths.CONFIGDIR.get();
		Path to = dir.resolve(CDConfig.FILE);
		for (Map.Entry<String, Map<String, String>> file : CDConfig.MEMBER_FILES.entrySet()) {
			Path from = dir.resolve(file.getKey());
			if (!Files.isRegularFile(from)) continue;
			List<String> moved = new ArrayList<>();
			try (CommentedFileConfig src = CommentedFileConfig.builder(from, TomlFormat.instance()).build();
				 CommentedFileConfig dst = CommentedFileConfig.builder(to, TomlFormat.instance()).build()) {
				src.load();
				if (Files.isRegularFile(to)) dst.load();
				for (Map.Entry<String, String> key : file.getValue().entrySet()) {
					Object v = src.get(key.getKey());
					boolean invert = key.getValue().startsWith("!");
					String target = invert ? key.getValue().substring(1) : key.getValue();
					if (v == null || dst.contains(target)) continue;
					if (invert) {
						if (!(v instanceof Boolean b)) continue;
						dst.set(target, !b);
					} else {
						dst.set(target, v);   // any type: Salt's numbers and food list as well as switches
					}
					moved.add(key.getKey() + " -> " + target);
				}
				if (!moved.isEmpty()) dst.save();
			} catch (Exception e) {
				CreativeDelight.LOGGER.error("[creativedelight] could not carry config/{} into config/{}: {}", file.getKey(),
						CDConfig.FILE, e.toString());
				continue;
			}
			try {
				Files.move(from, dir.resolve(file.getKey() + ".moved"), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				CreativeDelight.LOGGER.error("[creativedelight] could not rename config/{}: {}", file.getKey(), e.toString());
			}
			CreativeDelight.LOGGER.info("[creativedelight] config/{} carried into config/{} ({} values) and renamed to {}.moved",
					file.getKey(), CDConfig.FILE, moved.size(), file.getKey());
		}
	}

	/**
	 * M8 (plan_cb_publish.md 3b): the {@code [members] blockheads_additions} and {@code incubation_additions} switches became
	 * {@code [compartments] cookingforblockheads} and {@code incubation}. Copies a player's value across once, before Forge
	 * corrects the file and drops the old keys.
	 */
	public static void carryCompartmentSwitches() {
		Path to = FMLPaths.CONFIGDIR.get().resolve(CDConfig.FILE);
		if (!Files.isRegularFile(to)) return;
		try (CommentedFileConfig cfg = CommentedFileConfig.builder(to, TomlFormat.instance()).build()) {
			cfg.load();
			boolean changed = false;
			for (Map.Entry<String, String> e : Compartments.OLD_MEMBER_SWITCH.entrySet()) {
				Object v = cfg.get("members." + e.getValue());
				if (!(v instanceof Boolean) || cfg.contains("compartments." + e.getKey())) continue;
				cfg.set("compartments." + e.getKey(), v);
				changed = true;
				CreativeDelight.LOGGER.info("[creativedelight] [members] {} carried to [compartments] {} ({})", e.getValue(), e.getKey(), v);
			}
			if (changed) cfg.save();
		} catch (Exception e) {
			CreativeDelight.LOGGER.error("[creativedelight] could not carry the [members] switches to [compartments]: {}", e.toString());
		}
	}
}
