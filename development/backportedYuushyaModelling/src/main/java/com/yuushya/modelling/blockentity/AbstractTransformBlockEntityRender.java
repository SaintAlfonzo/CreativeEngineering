// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/AbstractTransformBlockEntityRender.java (MIT, see
// NOTICE-upstream-MIT.txt) to Forge 1.19.2 (axis lines and text in 2.2.0's 1.19.2 form); deviations from 2.4.1 are
// listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.yuushya.modelling.blockentity.transformData.ITransformDataProvider;
import com.yuushya.modelling.utils.YuushyaUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTransformBlockEntityRender<T extends AbstractTransformBlockEntity> implements BlockEntityRenderer<T> {
   public static final Vector3d MIDDLE = new Vector3d(8.0, 8.0, 8.0);
   public static final Vector3d _MIDDLE = new Vector3d(-8.0, -8.0, -8.0);
   protected final Font font;
   protected final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

   public AbstractTransformBlockEntityRender(Context context) {
      this.font = context.getFont();
      this.blockEntityRenderDispatcher = context.getBlockEntityRenderDispatcher();
   }

   public static void renderText(Font font, Component component, float high, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, Camera camera) {
      matrixStack.pushPose();
      // 2.4.1: new Quaternionf().rotationYXZ(-yRot, xRot, 0) in radians; Camera.rotation() is the same rotation.
      matrixStack.mulPose(camera.rotation());
      matrixStack.translate(2.0, 2.0F + high, 1.0);
      matrixStack.scale(-0.025F, -0.025F, 0.025F);
      Matrix4f matrix4f = matrixStack.last().pose();
      float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
      int backgroundColor = (int)(g * 255.0F) << 24;
      // 2.4.1: DisplayMode.SEE_THROUGH = 1.19.2's seeThrough flag true.
      font.drawInBatch(component, 0.0F, 0.0F, -1, false, matrix4f, buffer, true, backgroundColor, 15728880);
      matrixStack.popPose();
   }

   protected void renderFrame(T blockEntity, PoseStack matrixStack, MultiBufferSource multiBufferSource) {
      VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
      LevelRenderer.renderLineBox(matrixStack, vertexConsumer, -0.01, -0.01, -0.01, 1.01, 1.01, 1.01, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
      blockEntity.consumeShowFrame();
   }

   protected void renderAxes(T blockEntity, PoseStack matrixStack, MultiBufferSource multiBufferSource, ITransformDataProvider transformData) {
      this.renderAxes(blockEntity, matrixStack, multiBufferSource, transformData.getPosition(), transformData.getRotation(), transformData.getScale());
   }

   protected void renderAxes(T blockEntity, PoseStack matrixStack, MultiBufferSource multiBufferSource, Vector3d pos, Vector3f rot, Vector3f scales) {
      matrixStack.pushPose();
      Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
      float f = facing.toYRot();
      matrixStack.translate(0.5, 0.5, 0.5);
      matrixStack.mulPose(Vector3f.YP.rotationDegrees(-f));
      matrixStack.translate(-0.5, -0.5, -0.5);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferBuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
      RenderSystem.depthMask(true);
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableTexture();
      RenderSystem.lineWidth(8.0F);
      bufferBuilder.begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
      YuushyaUtils.translateAfterScale(matrixStack, pos, scales);
      YuushyaUtils.translate(matrixStack, MIDDLE);
      boolean showRotAxis = blockEntity.showRotAxis();
      int redX = 1692817990;
      int greenY = 1688263770;
      int blueZ = 1683666140;
      if (blockEntity.getShowAxis() != null) {
         switch (blockEntity.getShowAxis()) {
            case X:
               redX = -1680826;
               break;
            case Y:
               greenY = -6235046;
               break;
            case Z:
               blueZ = -10832676;
         }
      }

      if (showRotAxis) {
         matrixStack.mulPose(Vector3f.ZP.rotationDegrees(rot.z()));
      }

      bufferBuilder.vertex(matrixStack.last().pose(), 0.0F, 0.0F, -1.5F).color(blueZ).normal(0.0F, 0.0F, 1.5F).endVertex();
      bufferBuilder.vertex(matrixStack.last().pose(), 0.0F, 0.0F, 1.5F).color(blueZ).normal(0.0F, 0.0F, 1.5F).endVertex();
      if (showRotAxis) {
         matrixStack.mulPose(Vector3f.YP.rotationDegrees(rot.y()));
      }

      bufferBuilder.vertex(matrixStack.last().pose(), 0.0F, -1.5F, 0.0F).color(greenY).normal(0.0F, 1.5F, 0.0F).endVertex();
      bufferBuilder.vertex(matrixStack.last().pose(), 0.0F, 1.5F, 0.0F).color(greenY).normal(0.0F, 1.5F, 0.0F).endVertex();
      if (showRotAxis) {
         matrixStack.mulPose(Vector3f.XP.rotationDegrees(rot.x()));
      }

      bufferBuilder.vertex(matrixStack.last().pose(), -1.5F, 0.0F, 0.0F).color(redX).normal(1.5F, 0.0F, 0.0F).endVertex();
      bufferBuilder.vertex(matrixStack.last().pose(), 1.5F, 0.0F, 0.0F).color(redX).normal(1.5F, 0.0F, 0.0F).endVertex();
      tesselator.end();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      RenderSystem.enableTexture();
      matrixStack.popPose();
   }

   @Override
   public void render(T blockEntity, float tickDelta, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource multiBufferSource, int light, int overlay) {
      if (blockEntity.showFrame()) {
         this.renderFrame(blockEntity, matrixStack, multiBufferSource);
      }

      if (blockEntity.showRotAxis() || blockEntity.showPosAxis() || blockEntity.showText()) {
         this.renderSpecific(blockEntity, tickDelta, matrixStack, multiBufferSource, light, overlay);
         blockEntity.consumeShow();
         blockEntity.consumeShowAxis();
      }
   }

   protected abstract void renderSpecific(T var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6);
}
