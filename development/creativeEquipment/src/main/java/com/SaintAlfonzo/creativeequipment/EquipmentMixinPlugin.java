package com.SaintAlfonzo.creativeequipment;

import com.SaintAlfonzo.creativeequipment.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativeequipment.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies only when its mod is loaded (each one targets
 * a class of that mod). Every other mixin targets vanilla and always applies, as it did in patcherMod. The mixins moved
 * from patcherMod on 2026-11-04 unchanged apart from their package (SRG names, remap = false, creativeengineering$
 * member prefixes).
 */
public class EquipmentMixinPlugin extends GatedMixinPlugin {

    private static final List<String> BACKPACKS = List.of("sophisticatedbackpacks");
    private static final List<String> EASY_ELYTRA = List.of("easyelytratakeoff");

    private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
            // Copper tool tier on Everything Copper's pickaxe (CEQTiers)
            Map.entry("CopperPickaxeItemMixin", List.of("everythingcopper")),
            // Backpack skins (backpackskin/)
            Map.entry("BackpackSkinItemRendererMixin", BACKPACKS),
            Map.entry("BackpackSkinBakedModelMixin", BACKPACKS),
            Map.entry("BackpackSkinBlockEntityMixin", BACKPACKS),
            Map.entry("BackpackSkinBlockShapeMixin", BACKPACKS),
            Map.entry("BackpackSkinWornMixin", BACKPACKS),
            Map.entry("BackpackSkinEquipMixin", BACKPACKS),
            // Elytra chestplates take off with Easy Elytra Takeoff (util/ElytraChestplates)
            Map.entry("EasyElytraTakeoffHelperMixin", EASY_ELYTRA),
            Map.entry("EasyElytraTakeoffGroundCheckMixin", EASY_ELYTRA),
            // From Creative Brewing (plan_cb_polish.md 1): copper poisoning, the echo armor set bonus
            Map.entry("CopperPoisoningEffectMixin", List.of("everythingcopper")),
            Map.entry("EchoArmorEffectProcedureMixin", List.of("create_deep_dark")),
            Map.entry("EchoArmorTooltipMixin", List.of("create_deep_dark")),
            // Detail Armor Bar overflow row, from Creative Common (2026-11-07)
            Map.entry("ArmorBarRendererAccessor", List.of("detailab")),
            Map.entry("ArmorBarRendererOverflowTickMixin", List.of("detailab")));

    /** Tested version ranges (Detail Armor Bar, moved from Creative Common's CommonMixinPlugin 2026-11-07). */
    private static final Map<String, String> MOD_RANGE = Map.ofEntries(
            Map.entry("detailab", "[2.6.4,2.7)"));

    public EquipmentMixinPlugin() {
        super("[creativeequipment]", NEEDS_MOD, MOD_RANGE);
    }
}
