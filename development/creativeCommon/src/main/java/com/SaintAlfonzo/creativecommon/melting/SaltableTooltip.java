package com.SaintAlfonzo.creativecommon.melting;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * "Saltable" under the name of every block item whose block is in creativecommon:melting/saltable (plan_post_scratch_1115
 * G7.11; feature_writeup/melting.md, tooltips.md). Reads the client's copy of the block tags (synced by the server), so
 * it follows the server's tag. Gray: the general information role (tooltips.md 2, {@code col.norm}).
 */
@Mod.EventBusSubscriber(modid = CreativeCommon.MODID, value = Dist.CLIENT)
public final class SaltableTooltip {

	private SaltableTooltip() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		if (!(event.getItemStack().getItem() instanceof BlockItem item)) return;
		if (!Melting.isSaltable(item.getBlock().defaultBlockState())) return;
		event.getToolTip().add(Component.translatable("creativecommon.tooltip.saltable").withStyle(ChatFormatting.GRAY));
	}
}
