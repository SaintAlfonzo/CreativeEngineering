package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.ChatOpenKeyGuard;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Arms {@link ChatOpenKeyGuard} when chat opens (MC-122477 stale key character). SRG {@code m_91326_} =
 * {@code Minecraft.openChatScreen(String)}, {@code remap = false}.
 */
@Mixin(value = Minecraft.class, remap = false)
public class ChatOpenArmMixin {
    @Inject(method = "m_91326_", at = @At("HEAD"))
    private void creativecommon$armChatKeyGuard(String prefix, CallbackInfo ci) {
        ChatOpenKeyGuard.arm(prefix);
    }
}
