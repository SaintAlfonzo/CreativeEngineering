package com.SaintAlfonzo.creativebrewing.compat;

import com.SaintAlfonzo.creativebrewing.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativebrewing.mixins.json, the same compartment rule as Creative Delight's CDMixinPlugin
 * (creative_delight.md 12): a mixin in {@code mixin.<modid>.} applies only when that mod is loaded (and, for a mod in
 * {@link #FOR_VERSION}, only at that version), so CB boots the same without it. Vanilla targets go in
 * {@code mixin.minecraft.} (always loaded). The gate is the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). Potion and effect mixins moved from patcherMod land here (creative_brewing.md 4.2).
 */
public class CBMixinPlugin extends GatedMixinPlugin {

	private static final String COMPARTMENT_PREFIX = "com.SaintAlfonzo.creativebrewing.mixin.";

	/** Mod id -> the only version its compartment applies to (fix mixins written against one jar). */
	private static final Map<String, String> FOR_VERSION = Map.of();

	/**
	 * Create mixins that reference Potion Bundles classes: they apply only when Potion Bundles is loaded (a table
	 * entry replaces the compartment check for that mixin; Potion Bundles itself needs Create's potion classes).
	 */
	private static final Map<String, List<String>> NEEDS_MOD = Map.of(
			"PotionFluidHandlerMixin", List.of("potionbundles"),
			"ItemDrainCategoryMixin", List.of("potionbundles"),
			"SpoutCategoryMixin", List.of("potionbundles"));

	public CBMixinPlugin() {
		super("[creativebrewing]", COMPARTMENT_PREFIX, FOR_VERSION, NEEDS_MOD);
	}
}
