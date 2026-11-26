// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 utils/ClientMethod.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.utils;

import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.item.showblocktool.GetBlockStateItem;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ClientMethod {
   public static final Path ITEM_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("modellings").resolve("items");
   public static final Path BLOCK_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("modellings").resolve("blocks");
   public static final Path TEXT_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("modellings").resolve("texts");

   public static void openGuiScreen(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      ItemStack newItemStack = player.getItemInHand(InteractionHand.OFF_HAND);
      BlockEntity blockEntity = level.getBlockEntity(blockPos);
      BlockState newBlockState = null;
      if (newItemStack.getItem() instanceof GetBlockStateItem) {
         newBlockState = YuushyaDataTags.getBlockState(newItemStack);
         if (newBlockState == null) {
            newBlockState = Blocks.AIR.defaultBlockState();
         }
      } else if (newItemStack.getItem() instanceof BlockItem item) {
         newBlockState = item.getBlock().defaultBlockState();
      }

      if (!newItemStack.isEmpty()) {
         newItemStack = newItemStack.copy();
      }

      if (blockEntity instanceof ShowBlockEntity showBlockEntity) {
         // TODO(A6): Minecraft.getInstance().setScreen(new ShowBlockScreen(showBlockEntity, newBlockState));
      } else if (blockEntity instanceof ItemBlockEntity itemBlockEntity) {
         // TODO(A6): Minecraft.getInstance().setScreen(new ItemBlockScreen(itemBlockEntity, newItemStack));
      }
      // PHASE-B: else if TextBlockEntity -> new TextBlockScreen(textBlockEntity, new ArrayList<>()).
   }

   public static String getClipboard() {
      Minecraft mc = Minecraft.getInstance();
      return TextFieldHelper.getClipboardContents(mc);
   }

   public static void setClipboard(String clipboardValue) {
      Minecraft mc = Minecraft.getInstance();
      TextFieldHelper.setClipboardContents(mc, clipboardValue);
   }
}
