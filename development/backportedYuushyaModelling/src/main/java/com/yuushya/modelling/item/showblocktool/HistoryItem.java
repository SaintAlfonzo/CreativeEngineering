// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/showblocktool/HistoryItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item.showblocktool;

import com.yuushya.modelling.blockentity.itemblock.ItemBlock;
import com.yuushya.modelling.blockentity.showblock.ShowBlock;
import com.yuushya.modelling.gui.engrave.EngraveBlockResult;
import com.yuushya.modelling.gui.engrave.EngraveItemResult;
import com.yuushya.modelling.gui.history.HistoryMenu;
import com.yuushya.modelling.item.AbstractMultiPurposeToolItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HistoryItem extends AbstractMultiPurposeToolItem {
   // History List (plan R10): held in memory only (never written to disk), filled on the client when an Entity Block or
   // Item Block is removed, which includes client chunk unloads, keyed by "<dimension>/<x, y, z>". Port: upstream used
   // plain HashMaps that grew for the whole game session and were read by the integrated server thread while the
   // render thread wrote them; here each map is synchronized and keeps the HISTORY_LIMIT most recent entries.
   public static final int HISTORY_LIMIT = 256;
   public static final Map<String, EngraveBlockResult> HISTORY_SHOWBLOCK_MAP = boundedHistory();
   public static final Map<String, EngraveItemResult> HISTORY_ITEMBLOCK_ITEM_MAP = boundedHistory();
   // PHASE-B: public static final Map<String, EngraveTextResult> HISTORY_TEXTBLOCK_TEXT_MAP = boundedHistory();

   private static <V> Map<String, V> boundedHistory() {
      return Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Map.Entry<String, V> eldest) {
            return this.size() > HISTORY_LIMIT;
         }
      });
   }

   /** A copy of a history map's values, oldest first (iteration of a synchronized map must hold its lock). */
   public static <V> List<V> snapshot(Map<String, V> history) {
      synchronized (history) {
         return new ArrayList<>(history.values());
      }
   }

   public HistoryItem(Properties properties, Integer tipLines) {
      super(properties, tipLines);
   }

   public static HistoryMenu getHistoryMenu(int i, Inventory inventory, Level level, BlockPos pos, final ItemStack itemStack) {
      return new HistoryMenu(i, inventory, ContainerLevelAccess.create(level, pos)) {
         {
            this.getSlot(0).set(itemStack);
         }
      };
   }

   @Override
   public InteractionResult inMainHandRightClickOnBlock(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack offhandItem = player.getOffhandItem();
         if (offhandItem.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            // PHASE-B: also `|| block instanceof TextBlock`.
            if (block instanceof ShowBlock || block instanceof ItemBlock) {
               ItemStack itemStack = offhandItem.copy();
               if (player.isCreative()) {
                  player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
               } else {
                  player.getItemInHand(InteractionHand.OFF_HAND).setCount(0);
               }

               player.openMenu(this.getMenuProvider(level, player.blockPosition(), itemStack));
               player.awardStat(Stats.INTERACT_WITH_STONECUTTER);
               return InteractionResult.CONSUME;
            }
         }

         return InteractionResult.PASS;
      }
   }

   @Override
   public InteractionResult inMainHandRightClickInAir(Player player, BlockState blockState, Level level, BlockPos blockPos, ItemStack handItemStack) {
      return this.inMainHandRightClickOnBlock(player, blockState, level, blockPos, handItemStack);
   }

   public MenuProvider getMenuProvider(Level level, BlockPos pos, ItemStack itemStack) {
      return new SimpleMenuProvider((i, inventory, _player) -> getHistoryMenu(i, inventory, level, pos, itemStack), this.getDescription());
   }
}
