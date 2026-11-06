package com.SaintAlfonzo.creativedelight.roll.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Chopsticks (roll_delight.md 3.3, redesigned): no use of their own (the off-hand food is eaten by vanilla), durability
 * 64, enchantable (Unbreaking, Mending: the BREAKABLE category takes any damageable stack-1 item), repaired with any
 * planks (D16). What they do lives in {@link com.SaintAlfonzo.creativedelight.roll.logic.Chopsticks}.
 */
public class ChopsticksItem extends Item {
    public ChopsticksItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ItemTags.PLANKS);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.roll_delight.chopsticks.tooltip.0").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.roll_delight.chopsticks.tooltip.1").withStyle(ChatFormatting.GRAY));
    }
}
