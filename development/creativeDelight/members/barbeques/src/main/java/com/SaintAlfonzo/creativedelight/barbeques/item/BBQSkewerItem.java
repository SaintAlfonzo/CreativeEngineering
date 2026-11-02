package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A raw or cooked skewer (1.3.0 BBQSkewerItem). A seasoning prefixes the name, rewrites the food (BBQSeasoning) and runs
 * its on-eat action. Eating returns the stick.
 */
public class BBQSkewerItem extends FoodItem {

    public BBQSkewerItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        BBQSeasoning seasoning = BBQSeasoning.get(stack);
        Component name = super.getName(stack);
        return seasoning != null ? seasoning.getTitle().append(name.copy().withStyle(seasoning.color)) : name;
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        FoodProperties ans = super.getFoodProperties(stack, entity);
        BBQSeasoning seasoning = ans == null ? null : BBQSeasoning.get(stack);
        if (seasoning == null) {
            return ans;
        }
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(ans.getNutrition()).saturationMod(ans.getSaturationModifier());
        if (ans.isMeat()) {
            builder.meat();
        }
        for (Pair<MobEffectInstance, Float> e : ans.getEffects()) {
            seasoning.appendEffect(builder, e.getFirst(), e.getSecond());
        }
        if (ans.isFastFood()) {
            builder.fast();
        }
        if (ans.canAlwaysEat()) {
            builder.alwaysEat();
        }
        seasoning.modify(builder);
        return builder.build();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        applyEffects(stack, consumer);
        ItemStack remain = stack.getCraftingRemainingItem();
        ItemStack ans = super.finishUsingItem(stack, level, consumer);
        if (remain.isEmpty()) {
            return ans;
        }
        if (ans.isEmpty()) {
            return remain;
        }
        if (consumer instanceof Player player && !player.getAbilities().instabuild) {
            player.getInventory().placeItemBackInInventory(remain);
        }
        return ans;
    }

    protected void applyEffects(ItemStack stack, LivingEntity consumer) {
        BBQSeasoning seasoning = BBQSeasoning.get(stack);
        if (seasoning != null && consumer instanceof Player player) {
            seasoning.onFinish(player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        BBQSeasoning seasoning = BBQSeasoning.get(stack);
        if (seasoning != null) {
            list.add(seasoning.getTitle());
        }
        super.appendHoverText(stack, level, list, flag);
    }
}
