// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/engrave/EngraveMenu.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.engrave;

import com.yuushya.modelling.gui.AbstractEngraveMenu;
import com.yuushya.modelling.registries.MenuRegistry;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public class EngraveMenu extends AbstractEngraveMenu {
   public EngraveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
      this(containerId, playerInventory, ContainerLevelAccess.NULL);
   }

   public EngraveMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
      super(MenuRegistry.EngraveMenu.get(), containerId, playerInventory, access);
   }

   @Override
   protected List<EngraveBlockResult> getShowBlockRecipes() {
      return EngraveBlockResultLoader.SHOWBLOCK_ITEM_MAP.values().stream().toList();
   }

   @Override
   protected List<EngraveItemResult> getItemBlockRecipes() {
      return EngraveItemResultLoader.ITEMBLOCK_ITEM_MAP.values().stream().toList();
   }

   // PHASE-B: getTextBlockRecipes() -> EngraveTextResultLoader.TEXTBLOCK_ITEM_MAP.

   public MenuType<?> getType() {
      return MenuRegistry.EngraveMenu.get();
   }
}
