package com.SaintAlfonzo.creativedelight.delightful.item;

import com.SaintAlfonzo.creativedelight.delightful.DelightfulBackportTiers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.util.List;

/** Fluix Knife (3.8.1 FluixKnifeItem): always has at least Looting I. */
public class FluixKnifeItem extends KnifeItem {

	public FluixKnifeItem(Properties properties) {
		super(DelightfulBackportTiers.FLUIX, 0.5F, -2.0F, properties);
	}

	@Override
	public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
		int level = super.getEnchantmentLevel(stack, enchantment);
		return enchantment == Enchantments.MOB_LOOTING ? Math.max(1, level) : level;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.delightful.fluix_knife.tooltip", Enchantments.MOB_LOOTING.getFullname(1))
				.withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, level, tooltip, flag);
	}
}
