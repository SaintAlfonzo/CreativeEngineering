// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.rabbittrap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * The trap's item (farming_for_blockheads.md 2.3.3): 8 uses counted in the {@code TrapDamage} tag (1.19.2 has no data
 * components), shown as a durability bar; a used trap stacks to 1, a new one to 64. Tooltip: three behavior lines, the
 * uses left once used, and with Shift the catch list by name from the entity tag.
 */
public class RabbitTrapItem extends BlockItem {
	public static final String DAMAGE_TAG = "TrapDamage";

	public RabbitTrapItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static int damage(ItemStack stack) {
		return stack.hasTag() ? Mth.clamp(Objects.requireNonNull(stack.getTag()).getInt(DAMAGE_TAG), 0, RabbitTrapBlockEntity.MAX_USES) : 0;
	}

	public static ItemStack withDamage(ItemStack stack, int damage) {
		if (damage > 0) stack.getOrCreateTag().putInt(DAMAGE_TAG, damage);
		return stack;
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return damage(stack) > 0 ? 1 : super.getMaxStackSize(stack);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return damage(stack) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * (RabbitTrapBlockEntity.MAX_USES - damage(stack)) / RabbitTrapBlockEntity.MAX_USES);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float left = (RabbitTrapBlockEntity.MAX_USES - damage(stack)) / (float) RabbitTrapBlockEntity.MAX_USES;
		return Mth.hsvToRgb(Math.max(0.0F, left) / 3.0F, 1.0F, 1.0F);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		for (int i = 0; i < 3; i++)
			tooltip.add(Component.translatable("tooltip.farmingforblockheads.rabbit_trap." + i).withStyle(ChatFormatting.GRAY));
		if (damage(stack) > 0) tooltip.add(Component.translatable("tooltip.farmingforblockheads.rabbit_trap.uses",
				RabbitTrapBlockEntity.MAX_USES - damage(stack), RabbitTrapBlockEntity.MAX_USES).withStyle(ChatFormatting.GRAY));
		if (level == null || !level.isClientSide) return;
		if (!Screen.hasShiftDown()) {
			tooltip.add(Component.translatable("tooltip.farmingforblockheads.rabbit_trap.shift").withStyle(ChatFormatting.DARK_GRAY));
			return;
		}
		tooltip.add(Component.translatable("tooltip.farmingforblockheads.rabbit_trap.list").withStyle(ChatFormatting.GRAY));
		var tag = ForgeRegistries.ENTITY_TYPES.tags();
		if (tag == null) return;
		tag.getTag(RabbitTrap.CAPTURABLE).stream().map(EntityType::getDescription)
				.sorted((a, b) -> a.getString().compareToIgnoreCase(b.getString()))
				.forEach(name -> tooltip.add(Component.literal("  ").append(name).withStyle(ChatFormatting.DARK_GRAY)));
	}
}
