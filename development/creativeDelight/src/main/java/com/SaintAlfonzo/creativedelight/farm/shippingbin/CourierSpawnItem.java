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

/** The Courier Spawn Point item: one gray line on what it does (ruling D5b). */
public class CourierSpawnItem extends BlockItem {
	public CourierSpawnItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable("tooltip.farmingforblockheads.courier_spawn",
				com.SaintAlfonzo.creativedelight.CDConfig.SHIPPING_BIN.courierSpawnSearchRadius()).withStyle(ChatFormatting.GRAY));
	}
}
