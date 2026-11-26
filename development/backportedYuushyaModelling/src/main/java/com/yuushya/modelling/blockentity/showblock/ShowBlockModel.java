// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/showblock/ShowBlockModel.java (MIT, see NOTICE-upstream-MIT.txt) to
// Forge 1.19.2 (UnbakedModel signatures from 2.2.0); deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.showblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.utils.YuushyaUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowBlockModel implements BakedModel, UnbakedModel {
   protected final Direction facing;
   protected final BakedModel backup;

   public ShowBlockModel(Direction facing) {
      this.facing = facing;
      this.backup = this;
   }

   public ShowBlockModel(Direction facing, BakedModel backup) {
      this.facing = facing;
      this.backup = backup;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, List<TransformBlockData> transformDatas) {
      int vertexSize = YuushyaUtils.vertexSize();
      BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
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

         for (TransformBlockData transformData : transformDatas) {
            if (transformData.isShown) {
               BlockState blockState = transformData.blockState;
               BakedModel blockModel = blockRenderDispatcher.getBlockModel(blockState);

               for (Direction value : directions) {
                  for (BakedQuad bakedQuad : blockModel.getQuads(blockState, value, rand)) {
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
                     if (bakedQuad.getTintIndex() > -1) {
                        finalQuads.add(
                           new BakedQuad(
                              vertex,
                              YuushyaUtils.encodeTintWithState(bakedQuad.getTintIndex(), blockState),
                              bakedQuad.getDirection(),
                              bakedQuad.getSprite(),
                              bakedQuad.isShade()
                           )
                        );
                     } else {
                        finalQuads.add(new BakedQuad(vertex, bakedQuad.getTintIndex(), bakedQuad.getDirection(), bakedQuad.getSprite(), bakedQuad.isShade()));
                     }
                  }
               }
            }
         }

         return finalQuads;
      }
   }

   @Override
   public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction side, RandomSource rand) {
      return this.backup != this ? this.backup.getQuads(blockState, side, rand) : Collections.emptyList();
   }

   @Override
   public boolean useAmbientOcclusion() {
      return this.backup != this ? this.backup.usesBlockLight() : false;
   }

   @Override
   public boolean isGui3d() {
      return this.backup != this ? this.backup.isGui3d() : true;
   }

   @Override
   public boolean usesBlockLight() {
      return this.backup != this ? this.backup.usesBlockLight() : false;
   }

   @Override
   public boolean isCustomRenderer() {
      return this.backup != this ? this.backup.isCustomRenderer() : false;
   }

   @NotNull
   @Override
   public TextureAtlasSprite getParticleIcon() {
      return this.backup != this ? this.backup.getParticleIcon() : Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.IRON_BLOCK.defaultBlockState()).getParticleIcon();
   }

   @NotNull
   @Override
   public ItemTransforms getTransforms() {
      return this.backup != this ? this.backup.getTransforms() : Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.IRON_BLOCK.defaultBlockState()).getTransforms();
   }

   @NotNull
   @Override
   public ItemOverrides getOverrides() {
      return this.backup != this ? this.backup.getOverrides() : ItemOverrides.EMPTY;
   }

   @Override
   public Collection<ResourceLocation> getDependencies() {
      return Collections.emptyList();
   }

   @Override
   public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
      return Collections.emptyList();
   }

   @Nullable
   @Override
   public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation location) {
      return this;
   }
}
