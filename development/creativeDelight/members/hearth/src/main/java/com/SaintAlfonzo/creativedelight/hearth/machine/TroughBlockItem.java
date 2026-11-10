package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Water Trough's item: the N22 tooltip line stating that water speeds up baby animals. */
public class TroughBlockItem extends BlockItem {
    public TroughBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.hearthandharvest.trough.water_growth").withStyle(ChatFormatting.GRAY));
    }
}
