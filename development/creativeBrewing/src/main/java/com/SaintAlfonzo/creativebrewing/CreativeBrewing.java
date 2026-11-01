package com.SaintAlfonzo.creativebrewing;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

/**
 * Creative Brewing (feature_writeup/creative_brewing.md): the brewing, alcohol and potion add-ons the pack owns, in one
 * jar, built like Creative Delight. Each member is its own mod, nested under META-INF/jarjar/ with its own mod id and
 * namespace (Vinery with its second id {@code foundation}, Brewin' and Chewin'); this mod id ({@code creative_brewing})
 * registers nothing yet. What CB adds is KubeJS scripts (startup scripts in {@code kubejs_startup/}, server and client
 * scripts under {@code data/} and {@code assets/creative_brewing/kubejs/}) that call the pack's helpers, {@link CBConfig},
 * and the potion and effect Java that moves here from patcherMod (creative_brewing.md 4).
 */
@Mod(CreativeBrewing.MOD_ID)
public class CreativeBrewing {

	public static final String MOD_ID = "creative_brewing";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreativeBrewing() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CBConfig.SPEC, "creative_brewing-common.toml");

		List<String> members = CBConfig.MEMBERS;
		List<String> missing = members.stream().filter(id -> !ModList.get().isLoaded(id)).toList();
		LOGGER.info("[creative_brewing] members: {}{}", members, missing.isEmpty() ? "" : " (NOT loaded: " + missing + ")");
		members.forEach(CreativeBrewing::warnIfNotNested);
	}

	/**
	 * A jar in mods/ always beats a nested one with the same mod id, whatever the versions (creative_delight.md 14.1
	 * point 4). A nested member is loaded from the root of its own jij: file system; a top-level jar from the default
	 * one, so a member whose mod file lives there is a stale standalone jar (backportedVinery-*, backportedBrewin-*)
	 * left beside CB.
	 */
	private static void warnIfNotNested(String id) {
		IModFileInfo info = ModList.get().getModFileById(id);
		if (info == null) return;
		Path path = info.getFile().getFilePath();
		if (path.getFileSystem() != FileSystems.getDefault()) return;
		LOGGER.error("[creative_brewing] member {} is loaded from {} (version {}), not from the Creative Brewing jar: "
				+ "a jar in mods/ overrides the nested copy. Remove that jar.", id, path.getFileName(),
				info.versionString());
	}
}
