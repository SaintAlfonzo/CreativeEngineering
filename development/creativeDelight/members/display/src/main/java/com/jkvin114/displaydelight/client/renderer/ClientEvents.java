package com.jkvin114.displaydelight.client.renderer;

import java.util.Map;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "displaydelight",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
/**
 * Wraps every Display Delight model so a plated food with its plate hidden (axe) is drawn without the plate. 1.19.2 has
 * no ModifyBakingResult; BakingCompleted hands over the same map before the block model cache is rebuilt.
 */
public class ClientEvents {
   @SubscribeEvent
   public static void modifyBakingResult(BakingCompleted event) {
      Map<ResourceLocation, BakedModel> models = event.getModels();
      models.replaceAll((loc, model) -> (BakedModel)(loc.getNamespace().equals("displaydelight") ? new PlateHidingBakedModel(model) : model));
   }
}
