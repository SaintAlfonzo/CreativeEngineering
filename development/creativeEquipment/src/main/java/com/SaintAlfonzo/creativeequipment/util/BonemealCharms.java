package com.SaintAlfonzo.creativeequipment.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * "Items that trigger the bone-meal-refund charm effect when held opposite the bone meal."
 *
 * <p>Membership is the {@code #creativeequipment:bonemeal_charms} item tag, populated in
 * KubeJS ({@code kubejs/server_scripts/tags/equipmenttags.js}) so more items can opt in without
 * rebuilding patcherMod. The single source of truth for that tag key on the Java side lives
 * here; {@code mixin.BonemealCharmMixin} reads it.
 *
 * @see com.SaintAlfonzo.creativeequipment.mixin.BonemealCharmMixin
 */
public final class BonemealCharms {

    public static final TagKey<Item> TAG =
            TagKey.create(Registry.ITEM_REGISTRY,
                    new ResourceLocation("creativeequipment", "bonemeal_charms"));

    private BonemealCharms() {}
}
