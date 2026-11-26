// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/engrave/EngraveItemResult.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.engrave;

import com.yuushya.modelling.blockentity.transformData.ITransformItemDataInventory;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
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

public class EngraveItemResult implements IEngraveResult {
   private final ItemStack resultItemStack;
   private final String name;

   public EngraveItemResult(String name, ShareUtils.ShareItemInformation itemInfo) {
      this.name = name;
      List<TransformItemData> transformDataList = new ArrayList<>();
      itemInfo.transferItems(transformDataList);
      this.resultItemStack = ((Item)ItemRegistry.ITEM_BLOCK.get()).getDefaultInstance();
      this.resultItemStack.setHoverName(Component.literal(name));
      saveItemBlockData(this.resultItemStack, transformDataList);
   }

   private static void saveItemBlockData(ItemStack itemStack, List<TransformItemData> transformDataList) {
      CompoundTag compoundTag = new CompoundTag();
      ITransformItemDataInventory.saveAdditional(compoundTag, transformDataList);
      BlockItem.setBlockEntityData(itemStack, BlockEntityRegistry.ITEM_BLOCK_ENTITY.get(), compoundTag);
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
