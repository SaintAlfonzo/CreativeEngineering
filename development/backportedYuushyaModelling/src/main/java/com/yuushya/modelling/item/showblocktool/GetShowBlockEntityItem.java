// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/GetShowBlockEntityItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.item.AbstractMultiPurposeToolItem;
import com.yuushya.modelling.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GetShowBlockEntityItem extends AbstractMultiPurposeToolItem {
   private static final int GET_SHOWBLOCK = 0;
   private static final int GET_MIXEDBLOCK = 1;
   private static final int GET_ITEM = 2;
   private static final int RESTORE = 3;

   public GetShowBlockEntityItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
      this.MAX_FORMS = 4;
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      this.getTag(handItemStack);
      switch (this.getForm()) {
         case 0:
         case 1:
            if (blockState.getBlock() instanceof ShowBlock) {
               return InteractionResult.PASS;
            }

            level.setBlock(blockPos, ((Block)BlockRegistry.SHOW_BLOCK.get()).defaultBlockState(), 35);
            ShowBlockEntity showBlockEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
            showBlockEntity.setSlotBlockState(0, blockState);
            showBlockEntity.getTransformData(0).isShown = true;
            showBlockEntity.setSlot(this.getForm());
            showBlockEntity.saveChanged();
            return InteractionResult.SUCCESS;
         case 2:
            if (!(blockState.getBlock() instanceof ShowBlock)) {
               return InteractionResult.PASS;
            }

            ShowBlockEntity targetEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
            player.getInventory().placeItemBackInInventory(targetEntity.getTransFormDataNow().blockState.getBlock().asItem().getDefaultInstance());
            return InteractionResult.SUCCESS;
         case 3:
            if (!(blockState.getBlock() instanceof ShowBlock)) {
               return InteractionResult.PASS;
            }

            ShowBlockEntity restoreEntity = (ShowBlockEntity)level.getBlockEntity(blockPos);
            level.setBlock(blockPos, restoreEntity.getTransFormDataNow().blockState, 35);
            return InteractionResult.SUCCESS;
         default:
            return InteractionResult.PASS;
      }
   }
}
