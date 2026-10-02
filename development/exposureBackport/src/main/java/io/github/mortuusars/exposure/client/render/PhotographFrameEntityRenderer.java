package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.PhotographFrameEntity;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.block.model.ItemTransforms;

public class PhotographFrameEntityRenderer<T extends PhotographFrameEntity> extends EntityRenderer<T> {
   protected final BlockRenderDispatcher blockRenderer;

   public PhotographFrameEntityRenderer(Context context) {
      super(context);
      this.blockRenderer = context.getBlockRenderDispatcher();
   }

   @NotNull
   public ResourceLocation getTextureLocation(@NotNull T pEntity) {
      return InventoryMenu.BLOCK_ATLAS;
   }

   public ResourceLocation getModelLocation(T entity, int size) {
      return switch (size) {
         case 0 -> ExposureClient.Models.PHOTOGRAPH_FRAME_SMALL;
         case 1 -> ExposureClient.Models.PHOTOGRAPH_FRAME_MEDIUM;
         case 2 -> ExposureClient.Models.PHOTOGRAPH_FRAME_LARGE;
         default -> throw new IllegalArgumentException("size " + size + " is not valid. Expected 0-2.");
      };
   }

   @NotNull
   protected RenderType getRenderType() {
      return Sheets.solidBlockSheet();
   }

