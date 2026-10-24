package com.sammy.minersdelight.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.List;

/** The nutritional bars: their honey centre cures poison. */
public class HoneyBarItem extends ConsumableItem {
    public HoneyBarItem(Properties properties) {
        super(properties, false, true);
    }

    @Override
    public void affectConsumer(ItemStack stack, Level level, LivingEntity consumer) {
        consumer.removeEffect(MobEffects.POISON);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Configuration.FOOD_EFFECT_TOOLTIP.get()) {
            tooltip.add(Component.translatable("tooltip.minersdelight.honey_bar").withStyle(ChatFormatting.BLUE));
        }
    }
}
