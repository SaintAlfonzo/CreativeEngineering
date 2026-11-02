package net.mehdinoui.fungidelight.client.event;

import net.mehdinoui.fungidelight.client.renderer.FDMCowRenderer;
import net.mehdinoui.fungidelight.common.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "fungidelight",
   bus = Bus.MOD
)
public class ModClientSetupEvents {
   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)ModEntities.INKY_CAP_COW.get(), FDMCowRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.MOREL_COW.get(), FDMCowRenderer::new);
   }
}
