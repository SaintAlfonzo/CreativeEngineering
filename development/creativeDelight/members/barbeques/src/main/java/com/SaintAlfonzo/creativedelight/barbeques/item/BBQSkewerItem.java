package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.SaintAlfonzo.creativedelight.barbeques.compat.PackSeasonings;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * A raw or cooked skewer (1.3.0 BBQSkewerItem). Eating returns the stick. Since the seasoning fold (food/seasoning #11)
 * its seasonings are the pack's ({@link PackSeasonings}): Creative Delight shows the tooltip lines and applies the
 * on-eat bonuses; this item asks the framework for the fast, always-edible and effect-transform kinds. Names (O34):
 * seasonings with a prefix (Chili Flavored, Peppered, Garlic, Salted, ...) go in front of the name; Grilled is a
 * tooltip line only.
 */
public class BBQSkewerItem extends FoodItem {
    private static final ResourceLocation GARLIC = new ResourceLocation("creativeengineering", "garlic");

    public BBQSkewerItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        MutableComponent prefix = Component.empty();
        ChatFormatting color = null;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Salted")) {
            prefix.append(BBQLang.SALTED.get());
            color = ChatFormatting.WHITE;
        }
        for (ResourceLocation id : PackSeasonings.of(stack)) {
            BBQSeasoning s = BBQSeasoning.byId(id);
            if (s != null) {
                prefix.append(s.getTitle());
                color = s.color;
            } else if (GARLIC.equals(id)) {
                prefix.append(BBQLang.GARLIC.get());
                color = color == null ? ChatFormatting.WHITE : color;
            }
        }
        return color == null ? name : prefix.append(name.copy().withStyle(color));
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return PackSeasonings.foodProperties(stack, super.getFoodProperties(stack, entity));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
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
}
