package com.SaintAlfonzo.exposureextras;

import com.SaintAlfonzo.exposureextras.camerabag.CameraBag;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exposure Extras: this pack's own additions to Exposure (not upstream code), shipped in the backport jar.
 * So far: camera bags ({@code camerabag/}). Config: {@code exposureextras-common.toml}.
 *
 * The {@code exposure_extras:} ids, and the older {@code creativeengineering:<tier>_camera_bag} ids (before patcherMod
 * 1.0.3), move by the swap list {@code data/exposureextras/swaps/owner_namespace.json} (read by Creative Common); the
 * config file is carried over here.
 */
@Mod(ExposureExtras.ID)
public class ExposureExtras {

    public static final String ID = "exposureextras";

    /** The mod id before the naming-rule rename. */
    private static final String OLD_ID = "exposure_extras";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExposureExtras() {
        carryOverConfig(OLD_ID + "-common.toml", ID + "-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExtrasConfig.SPEC);
        CameraBag.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    /**
     * Keeps the player's values across the mod id rename: the old file is renamed to the new name when only the old
     * one exists (same rule as Creative Common's ConfigRename.carryOver, inlined: this jar has no Common dependency).
     */
    private static void carryOverConfig(String oldName, String newName) {
        Path dir = FMLPaths.CONFIGDIR.get();
        Path from = dir.resolve(oldName);
        Path to = dir.resolve(newName);
        if (!Files.isRegularFile(from) || Files.exists(to)) return;
        try {
            Files.move(from, to);
            LOGGER.info("[exposureextras] config {} carried over to {}", oldName, newName);
        } catch (Exception e) {
            LOGGER.error("[exposureextras] could not carry config {} over to {}: {}", oldName, newName, e.toString());
        }
    }
}
