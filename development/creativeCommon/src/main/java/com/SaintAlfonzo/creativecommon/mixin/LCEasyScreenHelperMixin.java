package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.compat.LightmansTooltipStacks;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Tags every line Lightman's Currency builds for an item tooltip with its stack. See
 * {@link LightmansTooltipStacks} and {@link LCEasyGuiGraphicsMixin}.
 */
@Mixin(value = EasyScreenHelper.class, remap = false)
public class LCEasyScreenHelperMixin {

    @Inject(method = "getTooltipFromItem", remap = false, require = 1, at = @At("RETURN"))
    private static void creativecommon$tagLines(ItemStack item, CallbackInfoReturnable<List<Component>> cir) {
        LightmansTooltipStacks.tag(cir.getReturnValue(), item);
    }
}
