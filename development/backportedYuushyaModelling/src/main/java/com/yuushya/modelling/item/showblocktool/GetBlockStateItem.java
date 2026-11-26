// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/GetBlockStateItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.item.AbstractToolItem;
import com.yuushya.modelling.utils.YuushyaDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GetBlockStateItem extends AbstractToolItem {
   private BlockState blockState = Blocks.AIR.defaultBlockState();

   public GetBlockStateItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   // Dropped: static renderByItem(...) (its only caller was the unused GetBlockStateItemForge; it referenced
   // client-only render types from common code). PORT_NOTES.md.

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockStateTarget, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      if (blockStateTarget.getBlock() instanceof ShowBlock) {
         ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
         BlockState blockStateShowBlock = showBlockEntity.getTransFormDataNow().blockState;
         if (blockStateShowBlock.getBlock() instanceof AirBlock) {
            player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".mainhand.pass"), true);
            return InteractionResult.PASS;
         }

         blockStateTarget = blockStateShowBlock;
         showBlockEntity.removeTransFormDataNow();
         showBlockEntity.saveChanged();
      }

      this.blockState = blockStateTarget;
      this.setTag(handItemStack);
      player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".mainhand.success"), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult inOffHandRightClickOnBlock(Player player, BlockState blockStateTarget, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      if (this.blockState.getBlock() instanceof AirBlock) {
         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".offhand.fail"), true);
         return InteractionResult.SUCCESS;
      } else if (blockStateTarget.getBlock() instanceof ShowBlock) {
         ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
         showBlockEntity.setSlotBlockStateNow(this.blockState);
         showBlockEntity.setSlotShown(showBlockEntity.getSlot(), true);
         showBlockEntity.saveChanged();
         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".offhand.success"), true);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void getTag(ItemStack itemStack) {
      this.blockState = YuushyaDataTags.getBlockState(itemStack);
      if (this.blockState == null) {
         this.blockState = Blocks.AIR.defaultBlockState();
      }
   }

   public void setTag(ItemStack itemStack) {
      YuushyaDataTags.setBlockState(itemStack, this.blockState);
   }
}
