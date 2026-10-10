package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.guititle.TitlePlaques;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stretches the GUI resource pack's title plaque to fit long container titles; see
 * {@link TitlePlaques}. Around {@code renderBg} ({@code m_7286_}) in {@code render} ({@code m_6305_}) it
 * captures the background texture and draws the stretch. In vanilla {@code renderLabels}
 * ({@code m_7027_}) it swaps in the truncated title and x when the title is wider than the whole GUI.
 * The title draw is changed with {@code @ModifyArg} (not {@code @Redirect}) so other mods can still
 * touch the same call.
 */
@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class ContainerTitlePlaqueMixin {

    private static final String RENDER_BG =
            "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;m_7286_(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V";
    private static final String FONT_DRAW =
            "Lnet/minecraft/client/gui/Font;m_92889_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I";

    @Unique
    private TitlePlaques.Label creativeengineering$titleLabel;

    @Inject(method = "m_6305_", remap = false, require = 1,
            at = @At(value = "INVOKE", target = RENDER_BG, remap = false))
    private void creativeengineering$beforeBackground(PoseStack pose, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TitlePlaques.beginCapture();
    }

    @Inject(method = "m_6305_", remap = false, require = 1,
            at = @At(value = "INVOKE", target = RENDER_BG, shift = At.Shift.AFTER, remap = false))
    private void creativeengineering$afterBackground(PoseStack pose, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        creativeengineering$titleLabel = TitlePlaques.afterBackground((AbstractContainerScreen<?>) (Object) this, pose);
    }

    @ModifyArg(method = "m_7027_", remap = false, require = 1, index = 1,
            at = @At(value = "INVOKE", target = FONT_DRAW, ordinal = 0, remap = false))
    private Component creativeengineering$titleText(Component text) {
        TitlePlaques.Label label = creativeengineering$titleLabel;
        return label != null ? label.text() : text;
    }

    @ModifyArg(method = "m_7027_", remap = false, require = 1, index = 2,
            at = @At(value = "INVOKE", target = FONT_DRAW, ordinal = 0, remap = false))
    private float creativeengineering$titleX(float x) {
        TitlePlaques.Label label = creativeengineering$titleLabel;
        return label != null ? label.x() : x;
    }
}
