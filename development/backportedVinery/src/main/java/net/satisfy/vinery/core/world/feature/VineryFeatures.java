package net.satisfy.vinery.core.world.feature;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.vinery.core.Vinery;

public class VineryFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Vinery.MOD_ID);
    public static final RegistryObject<Feature<BlockStateConfiguration>> JUNGLE_GRAPE_FEATURE = FEATURES.register("jungle_grape_feature", () -> new JungleGrapeFeature(BlockStateConfiguration.CODEC));
}
