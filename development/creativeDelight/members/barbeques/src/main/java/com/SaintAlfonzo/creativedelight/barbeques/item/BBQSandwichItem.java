package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.compat.PackSeasonings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Kebab Wrap, Kebab Sandwich, Bibimbap (1.3.0 BBQSandwichItem). Since plan_meals_rolls_bbq.md C8 (2026-11-04) they are
 * plain foods made by shaped and meal recipes: their own values and effects, their own seasoning, nothing from the
 * skewers. The combine recipe's {@value #KEY} list (1.3.0's {@code content} component) is no longer read; stacks that
 * still carry it stay valid and simply ignore it (no conversion).
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
        return PackSeasonings.foodProperties(stack, super.getFoodProperties(stack, entity));
    }
}
