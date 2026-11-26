// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/AbstractMultiPurposeToolItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item;

import com.yuushya.modelling.utils.YuushyaDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AbstractMultiPurposeToolItem extends AbstractToolItem {
   protected int MAX_FORMS;
   private int _form_now = 0;

   public AbstractMultiPurposeToolItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public int getForm() {
      return this._form_now;
   }

   public void changeForm() {
      if (++this._form_now >= this.MAX_FORMS) {
         this._form_now = 0;
      }
   }

   public void getTag(ItemStack itemStack) {
      this._form_now = YuushyaDataTags.getTransDirection(itemStack);
   }

   public void setTag(ItemStack itemStack) {
      YuushyaDataTags.setTransDirection(itemStack, this._form_now);
   }

   @Override
   public InteractionResult inOffHandRightClickInAir(Player player, BlockState blockStateTarget, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      this.changeForm();
      this.setTag(handItemStack);
      player.displayClientMessage(Component.translatable(this.getDescriptionId() + "." + this._form_now), true);
      return InteractionResult.PASS;
   }
}
