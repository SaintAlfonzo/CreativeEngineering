package com.SaintAlfonzo.creativecompat.melting;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * "Saltable" under the name of every block item whose block is in creativecompat:melting/saltable (plan_post_scratch_1115
 * G7.11; feature_writeup/melting.md, tooltips.md). Reads the client's copy of the block tags (synced by the server), so
 * it follows the server's tag. Gray: the general information role (tooltips.md 2, {@code col.norm}). Items in the item tag
 * creativecompat:melting/salt_items (salt dust, Salt Melt) show "Usable on ice and snow" the same way.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, value = Dist.CLIENT)
public final class SaltableTooltip {

	private SaltableTooltip() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		if (event.getItemStack().is(MeltTags.SALT_ITEMS)) {
			event.getToolTip().add(Component.translatable("creativecompat.tooltip.salt_use").withStyle(ChatFormatting.GRAY));
		}
		if (!(event.getItemStack().getItem() instanceof BlockItem item)) return;
		if (!Melting.isSaltable(item.getBlock().defaultBlockState())) return;
		event.getToolTip().add(Component.translatable("creativecompat.tooltip.saltable").withStyle(ChatFormatting.GRAY));
	}
}
