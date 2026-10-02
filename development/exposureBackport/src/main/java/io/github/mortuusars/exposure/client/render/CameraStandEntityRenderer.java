package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.renderer.block.model.ItemTransforms;

public class CameraStandEntityRenderer<T extends CameraStandEntity> extends EntityRenderer<T> {
   public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/item/camera.png");
   public static final float MOUNT_SCALE = 0.9F;
   protected final BlockRenderDispatcher blockRenderer;

   public CameraStandEntityRenderer(Context context) {
      super(context);
      this.blockRenderer = context.getBlockRenderDispatcher();
   }

   @NotNull
   public ResourceLocation getTextureLocation(T entity) {
      return TEXTURE_LOCATION;
   }

   public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      float hurtTime = (float)entity.getHurtTime() - partialTick;
      float damage = Math.max(0.0F, entity.getDamage() - partialTick);
      if (hurtTime > 0.0F) {
         float rotation = Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float)entity.getHurtDir();
         poseStack.mulPose(Vector3f.YP.rotationDegrees(rotation));
         poseStack.mulPose(Vector3f.XP.rotationDegrees(rotation));
      }

      float entityPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
      this.renderStand(entity, entityYaw, entityPitch, partialTick, poseStack, bufferSource, packedLight);
      this.renderMount(entity, entityYaw, entityPitch, partialTick, poseStack, bufferSource, packedLight);
      if (!entity.getCamera().isEmpty()) {
         this.renderCamera(entity, entityYaw, entityPitch, partialTick, poseStack, bufferSource, packedLight);
      }
   }

   private void renderStand(
      T entity, float entityYaw, float entityPitch, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight
   ) {
      poseStack.pushPose();
      if (entity.getVehicle() != null) {
         float vehicleRot = Mth.lerp(partialTick, entity.getVehicle().yRotO, entity.getVehicle().getYRot());
         poseStack.mulPose(Vector3f.YP.rotationDegrees(-vehicleRot + 45.0F));
      }

      poseStack.translate(-0.5F, 0.0F, -0.5F);
      ResourceLocation modelLocation = ExposureClient.Models.CAMERA_STAND;
      BakedModel model = PlatformHelperClient.getModel(modelLocation);
      this.blockRenderer
         .getModelRenderer()
         .renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.solid()), null, model, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
      poseStack.popPose();
   }

   private void renderMount(
      T entity, float entityYaw, float entityPitch, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight
   ) {
      poseStack.pushPose();
      poseStack.translate(0.0, 1.125, 0.0);
      float scale = 0.9F;
      poseStack.scale(scale, scale, scale);
      poseStack.mulPose(Vector3f.YP.rotationDegrees(-entityYaw + 180.0F));
      poseStack.mulPose(Vector3f.XP.rotationDegrees(-entityPitch));
      if (entity.isMalfunctioned()) {
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(-50.0F));
         poseStack.mulPose(Vector3f.XP.rotationDegrees(-10.0F));
      }

      poseStack.translate(-0.5F, 0.0F, -0.5F);
      ResourceLocation mountModelLocation = ExposureClient.Models.CAMERA_STAND_MOUNT;
      BakedModel mountModel = PlatformHelperClient.getModel(mountModelLocation);
      this.blockRenderer
         .getModelRenderer()
         .renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.solid()), null, mountModel, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
      poseStack.popPose();
   }

   private void renderCamera(
      T entity, float entityYaw, float entityPitch, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight
   ) {
      poseStack.pushPose();
      poseStack.translate(0.0, 1.125, 0.0);
      poseStack.mulPose(Vector3f.YP.rotationDegrees(-entityYaw + 180.0F));
      poseStack.mulPose(Vector3f.XP.rotationDegrees(-entityPitch));
      poseStack.translate(0.0, 0.1125F, 0.0);
      if (entity.isMalfunctioned()) {
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(-50.0F));
         poseStack.mulPose(Vector3f.XP.rotationDegrees(-15.0F));
      }

      ItemStack camera = entity.getCamera();
      float scale = camera.getItem() instanceof CameraItem cameraItem ? cameraItem.getScaleOnStand() : 0.9F;
      poseStack.scale(scale, scale, scale);
      poseStack.translate(0.0, 0.5, 0.0);
      Minecrft.get()
         .getItemRenderer()
         .renderStatic(camera, ItemTransforms.TransformType.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, 0);
      poseStack.popPose();
   }
}
