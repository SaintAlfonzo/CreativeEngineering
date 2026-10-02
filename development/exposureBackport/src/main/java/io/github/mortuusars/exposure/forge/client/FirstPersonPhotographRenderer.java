package io.github.mortuusars.exposure.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.mixin.client.ItemInHandRendererAccessor;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;

/**
 * First-person photographs. Upstream injects into {@code ItemInHandRenderer.renderArmWithItem} (mixin.client
 * .ItemInHandRendererMixin); Forge 1.19.2 posts {@link RenderHandEvent} right before that call, with the same
 * values, so this runs from the event. The rendering code is upstream's.
 */
public class FirstPersonPhotographRenderer {
   public static void onRenderHand(RenderHandEvent event) {
      if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
         event.setCanceled(true);
         return;
      }

      ItemStack stack = event.getItemStack();
      if (!(stack.getItem() instanceof PhotographItem) && !(stack.getItem() instanceof StackedPhotographsItem)) {
         return;
      }

      AbstractClientPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return;
      }

      ItemInHandRendererAccessor renderer = (ItemInHandRendererAccessor)Minecraft.getInstance().gameRenderer.itemInHandRenderer;
      boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
      HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
      PoseStack poseStack = event.getPoseStack();
      poseStack.pushPose();
      if (isMainHand && renderer.exposure$getOffHandItem().isEmpty()) {
         renderTwoHandedPhotograph(renderer, player, poseStack, event.getMultiBufferSource(), event.getPackedLight(), event.getInterpolatedPitch(), event.getEquipProgress(), event.getSwingProgress());
      } else {
         renderOneHandedPhotograph(renderer, player, poseStack, event.getMultiBufferSource(), event.getPackedLight(), event.getEquipProgress(), arm, event.getSwingProgress(), stack);
      }
      poseStack.popPose();
      event.setCanceled(true);
   }

   private static void renderOneHandedPhotograph(
      ItemInHandRendererAccessor renderer,
      AbstractClientPlayer player,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      float pEquippedProgress,
      HumanoidArm pHand,
      float pSwingProgress,
      ItemStack stack
   ) {
      float f = pHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      poseStack.translate((double)(f * 0.125F), -0.125, 0.0);
      if (!player.isInvisible()) {
         poseStack.pushPose();
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(f * 10.0F));
         renderer.exposure$renderPlayerArm(poseStack, buffer, packedLight, pEquippedProgress, pSwingProgress, pHand);
         poseStack.popPose();
      }

      poseStack.pushPose();
      poseStack.translate((double)(f * 0.51F), (double)(-0.08F + pEquippedProgress * -1.2F), -0.75);
      float f1 = Mth.sqrt(pSwingProgress);
      float f2 = Mth.sin(f1 * (float) Math.PI);
      float f3 = -0.5F * f2;
      float f4 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2));
      float f5 = -0.3F * Mth.sin(pSwingProgress * (float) Math.PI);
      poseStack.translate(f * f3, f4 - 0.3F * f2, f5);
      poseStack.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
      poseStack.mulPose(Vector3f.YP.rotationDegrees(f * f2 * -30.0F));
      poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      poseStack.scale(0.38F, 0.38F, 0.38F);
      poseStack.translate(-0.5, -0.5, 0.0);
      poseStack.scale(1.0F, 1.0F, -1.0F);
      ExposureClient.photographRenderer().render(stack, true, false, poseStack, buffer, packedLight);
      poseStack.popPose();
   }

   private static void renderTwoHandedPhotograph(
      ItemInHandRendererAccessor renderer,
      AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float pitch, float equippedProgress, float swingProgress
   ) {
      float f = Mth.sqrt(swingProgress);
      float f1 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
      float f2 = -0.4F * Mth.sin(f * (float) Math.PI);
      poseStack.translate(0.0, (double)(-f1 / 2.0F), (double)f2);
      float f3 = renderer.exposure$calculateMapTilt(pitch);
      poseStack.translate(0.0, (double)(0.04F + equippedProgress * -1.2F + f3 * -0.5F), -0.72F);
      poseStack.mulPose(Vector3f.XP.rotationDegrees(f3 * -85.0F));
      if (!player.isInvisible()) {
         poseStack.pushPose();
         poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
         renderer.exposure$renderMapHand(poseStack, buffer, packedLight, HumanoidArm.RIGHT);
         renderer.exposure$renderMapHand(poseStack, buffer, packedLight, HumanoidArm.LEFT);
         poseStack.popPose();
      }

      float f4 = Mth.sin(f * (float) Math.PI);
      poseStack.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
      poseStack.scale(2.0F, 2.0F, 2.0F);
      poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      poseStack.scale(0.38F, 0.38F, 0.38F);
      poseStack.translate(-0.5, -0.5, 0.0);
      poseStack.scale(1.0F, 1.0F, -1.0F);
      ExposureClient.photographRenderer().render(renderer.exposure$getMainHandItem(), true, false, poseStack, buffer, packedLight);
   }
}
