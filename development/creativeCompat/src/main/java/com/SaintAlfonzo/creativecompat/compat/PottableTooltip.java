// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * "Pottable" on every plant item a flower pot takes (blocks/flower-pots #2, owner 2026-11-22): a block item whose
 * block the vanilla pot's live map lists ({@link BotanistPot#pottedFor}, so mods' potted plants and glow lichen for
 * the botanist workbench and the double pot count too). Pot items get no line. Dark green, like the pack's
 * plantable lines ("Sapling", "Seed Item").
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, value = Dist.CLIENT)
public final class PottableTooltip {

	static final String KEY = "tooltip.creativecompat.pottable";

	private PottableTooltip() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!(stack.getItem() instanceof BlockItem item)) return;
		if (BotanistPot.pottedFor(ForgeRegistries.BLOCKS.getKey(item.getBlock())) == null) return;
		event.getToolTip().add(Component.translatable(KEY).withStyle(ChatFormatting.DARK_GREEN));
	}
}
