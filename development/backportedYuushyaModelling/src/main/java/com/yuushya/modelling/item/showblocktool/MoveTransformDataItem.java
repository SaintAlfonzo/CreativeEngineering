// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/MoveTransformDataItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.item.AbstractToolItem;
import com.yuushya.modelling.utils.YuushyaDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MoveTransformDataItem extends AbstractToolItem {
   private final TransformBlockData transformData = new TransformBlockData();

   public MoveTransformDataItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockStateTarget, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(level, handItemStack);
      if (blockStateTarget.getBlock() instanceof ShowBlock) {
         ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
         BlockState blockStateShowBlock = showBlockEntity.getTransFormDataNow().blockState;
         if (blockStateShowBlock.getBlock() instanceof AirBlock) {
            player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".mainhand.pass"), true);
            return InteractionResult.PASS;
         }

         this.transformData.set(showBlockEntity.getTransFormDataNow());
         showBlockEntity.removeTransFormDataNow();
         showBlockEntity.saveChanged();
      } else {
         this.transformData.set();
         this.transformData.blockState = blockStateTarget;
      }

      this.setTag(handItemStack);
      player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".mainhand.success"), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult inOffHandRightClickOnBlock(Player player, BlockState blockStateTarget, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(level, handItemStack);
      if (this.transformData.blockState.getBlock() instanceof AirBlock) {
         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".offhand.fail"), true);
         return InteractionResult.SUCCESS;
      } else if (blockStateTarget.getBlock() instanceof ShowBlock) {
         ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
         showBlockEntity.getTransFormDataNow().set(this.transformData);
         showBlockEntity.saveChanged();
         player.displayClientMessage(Component.translatable(this.getDescriptionId() + ".offhand.success"), true);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void getTag(Level level, ItemStack itemStack) {
      CompoundTag compoundTag = YuushyaDataTags.getTransformData(itemStack);
      if (compoundTag.contains("TransformData")) {
         this.transformData.load(compoundTag.getCompound("TransformData"));
      }
   }

   public void setTag(ItemStack itemStack) {
      CompoundTag transformDataTag = new CompoundTag();
      this.transformData.saveAdditional(transformDataTag);
      CompoundTag compoundTag = new CompoundTag();
      compoundTag.put("TransformData", transformDataTag);
      YuushyaDataTags.setTransformData(itemStack, compoundTag);
   }
}