   public void render(
      @NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight
   ) {
      if (Minecraft.getInstance().hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == entity) {
         Minecraft.getInstance().crosshairPickEntity = entity;
      }

      Direction direction = entity.getDirection();
      int size = entity.getSize();
      poseStack.pushPose();
      poseStack.translate((float)direction.getStepX() * 0.3F, (float)direction.getStepY() * 0.3F, (float)direction.getStepZ() * 0.3F);
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      poseStack.popPose();
      poseStack.pushPose();
      double hangOffset = 0.46875;
      poseStack.translate((double)direction.getStepX() * hangOffset, (double)direction.getStepY() * hangOffset, (double)direction.getStepZ() * hangOffset);
      poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot()));
      poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - entity.getYRot()));
      ItemStack item = entity.getItem();
      if (!item.isEmpty()) {
         boolean photographRendered = this.renderPhotograph(entity, poseStack, bufferSource, packedLight, item, size);
         if (!photographRendered) {
            poseStack.pushPose();
            float scale = 0.65F + (float)entity.getSize() * 0.5F;
            poseStack.translate(0.0, 0.0, 0.46875);
            poseStack.scale(scale, scale, scale * 0.75F);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)entity.getItemRotation() * 360.0F / 4.0F));
            Minecrft.get()
               .getItemRenderer()
               .renderStatic(item, ItemTransforms.TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, 0);
            poseStack.popPose();
         }
      }

      if (!entity.isFrameInvisible()) {
         this.renderFrame(entity, poseStack, bufferSource, packedLight, size);
      }

      poseStack.popPose();
   }

   protected void renderFrame(@NotNull T entity, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int size) {
      poseStack.pushPose();
      poseStack.translate(-0.5F, -0.5F, -0.5F);
      ResourceLocation modelLocation = this.getModelLocation(entity, size);
      BakedModel model = PlatformHelperClient.getModel(modelLocation);
      this.blockRenderer
         .getModelRenderer()
         .renderModel(poseStack.last(), bufferSource.getBuffer(this.getRenderType()), null, model, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
      poseStack.popPose();
   }

   protected boolean renderPhotograph(
      @NotNull T entity, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, ItemStack item, int size
   ) {
      poseStack.pushPose();
      boolean frameInvisible = entity.isFrameInvisible();
      float frameBorderOffset = frameInvisible ? 0.0F : 0.125F;
      float offsetFromCenter = frameInvisible ? 0.497F : 0.48F;
      offsetFromCenter = (float)((double)offsetFromCenter - (Double)Config.Client.PHOTOGRAPH_FRAME_IMAGE_OFFSET.get());
      float desiredSize = (float)(size + 1) - frameBorderOffset * 2.0F;
      poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)entity.getItemRotation() * 360.0F / 4.0F));
      poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      poseStack.translate(
         -0.5 * (double)(size + 1) + (double)frameBorderOffset, -0.5 * (double)(size + 1) + (double)frameBorderOffset, (double)offsetFromCenter
      );
      poseStack.scale(desiredSize, desiredSize, 1.0F);
      boolean isGlowing = entity.isGlowing();
      if (isGlowing) {
         packedLight = 15728880;
      }

      int brightness = isGlowing ? 255 : this.getPhotographBrightness(entity);
      boolean photographRendered = false;
      if ((Boolean)Config.Client.PIXEL_PERFECT_PHOTOGRAPH_FRAME.get()) {
         if (item.getItem() instanceof PhotographItem photographItem) {
            PhotographStyle style = PhotographStyle.of(item);
            Frame frame = photographItem.getFrame(item);
            RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));
            int pixels = 16 * (entity.getSize() + 1);
            if (!frameInvisible) {
               pixels -= 4;
            }

            image = image.modifyWith(ImageEffect.Resize.to(pixels)::modify, "pixels-" + pixels);
            ExposureClient.imageRenderer()
               .render(image, poseStack, bufferSource, RenderCoordinates.DEFAULT, packedLight, brightness, brightness, brightness, 255);
            photographRendered = !image.isEmpty();
         }
      } else {
         photographRendered = ExposureClient.photographRenderer()
            .render(item, false, false, poseStack, bufferSource, packedLight, brightness, brightness, brightness, 255);
      }

      poseStack.popPose();
      return photographRendered;
   }

   public int getPhotographBrightness(T entity) {
      if (entity.getDirection() == Direction.UP) {
         return 255;
      } else {
         int lightLevel = entity.level.getBrightness(LightLayer.BLOCK, entity.blockPosition());
         float shadeFactor = entity.level.getShade(entity.getDirection(), true);
         shadeFactor += (1.0F - shadeFactor) * 0.2F;
         int shadedBrightness = (int)(255.0F * shadeFactor);
         int missingLight = 255 - shadedBrightness;
         int lightUp = (int)((float)missingLight * (float)lightLevel / 15.0F * 0.5F);
         return Math.min(255, shadedBrightness + lightUp);
      }
   }

   protected void renderNameTag(T entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
      double d = this.entityRenderDispatcher.distanceToSqr(entity);
      if (!(d > 4096.0)) {
         Vec3 vec3 = new Vec3(0.0, entity.getBbHeight(), 0.0); // 1.20.5 NAME_TAG attachment fallback
         if (vec3 != null) {
            boolean bl = !entity.isDiscrete();
            poseStack.pushPose();
            double yOffset = entity.getDirection().getAxis().isHorizontal()
               ? vec3.y - 0.2 + (double)entity.getSize() * 0.5
               : (entity.getDirection().getStepY() > 0 ? vec3.y - 0.5 : vec3.y - 1.0);
            poseStack.translate(vec3.x, vec3.y + yOffset, vec3.z);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();
            float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int j = (int)(f * 255.0F) << 24;
            Font font = this.getFont();
            float g = (float)(-font.width(displayName) / 2);
            font.drawInBatch(displayName, g, 0.0F, 553648127, false, matrix4f, bufferSource, bl, j, packedLight);
            if (bl) {
               font.drawInBatch(displayName, g, 0.0F, -1, false, matrix4f, bufferSource, false, 0, packedLight);
            }

            poseStack.popPose();
         }
      }
   }

   protected boolean shouldShowName(T entity) {
      if (Minecraft.renderNames()
         && !entity.getItem().isEmpty()
         && DataComponents.CUSTOM_NAME.has(entity.getItem())
         && Minecraft.getInstance().crosshairPickEntity == entity) {
         double distSqr = Minecraft.getInstance().crosshairPickEntity.distanceToSqr(entity);
         float showRangeSqr = entity.isDiscrete() ? 32.0F : 64.0F;
         return distSqr < (double)(showRangeSqr * showRangeSqr);
      } else {
         return false;
      }
   }
}
