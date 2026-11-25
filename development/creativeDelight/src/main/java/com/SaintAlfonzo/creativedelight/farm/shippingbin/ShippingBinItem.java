package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/** The bin's item: two gray lines on how it sells (feature_writeup/farming_for_blockheads.md 4 step 8). */
public class ShippingBinItem extends BlockItem {
	public ShippingBinItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable("tooltip.farmingforblockheads.shipping_bin.0", ShippingBinBlockEntity.capacity())
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("tooltip.farmingforblockheads.shipping_bin.1").withStyle(ChatFormatting.GRAY));
	}
}
