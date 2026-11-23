// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.privacy.MojangReporting;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hides the "Chat messages can't be verified" toast vanilla shows on joining a server that does not enforce secure
 * chat, while {@link MojangReporting#blocked()}: the pack's server accepts unsigned chat on purpose
 * ({@code DedicatedServerUnsignedChatMixin}). The only toast in that method. feature_writeup/privacy.md 4.2.
 *
 * <p>SRG {@code m_213672_} = {@code ClientPacketListener.handleServerData}, {@code m_94922_} =
 * {@code ToastComponent.addToast}, {@code remap = false}.
 */
@Mixin(value = ClientPacketListener.class, remap = false)
public class UnsecureServerToastMixin {

	@Redirect(method = "m_213672_", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;m_94922_(Lnet/minecraft/client/gui/components/toasts/Toast;)V"))
	private void creativecommon$skipUnsecureToast(ToastComponent toasts, Toast toast) {
		if (!MojangReporting.blocked()) {
			toasts.addToast(toast);
		}
	}
}
