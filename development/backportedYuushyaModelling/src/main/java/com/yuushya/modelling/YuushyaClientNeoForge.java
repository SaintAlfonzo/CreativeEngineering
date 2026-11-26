// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 YuushyaClientNeoForge.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md (model wrapping on ModelEvent.BakingCompleted as in 2.2.0, since
// Forge 43 has no ModifyBakingResult; color handlers static; item model cache cleared on resource reload, B2).
package com.yuushya.modelling;

import com.yuushya.modelling.client.ItemModelCache;
import com.yuushya.modelling.client.NeoItemBlockModel;
import com.yuushya.modelling.client.NeoShowBlockModel;
import com.yuushya.modelling.registries.BlockRegistry;
import com.yuushya.modelling.registries.ItemRegistry;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = "yuushya_modelling")
public class YuushyaClientNeoForge {
   @SubscribeEvent
   public static void onInitializeClient(FMLClientSetupEvent event) {
      // TODO(A6): event.enqueueWork(() -> new ColorTexture()); (gui/widget/ColorTexture comes with the screens)
   }

   @SubscribeEvent
   public static void onModelBaked(BakingCompleted event) {
      Map<ResourceLocation, BakedModel> models = event.getModels();
      wrap(models, "showblock", BlockRegistry.SHOW_BLOCK.get(), NeoShowBlockModel::new);
      wrap(models, "itemblock", BlockRegistry.ITEM_BLOCK.get(), NeoItemBlockModel::new);
   }

   private static void wrap(Map<ResourceLocation, BakedModel> models, String name, Block block, BiFunction<Direction, BakedModel, BakedModel> factory) {
      ModelResourceLocation inventory = new ModelResourceLocation(new ResourceLocation("yuushya", name), "inventory");
      models.put(inventory, factory.apply(Direction.SOUTH, models.get(inventory)));

      for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
         ModelResourceLocation stateResourceLocation = BlockModelShaper.stateToModelLocation(blockState);
         models.put(stateResourceLocation, factory.apply(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING), models.get(stateResourceLocation)));
      }
   }

   @SubscribeEvent
   public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
      event.registerReloadListener(ItemModelCache.RELOAD_LISTENER);
   }

   // Upstream 2.4.1 declared both color handlers non-static in this @EventBusSubscriber class, so Forge never called
   // them (tinted blocks such as grass and leaves rendered untinted in Entity Blocks); static as in 2.2.0.
   @SubscribeEvent
   public static void handleBlockColor(RegisterColorHandlersEvent.Block event) {
      event.register((state, view, pos, tintIndex) -> {
         if (tintIndex > -1) {
            BlockState trueState = Block.stateById(tintIndex >> 8);
            int trueTint = tintIndex & 0xFF;
            return event.getBlockColors().getColor(trueState, view, pos, trueTint);
         } else {
            return -1;
         }
      }, BlockRegistry.SHOW_BLOCK.get());
   }

   @SubscribeEvent
   public static void handleItemColor(RegisterColorHandlersEvent.Item event) {
      event.register((itemStack, i) -> {
         BlockState blockState = YuushyaDataTags.getBlockState(itemStack);
         if (blockState == null) {
            blockState = Blocks.AIR.defaultBlockState();
         }

         return event.getBlockColors().getColor(blockState, null, null, i);
      }, ItemRegistry.GET_BLOCKSTATE_ITEM.get());
      event.register((arg, tintIndex) -> {
         if (tintIndex > -1) {
            BlockState trueState = Block.stateById(tintIndex >> 8);
            int trueTint = tintIndex & 0xFF;
            return event.getBlockColors().getColor(trueState, null, null, trueTint);
         } else {
            return -1;
         }
      }, ItemRegistry.SHOW_BLOCK.get());
   }
}
