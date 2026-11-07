package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.FtbTooltipStacks;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.MenuScreenWrapper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** FTB Library container screens: hover text that holds an item's tooltip is drawn with the stack, see {@link FtbTooltipStacks}. */
@Mixin(value = MenuScreenWrapper.class, remap = false)
public class FtbMenuScreenWrapperTooltipMixin {

    @Inject(method = "m_7027_", remap = false, require = 1, at = @At("HEAD"))
    private void creativecommon$forgetStack(CallbackInfo ci) {
        FtbTooltipStacks.forget();
    }

    @Redirect(method = "m_7027_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Ldev/ftb/mods/ftblibrary/util/TooltipList;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIIILnet/minecraft/client/gui/Font;)V"))
    private void creativecommon$renderWithStack(TooltipList list, PoseStack poseStack, int mouseX, int mouseY,
                                                      int screenWidth, int screenHeight, Font font) {
        FtbTooltipStacks.render((Screen) (Object) this, list, poseStack, mouseX, mouseY, screenWidth, screenHeight, font);
    }
}
