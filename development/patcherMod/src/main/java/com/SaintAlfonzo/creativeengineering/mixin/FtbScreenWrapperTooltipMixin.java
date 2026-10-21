package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.FtbTooltipStacks;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** FTB Library plain screens (FTB Quests): hover text that holds an item's tooltip is drawn with the stack, see {@link FtbTooltipStacks}. */
@Mixin(value = ScreenWrapper.class, remap = false)
public class FtbScreenWrapperTooltipMixin {

    @Inject(method = "m_6305_", remap = false, require = 1, at = @At("HEAD"))
    private void creativeengineering$forgetStack(CallbackInfo ci) {
        FtbTooltipStacks.forget();
    }

    @Redirect(method = "m_6305_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Ldev/ftb/mods/ftblibrary/util/TooltipList;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIIILnet/minecraft/client/gui/Font;)V"))
    private void creativeengineering$renderWithStack(TooltipList list, PoseStack poseStack, int mouseX, int mouseY,
                                                      int screenWidth, int screenHeight, Font font) {
        FtbTooltipStacks.render((Screen) (Object) this, list, poseStack, mouseX, mouseY, screenWidth, screenHeight, font);
    }
}
