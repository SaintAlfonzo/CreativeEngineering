package net.satisfy.vinery.legacy.block;

import net.satisfy.foundation.block.FacingBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class LegacyStoveBlock extends FacingBlock {
   public LegacyStoveBlock(Properties settings) {
      super(settings);
   }

   public void appendHoverText(ItemStack itemStack, BlockGetter world, List<Component> tooltip, TooltipFlag tooltipContext) {
      tooltip.add(Component.translatable("block.vinery.stove.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
      tooltip.add(Component.translatable("block.vinery.deprecated.tooltip").withStyle(new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY}));
   }
}
