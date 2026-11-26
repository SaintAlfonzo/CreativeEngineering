// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/history/HistoryMenu.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.history;

import com.yuushya.modelling.gui.AbstractEngraveMenu;
import com.yuushya.modelling.gui.engrave.EngraveBlockResult;
import com.yuushya.modelling.gui.engrave.EngraveItemResult;
import com.yuushya.modelling.item.showblocktool.HistoryItem;
import com.yuushya.modelling.registries.MenuRegistry;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class HistoryMenu extends AbstractEngraveMenu {
   public HistoryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
      this(containerId, playerInventory, ContainerLevelAccess.NULL);
   }

   public HistoryMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
      super(MenuRegistry.HistoryMenu.get(), containerId, playerInventory, access);
   }

   @Override
   protected List<EngraveBlockResult> getShowBlockRecipes() {
      return HistoryItem.snapshot(HistoryItem.HISTORY_SHOWBLOCK_MAP);
   }

   @Override
   protected List<EngraveItemResult> getItemBlockRecipes() {
      return HistoryItem.snapshot(HistoryItem.HISTORY_ITEMBLOCK_ITEM_MAP);
   }

   // PHASE-B: getTextBlockRecipes() -> HistoryItem.snapshot(HistoryItem.HISTORY_TEXTBLOCK_TEXT_MAP).

   @NotNull
   @Override
   public MenuType<?> getType() {
      return MenuRegistry.HistoryMenu.get();
   }
}
