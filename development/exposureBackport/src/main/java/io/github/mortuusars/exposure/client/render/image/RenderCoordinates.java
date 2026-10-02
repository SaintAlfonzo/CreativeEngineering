package io.github.mortuusars.exposure.client.render.image;

public record RenderCoordinates(float minX, float minY, float maxX, float maxY, float minU, float minV, float maxU, float maxV) {
   public static final RenderCoordinates DEFAULT = new RenderCoordinates(0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F);

   public RenderCoordinates(float x, float y, float width, float height) {
      this(x, y, x + width, y + height, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   public RenderCoordinates(float width, float height) {
      this(0.0F, 0.0F, width, height);
   }
}
