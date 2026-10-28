package com.SaintAlfonzo.creativedelight;

import com.mojang.logging.LogUtils;
import com.SaintAlfonzo.creativedelight.farm.VillageFarmCrops;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.slf4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

/**
 * Creative Delight (feature_writeup/creative_delight.md): the Delight add-ons the pack owns, in one jar. Each member is
 * its own mod, nested under META-INF/jarjar/ with its own mod id and namespace; this mod id ({@code creative_delight})
 * holds no registry content. What CD adds is KubeJS scripts (startup scripts in {@code kubejs_startup/}, server and
 * client scripts under {@code data/} and {@code assets/creative_delight/kubejs/}) that call the pack's helpers, and
 * {@link CDConfig}.
 */
@Mod(CreativeDelight.MOD_ID)
public class CreativeDelight {

	public static final String MOD_ID = "creative_delight";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CreativeDelight() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CDConfig.SPEC, "creative_delight-common.toml");
		VillageFarmCrops.PROCESSORS.register(FMLJavaModLoadingContext.get().getModEventBus());
		MinecraftForge.EVENT_BUS.addListener(VillageFarmCrops::onServerAboutToStart);

		List<String> members = CDConfig.MEMBERS;
		List<String> missing = members.stream().filter(id -> !ModList.get().isLoaded(id)).toList();
		LOGGER.info("[creative_delight] members: {}{}", members, missing.isEmpty() ? "" : " (NOT loaded: " + missing + ")");
		members.forEach(CreativeDelight::warnIfNotNested);
	}

	/**
	 * A jar in mods/ always beats a nested one with the same mod id, whatever the versions (creative_delight.md 14.1
	 * point 4, SQ-CDJ-02). A nested member is loaded from the root of its own jij: file system; a top-level jar from
	 * the default one, so a member whose mod file lives there is a stale upstream jar left beside CD.
	 */
	private static void warnIfNotNested(String id) {
		IModFileInfo info = ModList.get().getModFileById(id);
		if (info == null) return;
		Path path = info.getFile().getFilePath();
		if (path.getFileSystem() != FileSystems.getDefault()) return;
		LOGGER.error("[creative_delight] member {} is loaded from {} (version {}), not from the Creative Delight jar: "
				+ "a jar in mods/ overrides the nested copy. Remove that jar.", id, path.getFileName(),
				info.versionString());
	}
}
