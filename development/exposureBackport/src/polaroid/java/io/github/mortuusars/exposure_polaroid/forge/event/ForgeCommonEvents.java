package io.github.mortuusars.exposure_polaroid.forge.event;

import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import io.github.mortuusars.exposure_polaroid.Register;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Upstream {@code neoforge/event/NeoForgeCommonEvents}. The creative tab entries are {@code .tab(...)} on the items
 * (see {@link ExposurePolaroid.Items}); packets: none upstream.
 */
public class ForgeCommonEvents {
   @Mod.EventBusSubscriber(modid = ExposurePolaroid.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
   public static class ForgeBus {
      @SubscribeEvent
      public static void registerCommands(RegisterCommandsEvent event) {
      }
   }

   @Mod.EventBusSubscriber(modid = ExposurePolaroid.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
   public static class ModBus {
      @SubscribeEvent
      public static void commonSetup(FMLCommonSetupEvent event) {
         event.enqueueWork(() -> {
            ExposurePolaroid.Stats.STATS.forEach((location, statFormatter) -> Stats.CUSTOM.get(location, statFormatter));
            Register.registerCriteriaTriggers();
         });
      }
   }
}
