package dev.titanite.sparkwave.exposureexpanded.forge;

import dev.titanite.sparkwave.exposureexpanded.ExposureExpanded;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entry point; upstream {@code dev.titanite.sparkwave.exposureexpanded.neoforge.ExampleModNeoForge}. Upstream's
 * {@code ExposureExpandedLootModifier} isn't ported: its codec is Exposure's {@code add_table}, which the loot modifier
 * JSON names directly, so Exposure's {@code ConfigurableAddTableLootModifier} handles it.
 */
@Mod(ExposureExpanded.MOD_ID)
public class ExposureExpandedForge {
   public ExposureExpandedForge() {
      ExposureExpanded.init(FMLJavaModLoadingContext.get().getModEventBus());
   }
}
