package net.mehdinoui.fungidelight;

import net.mehdinoui.fungidelight.common.registry.ModBlocks;
import net.mehdinoui.fungidelight.common.registry.ModEffects;
import net.mehdinoui.fungidelight.common.registry.ModEntities;
import net.mehdinoui.fungidelight.common.registry.ModFeatures;
import net.mehdinoui.fungidelight.common.registry.ModItems;
import net.mehdinoui.fungidelight.common.registry.ModPotions;
import net.mehdinoui.fungidelight.common.registry.ModRecipes;
import net.mehdinoui.fungidelight.common.registry.ModSoundEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// Fungi Delight 1.3 ported to Forge 1.19.2 (feature_writeup/fungi_delight.md). No config file: every switch upstream
// read from fungidelight-common.toml is hard-coded to its shipped default. Not ported: the sniffer mixin and tag, the
// configurable rarity filter, biome modifiers and placed features (the pack places everything), the compost hook,
// the mooshroom breeding mixin (patcherMod owns mooshroom breeding).
@Mod(FungiDelight.MOD_ID)
public class FungiDelight {
   public static final String MOD_ID = "fungidelight";

   public FungiDelight() {
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      ModItems.register(modEventBus);
      ModBlocks.register(modEventBus);
      ModFeatures.register(modEventBus);
      ModSoundEvents.register(modEventBus);
      ModEntities.register(modEventBus);
      ModEffects.register(modEventBus);
      ModPotions.register(modEventBus);
      ModRecipes.register(modEventBus);
   }
}
