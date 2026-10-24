package com.sammy.minersdelight.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.item.MilkBottleItem;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;

/** A cup of milk: drinks like Farmer's Delight's milk bottle (clears one curable effect) and shows its tooltip. */
public class MilkCupItem extends MilkBottleItem {
    public MilkCupItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Configuration.FOOD_EFFECT_TOOLTIP.get()) {
            tooltip.add(TextUtils.getTranslation("tooltip.milk_bottle").withStyle(ChatFormatting.BLUE));
        }
    }
}
