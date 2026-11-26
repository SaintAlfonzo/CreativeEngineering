// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 client/NeoItemBlockModel.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md (bug B2: item model cache; Forge 43 has no
// QuadTransformers.toABGR / applyingColor, local copies below).
package com.yuushya.modelling.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockModel;
import com.yuushya.modelling.blockentity.transformData.ITransformItemDataInventory;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import com.yuushya.modelling.utils.CustomRenderInstance;
import com.yuushya.modelling.utils.YuushyaDataTags;
import com.yuushya.modelling.utils.YuushyaUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoItemBlockModel extends ItemBlockModel implements IForgeBakedModel, BakedModel {
   private static final ItemModelCache<NeoItemBlockModel> itemModelCache = new ItemModelCache<>();
   public static final ModelProperty<ItemBlockEntity> BASE_BLOCK_ENTITY = new ModelProperty<>();

   public NeoItemBlockModel(Direction facing) {
      super(facing);
   }

   public NeoItemBlockModel(Direction facing, BakedModel backup) {
      super(facing, backup);
   }

   /** Forge 1.20's QuadTransformers.toABGR (swaps red and blue; the same call converts back). */
   public static int toABGR(int argb) {
      return argb & 0xFF00FF00 | (argb & 0x00FF0000) >> 16 | (argb & 0x000000FF) << 16;
   }

   /** Forge 1.20's QuadTransformers.applyingColor(argb).processInPlace(quad). */
   public static void applyColor(BakedQuad quad, int argb) {
      int abgr = toABGR(argb);
      int[] vertices = quad.getVertices();
      for (int i = 0; i < 4; i++) {
         vertices[i * IQuadTransformer.STRIDE + IQuadTransformer.COLOR] = abgr;
      }
   }

   @NotNull
   @Override
   public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
      return level.getBlockEntity(pos) instanceof ItemBlockEntity blockEntity1
         ? ModelData.builder().with(BASE_BLOCK_ENTITY, blockEntity1).build()
         : ModelData.builder().build();
   }

   @NotNull
   @Override
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType
   ) {
      ItemBlockEntity blockEntity = data.get(BASE_BLOCK_ENTITY);
      return blockEntity == null ? Collections.emptyList() : this.getQuads(side, rand, blockEntity.getTransformData(), blockEntity.getBlockPos());
   }

   @NotNull
   @Override
   public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
      CompoundTag transformDataTag = YuushyaDataTags.getTransformData(itemStack);
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return Collections.emptyList();
      } else {
         return transformDataTag != null && !transformDataTag.isEmpty()
            ? List.of(itemModelCache.get(transformDataTag, () -> new NeoItemBlockModel(Direction.SOUTH) {
                  private final List<TransformItemData> transformDatas = new ArrayList<>();

                  {
                     ITransformItemDataInventory.load(transformDataTag, this.transformDatas);
                  }

                  @NotNull
                  @Override
                  public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction side, RandomSource rand) {
                     return this.getQuads(side, rand, this.transformDatas, null);
                  }
               }))
            : List.of(this.backup);
      }
   }

   /**
    * Item path ({@code pos == null}): the stack's items as quads. World path: registers the block for the cached
    * pipeline (client/anvilcraft/rendering) and returns no quads; the pipeline draws placed Item Blocks.
    */
   public List<BakedQuad> getQuads(@Nullable Direction side, @NotNull RandomSource rand, List<TransformItemData> transformDatas, @Nullable BlockPos pos) {
      int vertexSize = YuushyaUtils.vertexSize();
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null) {
         return Collections.emptyList();
      } else {
         ItemRenderer itemRenderer = mc.getItemRenderer();
         List<BakedQuad> finalQuads = new ArrayList<>();
         if (side != null) {
            return Collections.emptyList();
         } else {
            ArrayList<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
            directions.add(null);
            float f = this.facing.toYRot();
            PoseStack stack = new PoseStack();
            stack.translate(0.5, 0.5, 0.5);
            stack.mulPose(Vector3f.YP.rotationDegrees(-f));
            stack.translate(-0.5, -0.5, -0.5);

            for (TransformItemData transformData : transformDatas) {
               if (transformData.isShown) {
                  ItemStack itemStack = transformData.itemStack;
                  BakedModel blockModel = itemRenderer.getModel(itemStack, null, null, player.getId());

                  for (BakedModel model : blockModel.getRenderPasses(itemStack, true)) {
                     if (pos != null) {
                        ChunkPos chunkPos = new ChunkPos(pos);
                        CustomRenderInstance.getINSTANCE().addBlockPos(chunkPos, pos);
                        CustomRenderInstance.getINSTANCE().dirty = true;
                        return Collections.emptyList();
                     }

                     for (Direction value : directions) {
                        for (BakedQuad bakedQuad : model.getQuads(null, value, rand)) {
                           int[] vertex = bakedQuad.getVertices().clone();
                           stack.pushPose();
                           YuushyaUtils.scale(stack, transformData.scales);
                           YuushyaUtils.translate(stack, transformData.pos);
                           YuushyaUtils.rotate(stack, transformData.rot);

                           for (int i = 0; i < 4; i++) {
                              Vector4f vector4f = new Vector4f(
                                 Float.intBitsToFloat(vertex[vertexSize * i]),
                                 Float.intBitsToFloat(vertex[vertexSize * i + 1]),
                                 Float.intBitsToFloat(vertex[vertexSize * i + 2]),
                                 1.0F
                              );
                              vector4f.transform(stack.last().pose());
                              vertex[vertexSize * i] = Float.floatToRawIntBits(vector4f.x());
                              vertex[vertexSize * i + 1] = Float.floatToRawIntBits(vector4f.y());
                              vertex[vertexSize * i + 2] = Float.floatToRawIntBits(vector4f.z());
                           }

                           stack.popPose();
                           int color = transformData.color;
                           if (model instanceof NeoItemBlockModel) {
                              int[] vertices = bakedQuad.getVertices();
                              color = new Color(toABGR(vertices[IQuadTransformer.STRIDE + IQuadTransformer.COLOR])).getRGB();
                           }

                           BakedQuad finalQuad = new BakedQuad(
                              vertex, bakedQuad.getTintIndex(), bakedQuad.getDirection(), bakedQuad.getSprite(), bakedQuad.isShade()
                           );
                           applyColor(finalQuad, color);
                           finalQuads.add(finalQuad);
                        }
                     }
                  }
               }
            }

            return finalQuads;
         }
      }
   }
}
