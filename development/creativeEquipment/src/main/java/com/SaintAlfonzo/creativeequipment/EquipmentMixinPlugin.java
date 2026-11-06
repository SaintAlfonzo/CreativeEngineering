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
            // Copper tool tier on Everything Copper's pickaxe (CETiers)
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
            Map.entry("EasyElytraTakeoffGroundCheckMixin", EASY_ELYTRA));

    public EquipmentMixinPlugin() {
        super("[creativeequipment]", NEEDS_MOD);
    }
}
