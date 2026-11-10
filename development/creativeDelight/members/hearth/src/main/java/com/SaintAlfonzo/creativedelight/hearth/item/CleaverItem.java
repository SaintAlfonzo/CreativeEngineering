package com.SaintAlfonzo.creativedelight.hearth.item;

import com.SaintAlfonzo.creativedelight.hearth.entity.ThrownCleaver;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.item.KnifeItem;
import vectorwing.farmersdelight.common.registry.ModEnchantments;

/** A knife that butchers (CleaverEvents) and can be thrown. Crafting with it costs 1 durability. */
public class CleaverItem extends KnifeItem {
    public CleaverItem(Tier tier, Properties properties) {
        super(tier, 2.0F, -3.0F, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hearthandharvest.cleaver.butchering").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == ModEnchantments.BACKSTABBING.get()) return false;
        return enchantment == Enchantments.LOYALTY || super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        remainder.setCount(1);
        remainder.setDamageValue(remainder.getDamageValue() + 1);
        return remainder.getDamageValue() >= remainder.getMaxDamage() ? ItemStack.EMPTY : remainder;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ThrowableToolItem.startCharge(player, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        ThrowableToolItem.release(stack, level, entity, getUseDuration(stack) - timeLeft, 5, 2.0F, SoundEvents.TRIDENT_THROW, 1.2F, ThrownCleaver::new);
    }
}
