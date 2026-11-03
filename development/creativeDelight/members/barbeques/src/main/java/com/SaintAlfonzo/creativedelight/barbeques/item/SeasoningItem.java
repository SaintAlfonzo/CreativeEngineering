package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A seasoning item (1.3.0 SeasoningItem). Since the seasoning fold (food/seasoning #11) it is a plain stackable item:
 * a shapeless craft with a skewer consumes one and puts the pack seasoning on (Creative Delight's seasoning crafts).
 * 1.3.0's right-click on the grill or a cutting board, 1 durability per use (64 for a powder, 16 for a sauce), is retired.
 */
public class SeasoningItem extends Item {
    private final BBQSeasoning seasoning;

    public SeasoningItem(Properties properties, BBQSeasoning seasoning) {
        super(properties);
        this.seasoning = seasoning;
    }

    public BBQSeasoning getSeasoning() {
        return seasoning;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(seasoning.getInfo().withStyle(ChatFormatting.YELLOW));
        if (!seasoning.powder) {
            list.add(BBQLang.NO_SAUCE_RAW.get());
        }
        super.appendHoverText(stack, level, list, flag);
    }
}
