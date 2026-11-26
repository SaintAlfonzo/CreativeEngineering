// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/engrave/EngraveBlockResult.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.engrave;

import com.yuushya.modelling.blockentity.transformData.ITransformDataInventory;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.registries.BlockEntityRegistry;
import com.yuushya.modelling.registries.ItemRegistry;
import com.yuushya.modelling.utils.ShareUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EngraveBlockResult implements IEngraveResult {
   private final ItemStack resultItemStack;
   private final String name;

   public EngraveBlockResult(String name, ShareUtils.ShareBlockInformation itemInfo) {
      this.name = name;
      List<TransformBlockData> transformDataList = new ArrayList<>();
      itemInfo.transfer(transformDataList);
      this.resultItemStack = ((Item)ItemRegistry.SHOW_BLOCK.get()).getDefaultInstance();
      this.resultItemStack.setHoverName(Component.literal(name));
      saveShowBlockData(this.resultItemStack, transformDataList);
   }

   private static void saveShowBlockData(ItemStack itemStack, List<TransformBlockData> transformDataList) {
      CompoundTag compoundTag = new CompoundTag();
      ITransformDataInventory.saveAdditional(compoundTag, transformDataList);
      BlockItem.setBlockEntityData(itemStack, BlockEntityRegistry.SHOW_BLOCK_ENTITY.get(), compoundTag);
   }

   @Override
   public ItemStack getResultItem() {
      return this.resultItemStack;
   }

   @Override
   public String getName() {
      return this.name;
   }
}
