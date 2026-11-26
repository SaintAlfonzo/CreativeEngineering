// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 block/AbstractYuushyaBlock.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.block;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;

public class AbstractYuushyaBlock extends Block {
   private final Integer tipLines;

   public AbstractYuushyaBlock(Properties properties, Integer tipLines) {
      super(properties);
      this.tipLines = tipLines;
   }

   public void appendHoverText(@NotNull ItemStack itemStack, BlockGetter context, @NotNull List<Component> tooltips, @NotNull TooltipFlag tooltipFlag) {
      for (int i = 1; i <= this.tipLines; i++) {
         tooltips.add(Component.translatable(this.getDescriptionId() + ".line" + i));
      }
   }
}
