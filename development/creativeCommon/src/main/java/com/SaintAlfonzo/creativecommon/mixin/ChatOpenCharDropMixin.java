package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.ChatOpenKeyGuard;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops the stale chat-opening key character (MC-122477), see {@link ChatOpenKeyGuard}. SRG {@code m_90893_} =
 * {@code KeyboardHandler.keyPress(JIIII)V}, {@code m_90889_} = {@code charTyped(JII)V}, {@code remap = false}.
 */
@Mixin(value = KeyboardHandler.class, remap = false)
public class ChatOpenCharDropMixin {
    @Inject(method = "m_90893_", at = @At("HEAD"))
    private void creativecommon$disarmOnKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow()) {
            ChatOpenKeyGuard.onKeyPress(action);
        }
    }

    @Inject(method = "m_90889_", at = @At("HEAD"), cancellable = true)
    private void creativecommon$dropStaleChatChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow() && ChatOpenKeyGuard.shouldDrop(codePoint)) {
            ci.cancel();
        }
    }
}
