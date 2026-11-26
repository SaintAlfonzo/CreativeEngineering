// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/SlotTransItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.item.AbstractToolItem;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SlotTransItem extends AbstractToolItem {
   public SlotTransItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   @Override
   public InteractionResult inOffHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return this.translateSlot(
         player,
         blockState,
         level,
         blockPos,
         handItemStack,
         showBlockEntity -> {
            showBlockEntity.getTransFormDataNow().isShown = !showBlockEntity.getTransFormDataNow().isShown;
            player.displayClientMessage(
               Component.translatable(
                  this.getDescriptionId() + ".slot",
                  new Object[]{
                     showBlockEntity.getSlot(), showBlockEntity.getTransFormDataNow().blockState.toString(), showBlockEntity.getTransFormDataNow().isShown
                  }
               ),
               true
            );
         }
      );
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return this.translateSlot(
         player,
         blockState,
         level,
         blockPos,
         handItemStack,
         showBlockEntity -> {
            int slot = showBlockEntity.getSlot();
            if (slot == 0) {
               player.displayClientMessage(
                  Component.translatable(
                     this.getDescriptionId() + ".slot.fail.min",
                     new Object[]{showBlockEntity.getTransFormDataNow().blockState.toString(), showBlockEntity.getTransFormDataNow().isShown}
                  ),
                  true
               );
            } else {
               showBlockEntity.setSlot(slot - 1);
               player.displayClientMessage(
                  Component.translatable(
                     this.getDescriptionId() + ".slot",
                     new Object[]{
                        showBlockEntity.getSlot(), showBlockEntity.getTransFormDataNow().blockState.toString(), showBlockEntity.getTransFormDataNow().isShown
                     }
                  ),
                  true
               );
            }
         }
      );
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return this.translateSlot(
         player,
         blockState,
         level,
         blockPos,
         handItemStack,
         showBlockEntity -> {
            if (showBlockEntity.getTransFormDataNow().blockState.getBlock() instanceof AirBlock) {
               player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".slot.fail.max"), true);
            } else {
               showBlockEntity.setSlot(showBlockEntity.getSlot() + 1);
               if (showBlockEntity.getTransFormDataNow().blockState.getBlock() instanceof AirBlock) {
                  player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".slot.fail.max"), true);
               } else {
                  player.displayClientMessage(
                     Component.translatable(
                        this.getDescriptionId() + ".slot",
                        new Object[]{
                           showBlockEntity.getSlot(),
                           showBlockEntity.getTransFormDataNow().blockState.toString(),
                           showBlockEntity.getTransFormDataNow().isShown
                        }
                     ),
                     true
                  );
               }
            }
         }
      );
   }

   protected InteractionResult translateSlot(
      Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack, Consumer<ShowBlockEntity> consumer
   ) {
      if (blockState.getBlock() instanceof ShowBlock) {
         ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
         consumer.accept(showBlockEntity);
         showBlockEntity.saveChanged();
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }
}
