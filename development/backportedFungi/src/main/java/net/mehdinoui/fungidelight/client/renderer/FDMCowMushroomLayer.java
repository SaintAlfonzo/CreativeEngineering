package net.mehdinoui.fungidelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehdinoui.fungidelight.common.entity.custom.FDMCow;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.level.block.state.BlockState;

// 1.19.2: PoseStack.translate/mulPose with com.mojang.math.Vector3f (1.20 uses JOML Axis).
public class FDMCowMushroomLayer extends RenderLayer<FDMCow, CowModel<FDMCow>> {
   private final BlockRenderDispatcher blockRenderer;

   public FDMCowMushroomLayer(RenderLayerParent<FDMCow, CowModel<FDMCow>> renderer, BlockRenderDispatcher blockRenderer) {
      super(renderer);
      this.blockRenderer = blockRenderer;
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      FDMCow entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (!entity.isBaby() && !entity.isInvisible()) {
         BlockState blockstate = entity.getMushroomBlockState();
         int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
         poseStack.pushPose();
         poseStack.translate(0.2F, -0.35F, 0.5F);
         poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
         poseStack.scale(-1.0F, -1.0F, 1.0F);
         poseStack.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderer.renderSingleBlock(blockstate, poseStack, buffer, packedLight, overlay);
         poseStack.popPose();
         poseStack.pushPose();
         poseStack.translate(0.2F, -0.35F, 0.5F);
         poseStack.mulPose(Vector3f.YP.rotationDegrees(42.0F));
         poseStack.translate(0.1F, 0.0F, -0.6F);
         poseStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
         poseStack.scale(-1.0F, -1.0F, 1.0F);
         poseStack.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderer.renderSingleBlock(blockstate, poseStack, buffer, packedLight, overlay);
         poseStack.popPose();
         poseStack.pushPose();
         ((CowModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
         poseStack.translate(0.0F, -0.7F, -0.2F);
         poseStack.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
         poseStack.scale(-1.0F, -1.0F, 1.0F);
         poseStack.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderer.renderSingleBlock(blockstate, poseStack, buffer, packedLight, overlay);
         poseStack.popPose();
      }
   }
}
