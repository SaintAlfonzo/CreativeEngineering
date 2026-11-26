// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 event/ClientEvent.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.event;

import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntityRender;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntityRender;
import com.yuushya.modelling.gui.engrave.EngraveBlockResultLoader;
import com.yuushya.modelling.registries.BlockEntityRegistry;
import com.yuushya.modelling.gui.engrave.EngraveItemResultLoader;
import com.yuushya.modelling.registries.ItemRegistry;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.util.List;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class ClientEvent {
   private static void registerScreens() {
      // TODO(A6): MenuScreens.register(MenuRegistry.EngraveMenu.get(), EngraveScreen::new);
      // TODO(A6): MenuScreens.register(MenuRegistry.HistoryMenu.get(), HistoryScreen::new);
   }

   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers event) {
      event.registerBlockEntityRenderer(BlockEntityRegistry.SHOW_BLOCK_ENTITY.get(), ShowBlockEntityRender::new);
      event.registerBlockEntityRenderer(BlockEntityRegistry.ITEM_BLOCK_ENTITY.get(), ItemBlockEntityRender::new);
      // PHASE-B: event.registerBlockEntityRenderer(BlockEntityRegistry.TEXT_BLOCK_ENTITY.get(), TextBlockEntityRender::new);
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
         registerScreens();
         registerItemProperties();
         load();
      });
   }

   private static void registerItemProperties() {
      for (String s : List.of("rot_trans_item", "pos_trans_item", "micro_pos_trans_item", "get_showblock_item")) {
         ItemProperties.register(
            (Item)Registry.ITEM.get(new ResourceLocation("yuushya", s)),
            new ResourceLocation("direction"),
            (itemStack, clientWorld, livingEntity, i) -> (float)YuushyaDataTags.getTransDirection(itemStack) * 0.1F
         );
      }

      ItemProperties.register((Item)ItemRegistry.GET_BLOCKSTATE_ITEM.get(), new ResourceLocation("direction"), (itemStack, clientWorld, livingEntity, i) -> {
         BlockState blockState = YuushyaDataTags.getBlockState(itemStack);
         return blockState != null && !blockState.equals(Blocks.AIR.defaultBlockState()) ? 1.0F : 0.0F;
      });
   }

   public static void load() {
      EngraveBlockResultLoader.SHOWBLOCK_ITEM_MAP.clear();
      EngraveItemResultLoader.ITEMBLOCK_ITEM_MAP.clear();
      // PHASE-B: EngraveTextResultLoader.TEXTBLOCK_ITEM_MAP.clear();
      EngraveBlockResultLoader.load();
      EngraveItemResultLoader.load();
      // PHASE-B: EngraveTextResultLoader.load();
   }
}
