package com.SaintAlfonzo.exposure_extras;

import com.SaintAlfonzo.exposure_extras.camerabag.CameraBag;
import com.SaintAlfonzo.exposure_extras.camerabag.CameraBagTier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/**
 * Exposure Extras: this pack's own additions to Exposure (not upstream code), shipped in the backport jar.
 * So far: camera bags ({@code camerabag/}). Config: {@code exposure_extras-common.toml}.
 */
@Mod(ExposureExtras.ID)
public class ExposureExtras {

    public static final String ID = "exposure_extras";

    /** The bags were {@code creativeengineering:<tier>_camera_bag} before they moved here (patcherMod 1.0.3). */
    private static final String OLD_NAMESPACE = "creativeengineering";

    public ExposureExtras() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExtrasConfig.SPEC);
        CameraBag.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(ExposureExtras::remapOldIds);
    }

    /** Bags saved under the old creativeengineering ids load as the exposure_extras ones, contents and all. */
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
