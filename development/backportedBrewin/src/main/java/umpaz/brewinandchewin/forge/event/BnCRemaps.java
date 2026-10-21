package umpaz.brewinandchewin.forge.event;

import java.util.Map;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCItems;
import vectorwing.farmersdelight.common.registry.ModEffects;

/**
 * Old-world ids from Brewin' and Chewin' 2.0 (alcohol.md 3.3). Upstream renames these through a DFU schema; Forge 1.19.2
 * does it with MissingMappingsEvent. Block entity ids aren't in the registry snapshot, so the old coaster block entity
 * id is rewritten by BlockEntityLegacyIdMixin; its single item then moves into the new one in CoasterBlockEntity#load.
 */
public class BnCRemaps {
   private static final Map<String, String> ITEMS = Map.of("item_coaster", "coaster", "scarlet_pierogies", "scarlet_pierogi");

   public static void register() {
      MinecraftForge.EVENT_BUS.addListener(BnCRemaps::onMissingMappings);
   }

   private static void onMissingMappings(MissingMappingsEvent event) {
      for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, BrewinAndChewin.MODID)) {
         String target = ITEMS.get(mapping.getKey().getPath());
         if (target != null) {
            mapping.remap(target.equals("coaster") ? BnCItems.COASTER : BnCItems.SCARLET_PIEROGI);
         }
      }
      for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, BrewinAndChewin.MODID)) {
         if (mapping.getKey().getPath().equals("item_coaster")) {
            mapping.remap(BnCBlocks.COASTER);
         }
      }
      for (MissingMappingsEvent.Mapping<MobEffect> mapping : event.getMappings(ForgeRegistries.Keys.MOB_EFFECTS, BrewinAndChewin.MODID)) {
         if (mapping.getKey().getPath().equals("satisfaction")) {
            mapping.remap(ModEffects.NOURISHMENT.get());
         }
      }
   }
}
