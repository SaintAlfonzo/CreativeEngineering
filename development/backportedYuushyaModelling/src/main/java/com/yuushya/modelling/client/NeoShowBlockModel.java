// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 client/NeoShowBlockModel.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md (bug B2: item model cache).
package com.yuushya.modelling.client;

import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.blockentity.showblock.ShowBlockModel;
import com.yuushya.modelling.blockentity.transformData.ITransformDataInventory;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoShowBlockModel extends ShowBlockModel implements IForgeBakedModel {
   private static final ItemModelCache<NeoShowBlockModel> itemModelCache = new ItemModelCache<>();
   private static final ChunkRenderTypeSet CUTOUT_MIPPED = ChunkRenderTypeSet.of(RenderType.cutoutMipped());
   public static final ModelProperty<ShowBlockEntity> BASE_BLOCK_ENTITY = new ModelProperty<>();

   public NeoShowBlockModel(Direction facing) {
      super(facing);
   }

   public NeoShowBlockModel(Direction facing, BakedModel backup) {
      super(facing, backup);
   }

   @NotNull
   @Override
   public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
      return level.getBlockEntity(pos) instanceof ShowBlockEntity blockEntity1
         ? ModelData.builder().with(BASE_BLOCK_ENTITY, blockEntity1).build()
         : ModelData.builder().build();
   }

   @NotNull
   @Override
   public List<BakedQuad> getQuads(
      @Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType
   ) {
      ShowBlockEntity blockEntity = data.get(BASE_BLOCK_ENTITY);
      return blockEntity == null ? Collections.emptyList() : super.getQuads(state, side, rand, blockEntity.getTransformData());
   }

   @NotNull
   @Override
   public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
      CompoundTag transformDataTag = YuushyaDataTags.getTransformData(itemStack);
      return transformDataTag != null && !transformDataTag.isEmpty()
         ? List.of(itemModelCache.get(transformDataTag, () -> new NeoShowBlockModel(Direction.SOUTH) {
               private final List<TransformBlockData> transformDatas = new ArrayList<>();

               {
                  ITransformDataInventory.load(transformDataTag, this.transformDatas);
               }

               @NotNull
               @Override
               public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction side, RandomSource rand) {
                  return super.getQuads(blockState, side, rand, this.transformDatas);
               }
            }))
         : List.of(this.backup);
   }

   @Override
   public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
      return CUTOUT_MIPPED;
   }

   @Override
   public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
      return List.of(RenderType.cutoutMipped());
   }
}
