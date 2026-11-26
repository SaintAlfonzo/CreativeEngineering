// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/ScaleTransItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
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

public class ScaleTransItem extends PosTransItem {
   public ScaleTransItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
      this.MAX_FORMS = 1;
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3f scales = transformData.scales;
         if (this.getForm() == 0) {
            scales.add(0.1F, 0.1F, 0.1F);
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{scales.x(), scales.y(), scales.z()}), true);
      });
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3f scales = transformData.scales;
         if (this.getForm() == 0) {
            scales.add(-0.1F, -0.1F, -0.1F);
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{scales.x()}), true);
      });
   }
}
