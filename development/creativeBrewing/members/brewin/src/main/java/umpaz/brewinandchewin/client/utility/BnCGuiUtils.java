package umpaz.brewinandchewin.client.utility;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/** Port-only: the 1.20+ GuiGraphics helpers upstream uses, written for 1.19.2's PoseStack GUI. */
public final class BnCGuiUtils {
   private BnCGuiUtils() {
   }

   /** GuiGraphics#innerBlit with a tint: a textured quad with explicit UVs, colour and alpha. */
   public static void innerBlit(PoseStack pose, ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u0, float u1, float v0, float v1,
      float red, float green, float blue, float alpha) {
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      RenderSystem.enableBlend();
      Matrix4f matrix = pose.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().getBuilder();
      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z).color(red, green, blue, alpha).uv(u0, v0).endVertex();
      buffer.vertex(matrix, (float)x1, (float)y2, (float)z).color(red, green, blue, alpha).uv(u0, v1).endVertex();
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z).color(red, green, blue, alpha).uv(u1, v1).endVertex();
      buffer.vertex(matrix, (float)x2, (float)y1, (float)z).color(red, green, blue, alpha).uv(u1, v0).endVertex();
      BufferUploader.drawWithShader(buffer.end());
      RenderSystem.disableBlend();
   }

   /** GuiGraphics#blitSprite for a stand-alone texture of the given size (1.19.2 has no GUI sprite atlas). */
   public static void blitWhole(PoseStack pose, ResourceLocation texture, int x, int y, int width, int height) {
      RenderSystem.setShaderTexture(0, texture);
      net.minecraft.client.gui.GuiComponent.blit(pose, x, y, 0, 0.0F, 0.0F, width, height, width, height);
   }

   /** "brewinandchewin:hud/heart/tipsy_full" -> "brewinandchewin:textures/gui/sprites/hud/heart/tipsy_full.png". */
   public static ResourceLocation sprite(ResourceLocation id) {
      return new ResourceLocation(id.getNamespace(), "textures/gui/sprites/" + id.getPath() + ".png");
   }
}
