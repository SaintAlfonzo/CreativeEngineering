package com.SaintAlfonzo.creativeequipment.util;

import com.SaintAlfonzo.creativeequipment.CEQConfig;

/**
 * Parses {@link CEQConfig.ArmorEnchant#overrides} for
 * {@code mixin/ArmorMaterialEnchantabilityMixin} - a pure read-through over the config list
 * (same idiom as {@code forgivingvoid/ForgivingVoidBridge}), so entries are live-reloadable
 * without a restart.
 */
public final class ArmorEnchantabilityBridge {

    private ArmorEnchantabilityBridge() {}

    /** {@code ArmorMaterial.getName()} -> configured enchantment value, or {@code null} if unmapped. */
    public static Integer valueFor(String materialName) {
        if (materialName == null) return null;
        for (String entry : CEQConfig.ARMOR_ENCHANT.overrides.get()) {
            if (entry == null) continue;
            int sep = entry.indexOf(':');
            if (sep <= 0) continue;
            if (!entry.substring(0, sep).trim().equals(materialName)) continue;
            try {
                return Integer.parseInt(entry.substring(sep + 1).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
