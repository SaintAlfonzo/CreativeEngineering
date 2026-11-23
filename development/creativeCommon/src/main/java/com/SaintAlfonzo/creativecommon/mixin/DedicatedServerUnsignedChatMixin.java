// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The dedicated server never demands chat signing keys while {@code [privacy] acceptUnsignedChat} is on, whatever
 * {@code enforce-secure-profile} says in server.properties. The pack's clients hold no key
 * ({@code ProfileKeyNoSigningMixin}), and a server that enforces secure profiles refuses players without one.
 * Clients are told the server does not enforce secure chat. feature_writeup/privacy.md 4.2.
 *
 * <p>SRG {@code m_214005_} = {@code enforceSecureProfile()}, {@code remap = false}. Dedicated server only (the
 * integrated server never enforces).
 */
@Mixin(value = DedicatedServer.class, remap = false)
public class DedicatedServerUnsignedChatMixin {

	@Inject(method = "m_214005_", at = @At("HEAD"), cancellable = true)
	private void creativecommon$acceptUnsignedChat(CallbackInfoReturnable<Boolean> cir) {
		boolean accept;
		try {
			accept = CommonConfig.PRIVACY.acceptUnsignedChat.get();
		} catch (IllegalStateException notLoaded) {
			accept = true;
		}
		if (accept) {
			cir.setReturnValue(false);
		}
	}
}
