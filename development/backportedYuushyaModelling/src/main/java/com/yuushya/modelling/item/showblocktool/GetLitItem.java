// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/GetLitItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.block.blockstate.YuushyaBlockStates;
import com.yuushya.modelling.item.AbstractToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class GetLitItem extends AbstractToolItem {
   public GetLitItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return transformTwoProperty(player, blockState, level, blockPos, handItemStack, YuushyaBlockStates.LIT, BlockStateProperties.LIT, player.isSecondaryUseActive());
   }

   @Override
   public InteractionResult inMainHandLeftClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return transformTwoProperty(player, blockState, level, blockPos, handItemStack, YuushyaBlockStates.LIT, BlockStateProperties.LIT, !player.isSecondaryUseActive());
   }

   private static <T extends Comparable<T>, K extends Comparable<K>> InteractionResult transformTwoProperty(
      Player player,
      BlockState blockState,
      Level level,
      BlockPos blockPos,
      ItemStack handItemStack,
      Property<T> property,
      Property<K> property2,
      boolean doGetPre
   ) {
      if (blockState.hasProperty(property)) {
         BlockState blockState2 = YuushyaBlockStates.cycleState(blockState, property, doGetPre);
         level.setBlock(blockPos, blockState2, 18);
         player.displayClientMessage(
            Component.translatable(handItemStack.getDescriptionId() + ".select", new Object[]{property.getName(), property.getName(blockState2.getValue(property))}),
            true
         );
         return InteractionResult.CONSUME;
      } else if (blockState.hasProperty(property2)) {
         BlockState blockState2 = YuushyaBlockStates.cycleState(blockState, property2, doGetPre);
         level.setBlock(blockPos, blockState2, 18);
         player.displayClientMessage(
            Component.translatable(handItemStack.getDescriptionId() + ".select", new Object[]{property2.getName(), property2.getName(blockState2.getValue(property2))}),
            true
         );
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.PASS;
      }
   }
}
