package io.github.mortuusars.exposure.backport.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Client-only counterparts of {@link io.github.mortuusars.exposure.backport.util.Backport}. */
public final class ClientBackport {
    private ClientBackport() {
    }

    /** {@code Screen.getTooltipFromItem(minecraft, stack)} (1.20 static; 1.19.2 has it as an instance method). */
    public static List<Component> getTooltipFromItem(Minecraft minecraft, ItemStack stack) {
        return stack.getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }
}
