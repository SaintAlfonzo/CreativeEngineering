package net.ribs.vintagedelight.item.custom;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

// From KOTS 0.3.1: ghost pepper seeds say where they can be planted (the lang text lists the pack's soils).
public class GhostPepperPlantingItem extends ItemNameBlockItem {
    public GhostPepperPlantingItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.vintagedelight.ghost_pepper.planting_soil").withStyle(ChatFormatting.GRAY));
    }
}
