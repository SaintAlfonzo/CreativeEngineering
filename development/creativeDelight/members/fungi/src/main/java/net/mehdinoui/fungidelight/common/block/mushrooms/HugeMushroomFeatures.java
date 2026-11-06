package net.mehdinoui.fungidelight.common.block.mushrooms;

import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.server.ServerLifecycleHooks;

// Port helper: upstream's 1.20.1 MushroomBlock took a ResourceKey; 1.19.2 takes a holder supplier. The configured
// features are JSON (data/fungidelight/worldgen/configured_feature), so they live in the server's registry access, not
// in BuiltinRegistries. MushroomBlock only calls the supplier from growMushroom, which runs on the server.
public final class HugeMushroomFeatures {
   private HugeMushroomFeatures() {
   }

   public static Supplier<Holder<? extends ConfiguredFeature<?, ?>>> supplier(String path) {
      ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, new ResourceLocation("fungidelight", path));
      return () -> ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getHolderOrThrow(key);
   }
}
