// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/PosTransItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.item.AbstractMultiPurposeToolItem;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

public class PosTransItem extends AbstractMultiPurposeToolItem {
   public static double getMaxPos(double scale) {
      return scale < 1.0 ? Math.ceil(24.0 / scale) - 8.0 : Math.ceil(16.0 / scale);
   }

   public static double getStep(double max) {
      return max < 16.0 ? max / 16.0 : 1.0;
   }

   public static double getUpdate(boolean increase, double pos, float scale) {
      double max = getMaxPos((double)scale);
      double perPos = getStep(max);
      if (increase) {
         double res = pos + perPos;
         return res > max ? 0.0 : res;
      } else {
         double res = pos - perPos;
         return res < -max ? 0.0 : res;
      }
   }

   public PosTransItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
      this.MAX_FORMS = 3;
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3d pos = transformData.pos;
         Vector3f scale = transformData.scales;
         switch (this.getForm()) {
            case 0:
               pos.x = getUpdate(false, pos.x, scale.x());
               break;
            case 1:
               pos.y = getUpdate(false, pos.y, scale.y());
               break;
            case 2:
               pos.z = getUpdate(false, pos.z, scale.z());
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{pos.x, pos.y, pos.z}), true);
      });
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      return translateData(player, blockState, level, blockPos, handItemStack, transformData -> {
         Vector3d pos = transformData.pos;
         Vector3f scale = transformData.scales;
         switch (this.getForm()) {
            case 0:
               pos.x = getUpdate(true, pos.x, scale.x());
               break;
            case 1:
               pos.y = getUpdate(true, pos.y, scale.y());
               break;
            case 2:
               pos.z = getUpdate(true, pos.z, scale.z());
         }

         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".switch", new Object[]{pos.x, pos.y, pos.z}), true);
      });
   }

   protected static InteractionResult translateData(
      Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack, Consumer<TransformBlockData> consumer
   ) {
      if (blockState.getBlock() instanceof ShowBlock) {
         ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
         TransformBlockData transformData = showBlockEntity.getTransFormDataNow();
         consumer.accept(transformData);
         showBlockEntity.saveChanged();
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }
}
