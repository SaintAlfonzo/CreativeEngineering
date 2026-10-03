package com.SaintAlfonzo.exposure_extras.camerabag;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import io.github.mortuusars.exposure.Exposure;

/** Right-click (either hand) opens the bag. See {@link CameraBag} for storage and what it accepts. */
public class CameraBagItem extends Item {

    /** Tooltip lists this many stacks, then "and N more...". */
    private static final int TOOLTIP_LINES = 5;

    private final CameraBagTier tier;

    public CameraBagItem(CameraBagTier tier) {
        super(properties(tier));
        this.tier = tier;
    }

    private static Item.Properties properties(CameraBagTier tier) {
        Item.Properties p = new Item.Properties().tab(Exposure.CreativeTabs.EXPOSURE.get()).stacksTo(1);
        return tier.fireproof() ? p.fireResistant() : p;
    }

    public CameraBagTier tier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            int bagSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
            int size = tier.slots();
            CameraBag.spillOverflow(player, bag, size);
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, inventory, p) -> new CameraBagMenu(id, inventory, bagSlot, size), bag.getHoverName()),
                    buf -> {
                        buf.writeVarInt(bagSlot);
                        buf.writeVarInt(size);
                    });
        }
        player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.8F, 0.9F + level.random.nextFloat() * 0.2F);
        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide());
    }

    /** Burnt / despawned / voided bag: the contents fall out, like a shulker box or bundle. */
    @Override
    public void onDestroyed(ItemEntity entity) {
        ItemUtils.onContainerDestroyed(entity, CameraBag.contents(entity.getItem()).stream());
    }

    @Override
    public void appendHoverText(ItemStack bag, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<ItemStack> contents = CameraBag.contents(bag);
        tooltip.add(Component.translatable("tooltip.exposure_extras.camera_bag.slots", contents.size(), tier.slots())
                .withStyle(ChatFormatting.GRAY));
        for (int n = 0; n < contents.size() && n < TOOLTIP_LINES; n++) {
            ItemStack stack = contents.get(n);
            tooltip.add(Component.translatable("tooltip.exposure_extras.camera_bag.entry", stack.getHoverName(), stack.getCount())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (contents.size() > TOOLTIP_LINES) {
            tooltip.add(Component.translatable("container.shulkerBox.more", contents.size() - TOOLTIP_LINES)
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
