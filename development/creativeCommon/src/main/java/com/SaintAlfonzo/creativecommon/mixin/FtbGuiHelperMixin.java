package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.compat.FtbTooltipStacks;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** FTB Library's item-tooltip-as-text helper: remember the stack, see {@link FtbTooltipStacks}. */
@Mixin(value = GuiHelper.class, remap = false)
public class FtbGuiHelperMixin {

    @Inject(method = "addStackTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;Lnet/minecraft/network/chat/Component;)V",
            remap = false, require = 1, at = @At("HEAD"))
    private static void creativecommon$rememberStack(ItemStack stack, List<Component> list, @Nullable Component prefix, CallbackInfo ci) {
        FtbTooltipStacks.remember(stack);
    }
}
