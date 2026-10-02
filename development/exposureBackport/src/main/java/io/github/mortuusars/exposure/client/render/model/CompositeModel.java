package io.github.mortuusars.exposure.client.render.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompositeModel implements BakedModel {
   private final List<BakedModel> models;

   public CompositeModel(List<BakedModel> models) {
      this.models = models;
   }

   public CompositeModel(BakedModel... models) {
      this.models = List.of(models);
   }

   @NotNull
   public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
      ArrayList<BakedQuad> list = new ArrayList<>();

      for (BakedModel model : this.models) {
         list.addAll(model.getQuads(state, direction, random));
      }

      return list;
   }

   public boolean useAmbientOcclusion() {
      return ((BakedModel)this.models.get(0)).useAmbientOcclusion();
   }

   public boolean isGui3d() {
      return ((BakedModel)this.models.get(0)).isGui3d();
   }

   public boolean usesBlockLight() {
      return ((BakedModel)this.models.get(0)).usesBlockLight();
   }

   public boolean isCustomRenderer() {
      return ((BakedModel)this.models.get(0)).isCustomRenderer();
   }

   @NotNull
   public TextureAtlasSprite getParticleIcon() {
      return ((BakedModel)this.models.get(0)).getParticleIcon();
   }

   @NotNull
   public ItemTransforms getTransforms() {
      return ((BakedModel)this.models.get(0)).getTransforms();
   }

   @NotNull
   public ItemOverrides getOverrides() {
      return ((BakedModel)this.models.get(0)).getOverrides();
   }
}
