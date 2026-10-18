package net.satisfy.vinery.core.world;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.satisfy.vinery.core.Vinery;

public class VineryWorldGen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_CHERRY_KEY = configured("dark_cherry");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_CHERRY_VARIANT_KEY = configured("dark_cherry_variant");
    public static final ResourceKey<ConfiguredFeature<?, ?>> APPLE_KEY = configured("apple");
    public static final ResourceKey<ConfiguredFeature<?, ?>> APPLE_VARIANT_KEY = configured("apple_variant");

    public static final ResourceKey<PlacedFeature> TREE_CHERRY_PLACED_KEY = placed("tree_cherry");
    public static final ResourceKey<PlacedFeature> TREE_APPLE_PLACED_KEY = placed("tree_apple");

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, Vinery.identifier(name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, Vinery.identifier(name));
    }
}
