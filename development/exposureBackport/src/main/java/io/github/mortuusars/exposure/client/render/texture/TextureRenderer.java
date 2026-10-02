package io.github.mortuusars.exposure.client.render.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix4f;

public class TextureRenderer {
   public static void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight, Color color) {
      render(poseStack, bufferSource, texture, packedLight, color.getR(), color.getG(), color.getB(), color.getA());
   }

   public static void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight, int r, int g, int b, int a) {
      render(poseStack, bufferSource, texture, 0.0F, 0.0F, 1.0F, 1.0F, packedLight, r, g, b, a);
   }

   public static void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      ResourceLocation texture,
      float x,
      float y,
      float width,
      float height,
      int packedLight,
      int r,
      int g,
      int b,
      int a
   ) {
      render(poseStack, bufferSource, texture, x, y, x + width, y + height, 0.0F, 0.0F, 1.0F, 1.0F, packedLight, r, g, b, a);
   }

   public static void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      ResourceLocation texture,
      float minX,
      float minY,
      float maxX,
      float maxY,
      float minU,
      float minV,
      float maxU,
      float maxV,
      int packedLight,
      int r,
      int g,
      int b,
      int a
   ) {
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
      RenderSystem.disableBlend();
      RenderSystem.disableDepthTest();
      Matrix4f matrix = poseStack.last().pose();
      VertexConsumer bufferBuilder = bufferSource.getBuffer(RenderType.text(texture));
      bufferBuilder.vertex(matrix, minX, maxY, 0.0F).color(r, g, b, a).uv(minU, maxV).uv2(packedLight).endVertex();
      bufferBuilder.vertex(matrix, maxX, maxY, 0.0F).color(r, g, b, a).uv(maxU, maxV).uv2(packedLight).endVertex();
      bufferBuilder.vertex(matrix, maxX, minY, 0.0F).color(r, g, b, a).uv(maxU, minV).uv2(packedLight).endVertex();
      bufferBuilder.vertex(matrix, minX, minY, 0.0F).color(r, g, b, a).uv(minU, minV).uv2(packedLight).endVertex();
   }
}
