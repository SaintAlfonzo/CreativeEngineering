// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.fats;

import com.SaintAlfonzo.creativedelight.CDConfig;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** The Whale Oil Lamp item: shows the oil it holds ({@link WhaleOilLampBlockEntity#oilOf}). */
public class WhaleOilLampItem extends BlockItem {

	public WhaleOilLampItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable("tooltip.creativedelight.whale_oil_lamp.oil",
				WhaleOilLampBlockEntity.oilOf(stack), CDConfig.LAMP.tankMb()).withStyle(ChatFormatting.GRAY));
	}
}
