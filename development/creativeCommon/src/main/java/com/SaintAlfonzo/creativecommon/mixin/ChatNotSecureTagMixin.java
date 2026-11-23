// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.privacy.MojangReporting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While {@link MojangReporting#blocked()} nobody in the pack signs chat, so vanilla would put the gray "Not Secure"
 * bar beside every chat line. This drops that one tag (the line shows as a plain message, as signed chat does);
 * "Modified" and "Filtered" tags from the server stay. Messages are not marked secure: the trust level itself is
 * unchanged. feature_writeup/privacy.md 4.2.
 *
 * <p>SRG {@code m_240405_} = {@code ChatTrustLevel.createTag(PlayerChatMessage)}, {@code remap = false}.
 */
@Mixin(value = ChatTrustLevel.class, remap = false)
public class ChatNotSecureTagMixin {

	@Inject(method = "m_240405_", at = @At("HEAD"), cancellable = true)
	private void creativecommon$noNotSecureTag(PlayerChatMessage message, CallbackInfoReturnable<GuiMessageTag> cir) {
		if ((Object) this == ChatTrustLevel.NOT_SECURE && MojangReporting.blocked()) {
			cir.setReturnValue(null);
		}
	}
}
