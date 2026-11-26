// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 item/AbstractYuushyaItem.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AbstractYuushyaItem extends Item {
   private final Integer tipLines;

   public AbstractYuushyaItem(Properties properties, Integer tipLines) {
      super(properties);
      this.tipLines = tipLines;
   }

   public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flags) {
      for (int i = 1; i <= this.tipLines; i++) {
         tooltips.add(Component.translatable(this.getDescriptionId() + ".line" + i));
      }
   }
}
