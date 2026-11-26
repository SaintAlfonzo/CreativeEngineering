// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/DestroyItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.AbstractTransformBlock;
import com.yuushya.modelling.blockentity.AbstractTransformBlockEntity;
import com.yuushya.modelling.blockentity.transformData.ITransformDataInventory;
import com.yuushya.modelling.blockentity.transformData.ITransformDataProvider;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.item.AbstractMultiPurposeToolItem;
import com.yuushya.modelling.registries.BlockEntityRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DestroyItem extends AbstractMultiPurposeToolItem {
   public DestroyItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public static void saveToItem(ItemStack itemStack, BlockState blockState) {
      List<ITransformDataProvider> transformDataList = new ArrayList<>();
      TransformBlockData data = new TransformBlockData();
      data.blockState = blockState;
      data.isShown = true;
      transformDataList.add(data);
      saveToItem(itemStack, transformDataList);
   }

   public static void saveToItem(ItemStack itemStack, List<? extends ITransformDataProvider> transformDataList) {
      CompoundTag compoundTag = new CompoundTag();
      ITransformDataInventory.saveAdditional(compoundTag, transformDataList);
      BlockItem.setBlockEntityData(itemStack, BlockEntityRegistry.SHOW_BLOCK_ENTITY.get(), compoundTag);
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      ItemStack offhandItem = player.getOffhandItem();
      if (offhandItem.getItem() instanceof BlockItem blockItem) {
         if (!(blockItem.getBlock() instanceof AbstractTransformBlock)) {
            return InteractionResult.PASS;
         } else {
            if (blockState.getBlock() instanceof AbstractTransformBlock) {
               if (level.getBlockEntity(blockPos) instanceof AbstractTransformBlockEntity showBlockEntity) {
                  showBlockEntity.saveToItem(offhandItem);
                  showBlockEntity.writeBlockState(offhandItem, blockState);
               }
            } else {
               saveToItem(offhandItem, blockState);
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      if (blockState.getBlock() instanceof AbstractTransformBlock showBlock
         && level.getBlockEntity(blockPos) instanceof AbstractTransformBlockEntity showBlockEntity
         && !level.isClientSide) {
         ItemStack itemStack = new ItemStack(showBlock);
         showBlockEntity.saveToItem(itemStack);
         showBlockEntity.writeBlockState(itemStack, blockState);
         level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 35);
         level.levelEvent(player, 2001, blockPos, Block.getId(blockState));
         if (!player.isCreative()) {
            handItemStack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(EquipmentSlot.MAINHAND));
         }

         ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack);
         itemEntity.setDefaultPickUpDelay();
         level.addFreshEntity(itemEntity);
      }

      return InteractionResult.SUCCESS;
   }
}
