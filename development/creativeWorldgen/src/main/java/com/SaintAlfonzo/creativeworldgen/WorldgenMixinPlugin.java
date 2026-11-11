package com.SaintAlfonzo.creativeworldgen;

import com.SaintAlfonzo.creativeworldgen.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativeworldgen.mixins.json: the table form of the shared {@link GatedMixinPlugin}
 * (development/shared/mixinplugin). A mixin in {@link #NEEDS_MOD} applies only when one of its mods is loaded (each one
 * targets a class of that mod). Every other mixin targets vanilla and always applies, as it did in patcherMod. The
 * mixins moved from patcherMod on 2026-11-02 unchanged apart from their package (SRG names, remap = false,
 * creativeengineering$ member prefixes).
 */
public class WorldgenMixinPlugin extends GatedMixinPlugin {

    private static final List<String> QUARK = List.of("quark");
    private static final List<String> MCA = List.of("mca");
    private static final List<String> BOP = List.of("biomesoplenty");

    private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
            // Async map trades (compat/AsyncMapTrades): Lightman's Currency and Cloud Storage explorer-map trades
            Map.entry("LCItemsForMapTradeMixin", List.of("lightmanscurrency")),
            Map.entry("CSSellingMapMixin", List.of("cloudstorage")),
            // Forgiving Void origin pull (forgivingvoid/ForgivingVoidBridge)
            Map.entry("ForgivingVoidMixin", List.of("forgivingvoid")),
            // Bushier Flowers' own flower growth off (wild flowers)
            Map.entry("BushierFlowersDisableMixin", List.of("bushierflowers")),
            // Quark permafrost merge ([worldgen.systems] permafrostMerge) and the Spiral Spire footprint check
            Map.entry("QuarkPermafrostStyleMixin", QUARK),
            Map.entry("QuarkSpiralSpireFootprintMixin", QUARK),
            // MCA villager pathing (its own node maker)
            Map.entry("McaVillagerPathGatesMixin", MCA),
            Map.entry("McaVillagerPathLaddersMixin", MCA),
            // Wise Oak sleep and anger loop, web lookups off (feature_writeup/wise_oak.md)
            Map.entry("WiseOakTileMixin", List.of("mysticaloaktree")),
            Map.entry("TreeLoreManagerMixin", List.of("mysticaloaktree")),
            // AE2 meteorite heights (worldgen/MeteoriteHeights)
            Map.entry("AeMeteoriteHeightMixin", List.of("ae2")),
            // Visceral Heap: Biomes O' Plenty reduced to one biome (feature_writeup/visceral_heap.md)
            Map.entry("HeapDeferredRegisterMixin", BOP),
            Map.entry("HeapResourcePackLoaderMixin", BOP),
            Map.entry("HeapModBiomesMixin", BOP),
            Map.entry("HeapModVanillaCompatMixin", BOP),
            Map.entry("HeapFleshTendonFeatureMixin", BOP),
            Map.entry("HeapColorHandlerMixin", BOP),
            Map.entry("HeapEntityRendererHandlerMixin", BOP),
            Map.entry("HeapParticleFactoryHandlerMixin", BOP));

    /** HeapFleshTendonFeatureMixin names a synthetic lambda, so these mixins only hold for the jar they were written against. */
    private static final Map<String, String> MOD_RANGE = Map.of("biomesoplenty", "[17.1.2.544]");

    public WorldgenMixinPlugin() {
        super("[creativeworldgen]", NEEDS_MOD, MOD_RANGE);
    }
}
