package io.github.mortuusars.exposure.forge;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.Register;
import io.github.mortuusars.exposure.forge.loot.ConfigurableAddTableLootModifier;
import io.github.mortuusars.exposure.network.Packets;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge entry point; upstream {@code io.github.mortuusars.exposure.neoforge.ExposureNeoForge}. */
@Mod(Exposure.ID)
public class ExposureForge {
   public ExposureForge() {
      Exposure.init();
      Exposure.Stats.STATS.forEach((location, formatter) -> Register.CUSTOM_STATS.register(location.getPath(), () -> location));
      ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
      ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
      ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      Register.register(modEventBus);
      LootModifiers.LOOT_MODIFIERS.register(modEventBus);
      Packets.register();
   }

   public static class LootModifiers {
      private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Exposure.ID);
      public static final RegistryObject<Codec<ConfigurableAddTableLootModifier>> ADD_TABLE = LOOT_MODIFIERS.register("add_table", ConfigurableAddTableLootModifier.CODEC);
   }
}
