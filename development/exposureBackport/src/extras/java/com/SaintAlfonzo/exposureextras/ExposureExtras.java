package com.SaintAlfonzo.exposureextras;

import com.SaintAlfonzo.exposureextras.camerabag.CameraBag;
import com.SaintAlfonzo.exposureextras.camerabag.CameraBagTier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exposure Extras: this pack's own additions to Exposure (not upstream code), shipped in the backport jar.
 * So far: camera bags ({@code camerabag/}). Config: {@code exposureextras-common.toml}.
 *
 * <p>Mod id {@code exposure_extras} until 2026-11-10 (namespace_move.md D-EXPO, naming rule). The {@code exposure_extras:}
 * ids move by the swap list {@code data/exposureextras/swaps/owner_namespace.json} (read by Creative Common); the
 * config file is carried over here.
 */
@Mod(ExposureExtras.ID)
public class ExposureExtras {

    public static final String ID = "exposureextras";

    /** The bags were {@code creativeengineering:<tier>_camera_bag} before they moved here (patcherMod 1.0.3). */
    private static final String OLD_NAMESPACE = "creativeengineering";
    /** The mod id before the naming-rule rename (2026-11-10). */
    private static final String OLD_ID = "exposure_extras";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExposureExtras() {
        carryOverConfig(OLD_ID + "-common.toml", ID + "-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExtrasConfig.SPEC);
        CameraBag.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(ExposureExtras::remapOldIds);
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

    /**
     * Bags saved under the old creativeengineering ids (before patcherMod 1.0.3) load as the exposureextras ones,
     * contents and all. Permanent read alias; the later exposure_extras ids are in the swap list.
     */
    private static void remapOldIds(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping
                : event.getMappings(ForgeRegistries.Keys.ITEMS, OLD_NAMESPACE)) {
            for (CameraBagTier tier : CameraBagTier.values()) {
                if (mapping.getKey().equals(new ResourceLocation(OLD_NAMESPACE, tier.itemId()))) {
                    mapping.remap(CameraBag.BY_TIER.get(tier).get());
                }
            }
        }
    }
}
