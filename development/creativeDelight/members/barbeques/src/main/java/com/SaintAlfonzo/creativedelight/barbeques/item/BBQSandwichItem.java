package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.compat.PackSeasonings;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kebab Wrap, Kebab Sandwich, Bibimbap (1.3.0 BBQSandwichItem). The combine recipe stores the cooked skewers in the
 * {@value #KEY} list (1.3.0's {@code content} component); the food takes each distinct effect once, from itself first, then
 * from each skewer (seasoning included), and the skewers' seasoning on-eat bonuses run too (pack seasonings since the
 * fold, food/seasoning #11). The stored skewers carry no {@code grilled} (O35: the combine recipe drops it).
 */
public class BBQSandwichItem extends FoodItem {
    public static final String KEY = "Skewers";

    public BBQSandwichItem(Properties properties) {
        super(properties);
    }

    public static List<ItemStack> getSkewers(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = tag.getList(KEY, Tag.TAG_COMPOUND);
        List<ItemStack> ans = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            ItemStack s = ItemStack.of(list.getCompound(i));
            if (!s.isEmpty()) {
                ans.add(s);
            }
        }
        return ans;
    }

    public static void setSkewers(ItemStack stack, List<ItemStack> skewers) {
        ListTag list = new ListTag();
        for (ItemStack s : skewers) {
            list.add(s.save(new CompoundTag()));
        }
        stack.getOrCreateTag().put(KEY, list);
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        FoodProperties old = PackSeasonings.foodProperties(stack, super.getFoodProperties(stack, entity));
        List<ItemStack> skewers = getSkewers(stack);
        if (old == null || skewers.isEmpty()) {
            return old;
        }
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(old.getNutrition()).saturationMod(old.getSaturationModifier());
        if (old.isFastFood()) {
            builder.fast();
        }
        if (old.canAlwaysEat()) {
            builder.alwaysEat();
        }
        Set<MobEffect> seen = new HashSet<>();
        List<FoodProperties> sources = new ArrayList<>();
        sources.add(old);
        for (ItemStack s : skewers) {
            FoodProperties x = s.getFoodProperties(entity);
            if (x != null) {
                sources.add(x);
            }
        }
        for (FoodProperties x : sources) {
            for (Pair<MobEffectInstance, Float> eff : x.getEffects()) {
                MobEffectInstance ins = eff.getFirst();
                if (seen.add(ins.getEffect())) {
                    builder.effect(() -> new MobEffectInstance(ins), eff.getSecond());
                }
            }
            if (x.isFastFood()) {
                builder.fast();
            }
            if (x.canAlwaysEat()) {
                builder.alwaysEat();
            }
        }
        return builder.build();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            for (ItemStack e : getSkewers(stack)) {
                PackSeasonings.applyEaten(player, e);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        for (ItemStack e : getSkewers(stack)) {
            list.add(e.getHoverName());
        }
        super.appendHoverText(stack, level, list, flag);
    }
}
