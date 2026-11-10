package com.SaintAlfonzo.creativedelight.hearth.gear;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;

/** Head item, no durability (N23-18). The XP rule is on the hat tag, so it is in {@link FarmersHatEvents}, not here. */
public class FarmersHatItem extends Item implements Wearable {
    public FarmersHatItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return InteractionResultHolder.fail(held);
        }
        player.setItemSlot(EquipmentSlot.HEAD, held.copy());
        if (!level.isClientSide) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }
        held.setCount(0);
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hearthandharvest.farmers_hat").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }
}
