// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 blockentity/transformData/ITransformItemDataInventory.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.blockentity.transformData;

import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public interface ITransformItemDataInventory {
   static ITransformItemDataInventory of(List<TransformItemData> transformDatas) {
      return () -> transformDatas;
   }

   static void load(CompoundTag compoundTag, List<TransformItemData> transformDatas) {
      ListTag listTag = compoundTag.getList("Blocks", 10);
      if (!transformDatas.isEmpty()) {
         transformDatas.clear();
      }

      for (int index = 0; index < listTag.size(); index++) {
         TransformItemData transformData = new TransformItemData();
         CompoundTag compoundTagTemp = listTag.getCompound(index);
         transformData.load(compoundTagTemp);
         transformDatas.add(transformData);
      }

      if (transformDatas.isEmpty()) {
         transformDatas.add(new TransformItemData());
      }
   }

   static void saveAdditional(CompoundTag compoundTag, List<TransformItemData> transformDatas) {
      ListTag listTag = new ListTag();
      int index = 0;

      for (TransformItemData transformData : transformDatas) {
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

   static void saveAdditionalWithoutAir(CompoundTag compoundTag, List<TransformItemData> transformDatas) {
      ListTag listTag = new ListTag();
      int index = 0;

      for (TransformItemData transformData : transformDatas) {
         if (!transformData.itemStack.isEmpty()) {
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

   List<TransformItemData> getTransformData();

   default int size() {
      return this.getTransformData().size();
   }

   default boolean isEmpty() {
      Iterator var1 = this.getTransformData().iterator();
      if (var1.hasNext()) {
         TransformItemData transformData = (TransformItemData)var1.next();
         return transformData.itemStack.isEmpty();
      } else {
         return true;
      }
   }

   @NotNull
   default TransformItemData getTransformData(int slot) {
      return slot < this.size() ? this.getTransformData().get(slot) : this.getTransformData().get(Math.min(this.size() - 1, 0));
   }

   default void addTransformData(int slot, TransformItemData transformData) {
      if (slot < this.size()) {
         this.getTransformData().add(slot, transformData);
      } else {
         this.getTransformData().add(transformData);
      }
   }

   default void addTransformData(TransformItemData transformData) {
      this.getTransformData().add(transformData);
   }

   default void removeTransformData(int slot) {
      this.getTransformData(slot).set();
   }

   default void removeSlotBlockState(int slot) {
      this.getTransformData(slot).itemStack = Items.AIR.getDefaultInstance();
   }

   default void setTransformData(int slot, TransformItemData transformData) {
      this.getTransformData(slot).set(transformData);
   }

   default void setSlotBlockState(int slot, ItemStack itemStack) {
      this.getTransformData(slot).itemStack = itemStack;
   }

   default void setSlotShown(int slot, boolean isShown) {
      this.getTransformData(slot).isShown = isShown;
   }

   default void clear() {
      this.getTransformData().clear();
   }
}
