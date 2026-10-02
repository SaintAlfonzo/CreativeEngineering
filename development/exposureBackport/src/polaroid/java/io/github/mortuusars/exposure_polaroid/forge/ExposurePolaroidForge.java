package io.github.mortuusars.exposure_polaroid.forge;

import io.github.mortuusars.exposure_polaroid.Config;
import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import io.github.mortuusars.exposure_polaroid.Register;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge entry point; upstream {@code io.github.mortuusars.exposure_polaroid.neoforge.ExposurePolaroidNeoForge}.
 * Upstream registers no packets (its packet lists are empty), so there is no network channel. The NeoForge config
 * screen extension has no 1.19.2 counterpart.
 */
@Mod(ExposurePolaroid.ID)
public class ExposurePolaroidForge {
   public ExposurePolaroidForge() {
      ExposurePolaroid.init();
      ExposurePolaroid.Stats.STATS.forEach((location, formatter) -> Register.CUSTOM_STATS.register(location.getPath(), () -> location));
      ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
      ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      Register.register(modEventBus);
   }
}
