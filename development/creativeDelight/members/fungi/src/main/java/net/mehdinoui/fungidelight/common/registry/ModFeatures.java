package net.mehdinoui.fungidelight.common.registry;

import net.mehdinoui.fungidelight.common.world.feature.HugeInkyCapFeature;
import net.mehdinoui.fungidelight.common.world.feature.HugeMorelFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
   public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "fungidelight");
   public static final RegistryObject<Feature<NoneFeatureConfiguration>> HUGE_MOREL_FEATURE = FEATURES.register(
      "huge_morel_feature", () -> new HugeMorelFeature(NoneFeatureConfiguration.CODEC)
   );
   public static final RegistryObject<Feature<NoneFeatureConfiguration>> HUGE_INKY_CAP_FEATURE = FEATURES.register(
      "huge_inky_cap_feature", () -> new HugeInkyCapFeature(NoneFeatureConfiguration.CODEC)
   );

   public static void register(IEventBus eventBus) {
      FEATURES.register(eventBus);
   }
}
