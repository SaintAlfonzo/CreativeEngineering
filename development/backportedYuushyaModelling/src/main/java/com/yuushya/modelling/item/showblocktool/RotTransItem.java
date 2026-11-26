// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/RotTransItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Vector3f;

public class RotTransItem extends PosTransItem {
   public RotTransItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public float getDegreeAfterRotation(float degree, int mode) {
      if (mode > 0) {
         return degree + 22.5F < 360.0F ? degree + 22.5F : 0.0F;
      } else {
         return degree - 22.5F > 0.0F ? degree - 22.5F : 360.0F;
      }
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3f rot = transformData.rot;
         switch (this.getForm()) {
            case 0:
               rot.set(this.getDegreeAfterRotation(rot.x(), 1), rot.y(), rot.z());
               break;
            case 1:
               rot.set(rot.x(), this.getDegreeAfterRotation(rot.y(), 1), rot.z());
               break;
            case 2:
               rot.set(rot.x(), rot.y(), this.getDegreeAfterRotation(rot.z(), 1));
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{rot.x(), rot.y(), rot.z()}), true);
      });
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3f rot = transformData.rot;
         switch (this.getForm()) {
            case 0:
               rot.set(this.getDegreeAfterRotation(rot.x(), -1), rot.y(), rot.z());
               break;
            case 1:
               rot.set(rot.x(), this.getDegreeAfterRotation(rot.y(), -1), rot.z());
               break;
            case 2:
               rot.set(rot.x(), rot.y(), this.getDegreeAfterRotation(rot.z(), -1));
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{rot.x(), rot.y(), rot.z()}), true);
      });
   }
}
