package io.github.mortuusars.exposure.forge.event;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Register;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Lens;
import io.github.mortuusars.exposure.event.CommonEvents;
import io.github.mortuusars.exposure.event.ServerEvents;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

/** Upstream {@code neoforge.event.NeoForgeCommonEvents}. */
public class ForgeCommonEvents {
   @Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
   public static class GameBus {
      @SubscribeEvent
      public static void serverStarted(ServerStartedEvent event) {
         ServerEvents.serverStarted(event.getServer());
      }

      @SubscribeEvent
      public static void onDatapackSync(OnDatapackSyncEvent event) {
         ServerEvents.syncDatapack(event.getPlayer() != null
                 ? java.util.stream.Stream.of(event.getPlayer())
                 : event.getPlayerList().getPlayers().stream());
      }

      @SubscribeEvent
      public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
         Packets.onPlayerLeft(event.getEntity().getUUID());
      }

      @SubscribeEvent
      public static void registerCommands(RegisterCommandsEvent event) {
         CommonEvents.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
      }
   }

   @Mod.EventBusSubscriber(modid = Exposure.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
   public static class ModBus {
      @SubscribeEvent
      public static void commonSetup(FMLCommonSetupEvent event) {
         event.enqueueWork(() -> {
            Exposure.Stats.STATS.forEach((location, statFormatter) -> Stats.CUSTOM.get(location, statFormatter));
            Register.registerCriteriaTriggers();
            Exposure.ItemSubPredicates.register();
         });
      }

      /** 1.19.2 datapack registries: a Forge registry built with {@code dataPackRegistry(codec, networkCodec)}. */
      @SubscribeEvent
      public static void addDatapackRegistries(NewRegistryEvent event) {
         event.create(new RegistryBuilder<ColorPalette>().setName(Exposure.Registries.COLOR_PALETTE.location())
                 .disableSaving().dataPackRegistry(ColorPalette.CODEC, ColorPalette.CODEC));
         event.create(new RegistryBuilder<Lens>().setName(Exposure.Registries.LENS.location())
                 .disableSaving().dataPackRegistry(Lens.CODEC, Lens.CODEC));
         event.create(new RegistryBuilder<Filter>().setName(Exposure.Registries.FILTER.location())
                 .disableSaving().dataPackRegistry(Filter.CODEC, Filter.CODEC));
      }
   }
}
