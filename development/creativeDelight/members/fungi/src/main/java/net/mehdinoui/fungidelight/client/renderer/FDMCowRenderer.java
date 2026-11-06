package net.mehdinoui.fungidelight.client.renderer;

import net.mehdinoui.fungidelight.common.entity.custom.FDMCow;
import net.mehdinoui.fungidelight.common.registry.ModEntities;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class FDMCowRenderer extends MobRenderer<FDMCow, CowModel<FDMCow>> {
   private static final ResourceLocation INKY_LOCATION = new ResourceLocation("fungidelight", "textures/entity/inky_cap_cow.png");
   private static final ResourceLocation MOREL_LOCATION = new ResourceLocation("fungidelight", "textures/entity/morel_cow.png");

   public FDMCowRenderer(Context context) {
      super(context, new CowModel(context.bakeLayer(ModelLayers.MOOSHROOM)), 0.7F);
      this.addLayer(new FDMCowMushroomLayer(this, context.getBlockRenderDispatcher()));
   }

   public ResourceLocation getTextureLocation(FDMCow entity) {
      return entity.getType() == ModEntities.INKY_CAP_COW.get() ? INKY_LOCATION : MOREL_LOCATION;
   }
}
