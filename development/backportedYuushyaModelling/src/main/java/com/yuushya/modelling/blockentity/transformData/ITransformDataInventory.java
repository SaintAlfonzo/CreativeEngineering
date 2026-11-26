// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/ITransformDataInventory.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface ITransformDataInventory {
   static ITransformDataInventory of(List<TransformBlockData> transformDatas) {
      return () -> transformDatas;
   }

   static void load(CompoundTag compoundTag, List<TransformBlockData> transformDatas) {
      ListTag listTag = compoundTag.getList("Blocks", 10);
      if (!transformDatas.isEmpty()) {
         transformDatas.clear();
      }

      for (int index = 0; index < listTag.size(); index++) {
         TransformBlockData transformData = new TransformBlockData();
         CompoundTag compoundTagTemp = listTag.getCompound(index);
         transformData.load(compoundTagTemp);
         transformDatas.add(transformData);
      }

      if (transformDatas.isEmpty()) {
         transformDatas.add(new TransformBlockData());
      }
   }

   static void saveAdditional(CompoundTag compoundTag, List<? extends ITransformDataProvider> transformDatas) {
      ListTag listTag = new ListTag();
      int index = 0;

      for (ITransformDataProvider transformData : transformDatas) {
         CompoundTag compoundTagTemp = new CompoundTag();
         compoundTagTemp.putByte("Slot", (byte)index);
         transformData.saveAdditional(compoundTagTemp);
         listTag.add(compoundTagTemp);
         index++;
      }

      if (!listTag.isEmpty()) {
         compoundTag.put("Blocks", listTag);
      }
   }

   static void saveAdditionalWithoutAir(CompoundTag compoundTag, List<TransformBlockData> transformDatas) {
      ListTag listTag = new ListTag();
      int index = 0;

      for (TransformBlockData transformData : transformDatas) {
         if (!(transformData.blockState.getBlock() instanceof AirBlock)) {
            CompoundTag compoundTagTemp = new CompoundTag();
            compoundTagTemp.putByte("Slot", (byte)index);
            transformData.saveAdditional(compoundTagTemp);
            listTag.add(compoundTagTemp);
         }

         index++;
      }

      if (!listTag.isEmpty()) {
         compoundTag.put("Blocks", listTag);
      }
   }

   List<TransformBlockData> getTransformData();

   default int size() {
      return this.getTransformData().size();
   }

   default boolean isEmpty() {
      for (TransformBlockData transformData : this.getTransformData()) {
         if (!(transformData.blockState.getBlock() instanceof AirBlock)) {
            return false;
         }
      }

      return true;
   }

   @NotNull
   default TransformBlockData getTransformData(int slot) {
      return slot < this.size() ? this.getTransformData().get(slot) : this.getTransformData().get(Math.min(this.size() - 1, 0));
   }

   default void addTransformData(int slot, TransformBlockData transformData) {
      if (slot < this.size()) {
         this.getTransformData().add(slot, transformData);
      } else {
         this.getTransformData().add(transformData);
      }
   }

   default void addTransformData(TransformBlockData transformData) {
      this.getTransformData().add(transformData);
   }

   default void removeTransformData(int slot) {
      this.getTransformData(slot).set();
   }

   default void removeSlotBlockState(int slot) {
      this.getTransformData(slot).blockState = Blocks.AIR.defaultBlockState();
   }

   default void setTransformData(int slot, TransformBlockData transformData) {
      this.getTransformData(slot).set(transformData);
   }

   default void setSlotBlockState(int slot, BlockState blockState) {
      this.getTransformData(slot).blockState = blockState;
   }

   default void setSlotShown(int slot, boolean isShown) {
      this.getTransformData(slot).isShown = isShown;
   }

   default void clear() {
      this.getTransformData().clear();
   }
}
