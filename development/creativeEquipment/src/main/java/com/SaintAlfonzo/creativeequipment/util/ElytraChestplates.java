package com.SaintAlfonzo.creativeequipment.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * "Chestplates that glide but aren't a vanilla {@link net.minecraft.world.item.ElytraItem}."
 *
 * <p>Membership is the {@code #creativeequipment:elytra_chestplates} item tag, populated in
 * KubeJS ({@code kubejs/server_scripts/tags/equipmenttags.js}) so more modded elytra-chestplates
 * can be added without rebuilding patcherMod. The single source of truth for that tag key on the
 * Java side lives here; the Easy Elytra Takeoff mixins both read it.
 *
 * @see com.SaintAlfonzo.creativeequipment.mixin.EasyElytraTakeoffHelperMixin
 * @see com.SaintAlfonzo.creativeequipment.mixin.EasyElytraTakeoffGroundCheckMixin
 */
public final class ElytraChestplates {

    public static final TagKey<Item> TAG =
            TagKey.create(Registry.ITEM_REGISTRY,
                    new ResourceLocation("creativeequipment", "elytra_chestplates"));

    private ElytraChestplates() {}

    /** True if the player has a {@link #TAG}ged chestplate in any armour slot. */
    public static boolean isWorn(Player player) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && stack.is(TAG)) {
                return true;
            }
        }
        return false;
    }
}
