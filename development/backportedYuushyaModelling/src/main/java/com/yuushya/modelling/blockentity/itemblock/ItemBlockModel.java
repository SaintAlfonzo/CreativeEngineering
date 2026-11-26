// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/itemblock/ItemBlockModel.java (MIT, see NOTICE-upstream-MIT.txt) to
// Forge 1.19.2 (UnbakedModel signatures from 2.2.0); deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.itemblock;

import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
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

public class ItemBlockModel implements BakedModel, UnbakedModel {
   protected final Direction facing;
   protected final BakedModel backup;

   public ItemBlockModel(Direction facing) {
      this.facing = facing;
      this.backup = this;
   }

   public ItemBlockModel(Direction facing, BakedModel backup) {
      this.facing = facing;
      this.backup = backup;
   }

   public Direction getFacing() {
      return this.facing;
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
