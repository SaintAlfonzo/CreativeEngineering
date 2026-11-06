package com.jkvin114.displaydelight.client.renderer;

import com.jkvin114.displaydelight.block.AbstractStackablePlatedFoodBlock;
import com.jkvin114.displaydelight.block.SmallPlatedFoodBlock;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

public class PlateHidingBakedModel extends BakedModelWrapper<BakedModel> {
   private static final Set<ResourceLocation> EXCLUDED_SPRITES = Set.of(new ResourceLocation("displaydelight", "block/wood_support_oak"));

   public PlateHidingBakedModel(BakedModel wrapped) {
      super(wrapped);
   }

   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, @Nullable RenderType renderType
   ) {
      List<BakedQuad> original = super.getQuads(state, side, rand);
      float dy = 0.0F;
      if (state == null) {
         return original;
      } else {
         Predicate<BakedQuad> quadFilter;
         if (state.getBlock() instanceof SmallPlatedFoodBlock && (Boolean)state.getValue(SmallPlatedFoodBlock.PLATE_HIDDEN)) {
            dy = -0.0625F;
            quadFilter = quad -> !this.isFromElement(quad, 4.0F, 0.0F, 4.0F, 12.0F, 1.0F, 12.0F);
         } else {
            if (!(state.getBlock() instanceof AbstractStackablePlatedFoodBlock) || !(Boolean)state.getValue(AbstractStackablePlatedFoodBlock.PLATE_HIDDEN)) {
               return original;
            }

            dy = -0.125F;
            quadFilter = quad -> !this.isFromElement(quad, 1.0F, 0.0F, 1.0F, 15.0F, 2.0F, 15.0F);
         }

         float dy2 = dy;
         return original.stream().filter(quadFilter).map(quad -> this.translateQuad(quad, dy2)).toList();
      }
   }

   private boolean isFromElement(BakedQuad quad, float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
      float minX = fromX / 16.0F;
      float minY = fromY / 16.0F;
      float minZ = fromZ / 16.0F;
      float maxX = toX / 16.0F;
      float maxY = toY / 16.0F;
      float maxZ = toZ / 16.0F;
      int[] verts = quad.getVertices();

      for (int i = 0; i < 4; i++) {
         int base = i * 8;
         float x = Float.intBitsToFloat(verts[base]);
         float y = Float.intBitsToFloat(verts[base + 1]);
         float z = Float.intBitsToFloat(verts[base + 2]);
         if (x < minX - 0.001F || x > maxX + 0.001F || y < minY - 0.001F || y > maxY + 0.001F || z < minZ - 0.001F || z > maxZ + 0.001F) {
            return false;
         }
      }

      return true;
   }

   private BakedQuad translateQuad(BakedQuad quad, float dy) {
      ResourceLocation spriteName = quad.getSprite().getName();
      if (EXCLUDED_SPRITES.contains(spriteName)) {
         return quad;
      } else {
         int[] vertices = (int[])quad.getVertices().clone();

         for (int i = 0; i < 4; i++) {
            int base = i * 8;
            float y = Float.intBitsToFloat(vertices[base + 1]);
            vertices[base + 1] = Float.floatToRawIntBits(y + dy);
         }

         return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
      }
   }
}
