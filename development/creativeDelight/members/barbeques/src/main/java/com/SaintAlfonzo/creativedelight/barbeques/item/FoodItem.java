package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.Configuration;

import javax.annotation.Nullable;
import java.util.List;

/** 1.3.0's FoodItem: food effect lines (with chance), gated on Farmer's Delight's food effect tooltip option. */
public class FoodItem extends Item {

    public FoodItem(Properties properties) {
        super(properties);
    }

    static boolean showEffects() {
        try {
            return Configuration.FOOD_EFFECT_TOOLTIP.get();
        } catch (Throwable e) {
            return true;
        }
    }

    private static Component effectLine(MobEffectInstance eff) {
        MutableComponent ans = Component.translatable(eff.getDescriptionId());
        if (eff.getAmplifier() > 0) {
            ans = Component.translatable("potion.withAmplifier", ans, Component.translatable("potion.potency." + eff.getAmplifier()));
        }
        if (eff.getDuration() > 20) {
            ans = Component.translatable("potion.withDuration", ans, MobEffectUtil.formatDuration(eff, 1.0F));
        }
        return ans.withStyle(eff.getEffect().getCategory().getTooltipFormatting());
    }

    public static void addFoodEffects(@Nullable FoodProperties food, List<Component> list) {
        if (food == null) {
            return;
        }
        for (Pair<MobEffectInstance, Float> eff : food.getEffects()) {
            if (eff.getFirst() == null) {
                continue;
            }
            int chance = Math.round(eff.getSecond() * 100.0F);
            Component line = effectLine(eff.getFirst());
            list.add(chance >= 100 ? line : BBQLang.CHANCE_EFFECT.get(line, chance));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        if (showEffects()) {
            addFoodEffects(stack.getFoodProperties(null), list);
        }
    }
}
