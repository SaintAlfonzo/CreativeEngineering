package com.SaintAlfonzo.creativeworldgen.visceralheap;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

/**
 * The Biomes O' Plenty entries the pack keeps: only what the Visceral Heap needs (feature_writeup/visceral_heap.md).
 * Keyed by registry, because names such as {@code hair} repeat across registries. Anything else BOP tries to register is
 * dropped before its supplier runs, including whole registries with no row here (entities, block entities, carvers).
 */
public final class HeapAllowlist {

    public static final String MOD_ID = "biomesoplenty";

    private static final Map<String, Set<String>> KEPT = Map.of(
            "minecraft:worldgen/biome", Set.of("visceral_heap"),
            "minecraft:block", Set.of("blood", "flesh", "porous_flesh", "pus_bubble", "flesh_tendons",
                    "flesh_tendons_strand", "eyebulb", "hair"),
            "minecraft:item", Set.of("blood", "flesh", "porous_flesh", "pus_bubble", "flesh_tendons",
                    "flesh_tendons_strand", "eyebulb", "hair", "blood_bucket"),
            "minecraft:fluid", Set.of("blood", "flowing_blood"),
            "forge:fluid_type", Set.of("blood"),
            "minecraft:worldgen/feature", Set.of("bone_spine", "flesh_tendon", "hanging_flesh_tendon"),
            "minecraft:worldgen/configured_feature", Set.of("blood_lake", "blood_spring", "eyebulb", "flesh_tendon",
                    "hair", "hanging_flesh_tendon", "nether_bone_spine", "porous_flesh", "pus_bubbles"),
            "minecraft:worldgen/placed_feature", Set.of("blood_lake", "blood_spring", "eyebulb", "flesh_tendon",
                    "hair", "hanging_flesh_tendons", "nether_bone_spine", "porous_flesh", "pus_bubbles"),
            "minecraft:particle_type", Set.of("dripping_blood", "falling_blood", "landing_blood", "pus"),
            "minecraft:sound_event", Set.of("music.nether.visceral_heap"));

    private HeapAllowlist() {}

    public static boolean keep(ResourceLocation registry, String name) {
        Set<String> names = KEPT.get(registry.toString());
        return names != null && names.contains(name);
    }
}
