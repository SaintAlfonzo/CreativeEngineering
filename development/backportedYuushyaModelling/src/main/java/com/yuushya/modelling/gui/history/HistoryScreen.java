// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/history/HistoryScreen.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.history;

import com.yuushya.modelling.gui.AbstractEngraveScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class HistoryScreen extends AbstractEngraveScreen<HistoryMenu> {
   public HistoryScreen(HistoryMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
   }
}
