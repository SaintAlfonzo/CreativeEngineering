// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/MicroPosTransItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
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
import com.mojang.math.Vector3d;

public class MicroPosTransItem extends PosTransItem {
   public MicroPosTransItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
      this.MAX_FORMS = 4;
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3d pos = transformData.pos;
         switch (this.getForm()) {
            case 0:
               pos.x -= 0.001;
               break;
            case 1:
               pos.y -= 0.001;
               break;
            case 2:
               pos.z -= 0.001;
               break;
            case 3:
               pos.set((double)(Math.round(pos.x) % 17L), (double)(Math.round(pos.y) % 17L), (double)(Math.round(pos.z) % 17L));
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{pos.x, pos.y, pos.z}), true);
      });
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3d pos = transformData.pos;
         switch (this.getForm()) {
            case 0:
               pos.x += 0.001;
               break;
            case 1:
               pos.y += 0.001;
               break;
            case 2:
               pos.z += 0.001;
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{pos.x, pos.y, pos.z}), true);
      });
   }
}
