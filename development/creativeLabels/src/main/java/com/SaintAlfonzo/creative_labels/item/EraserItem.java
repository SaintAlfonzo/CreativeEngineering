package com.SaintAlfonzo.creative_labels.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@code creative_labels:eraser} (sticky_notes.md 3.6 / 3.8, BRIEF 5): stack 1, unbreakable, stays in the grid as
 * the crafting remainder. On the cursor, a right-click onto a labelled slot removes that stack's last line
 * ({@link #overrideStackedOnOther}); on placed notes and in the grid it wipes a note (FacePaperInteractions,
 * NoteEraseRecipe).
 */
public class EraserItem extends Item {

    public static final int TOOLTIP_LINES = 4;

    public EraserItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    /** The eraser on the cursor, right-clicked onto a labelled stack: its last line goes (gated like labelling). */
    @Override
    public boolean overrideStackedOnOther(ItemStack eraser, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        ItemStack target = slot.getItem();
        if (target.isEmpty() || !ItemLabels.has(target)) return false;
        if (!slot.allowModification(player)) return false;
        if (!LabelGate.allow(player)) return true;
        if (ItemLabels.eraseLast(player, target)) slot.setChanged();
        return true;
    }

    /** Spec 3.9: four grey lines, one per lang key. */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 1; i <= TOOLTIP_LINES; i++) {
            tooltip.add(Component.translatable("creative_labels.tooltip.eraser." + i).withStyle(ChatFormatting.GRAY));
        }
    }
}
