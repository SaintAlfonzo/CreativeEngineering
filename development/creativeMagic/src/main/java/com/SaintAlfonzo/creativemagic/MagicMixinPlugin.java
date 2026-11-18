package com.SaintAlfonzo.creativemagic;

import com.SaintAlfonzo.creativemagic.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativemagic.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). Every Magic mixin targets a class of one mod and applies only when that mod is
 * loaded.
 */
public class MagicMixinPlugin extends GatedMixinPlugin {

	private static final List<String> NEA = List.of("nock_enough_arrows");
	private static final List<String> BOTANIA = List.of("botania");
	private static final List<String> PNC = List.of("pneumaticcraft");
	private static final List<String> SOPHISTICATED = List.of("sophisticatedcore");

	private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
			// Nock Enough Arrows' Cycle, Stockpile and Recovery: not on tables, books or trades
			Map.entry("CycleEnchantmentMixin", NEA),
			Map.entry("StockpileEnchantmentMixin", NEA),
			Map.entry("RecoveryEnchantmentMixin", NEA),
			// Botania Trinket Case holds any item ([botania] trinketCaseAnyItem, compat/TrinketCaseRules)
			Map.entry("TrinketCaseInventoryMixin", BOTANIA),
			Map.entry("TrinketCaseSlotMixin", BOTANIA),
			Map.entry("TrinketCaseMenuMixin", BOTANIA),
			Map.entry("ItemBackedInventoryAccessor", BOTANIA),
			// Liquid XP bridge (fluid/FluidBridge)
			Map.entry("MemoryStickItemMixin", PNC),
			Map.entry("PneumaticCraftUtilsMixin", PNC),
			Map.entry("XPFluidManagerMixin", PNC),
			Map.entry("XpHelperMixin", SOPHISTICATED),
			Map.entry("XpPumpUpgradeWrapperMixin", SOPHISTICATED),
			// Immersive Portals: ordinary fire builds no portals, Dark Fire does (plan_portal_changes.md 1)
			Map.entry("IPFireGateMixin", List.of("imm_ptl_core")));

	public MagicMixinPlugin() {
		super("[creativemagic]", NEEDS_MOD);
	}
}
