package com.SaintAlfonzo.creativedelight.hearth.item;

import java.util.function.Supplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/** Drinks like a milk bucket: clears all effects. Holds the goat milk fluid for tanks and pipes. */
public class GoatMilkBucketItem extends BucketItem {
    public GoatMilkBucketItem(Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
        if (entity instanceof Player player && player.getAbilities().instabuild) return stack;
        stack.shrink(1);
        return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
    }
}
