package com.SaintAlfonzo.creativelabels.item;

import com.SaintAlfonzo.creativelabels.block.FacePaperInteractions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@code creativelabels:label} (sticky_notes.md 3.10): a blank label; placed on a five-spot face it shows an item
 * picture. The item itself never carries data (labels drop blank).
 */
public class LabelItem extends Item {

    public static final int TOOLTIP_LINES = 8;

    public LabelItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    /** Placement (3.10): an empty five-spot face at the boundary or within the inset limit. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return FacePaperInteractions.placeFromContext(context);
    }

    /** Spec 3.9: eight grey lines, one per lang key. */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 1; i <= TOOLTIP_LINES; i++) {
            tooltip.add(Component.translatable("creativelabels.tooltip.label." + i).withStyle(ChatFormatting.GRAY));
        }
    }
}
