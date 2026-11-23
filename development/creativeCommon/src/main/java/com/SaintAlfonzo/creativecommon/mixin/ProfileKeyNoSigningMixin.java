// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.privacy.MojangReporting;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * No chat signing key while {@link MojangReporting#blocked()}, including one cached on disk from an earlier session
 * ({@code profilekeys/<uuid>.json}; vanilla uses a cached key until it is due for refresh without asking the user API,
 * so {@link MojangUserApiOfflineMixin} alone would leave it signing). With no key the client sends unsigned chat and
 * no public key at login. Also skips the "Failed to retrieve profile key pair" error the offline service would cause.
 * Same target as ModernFix's optional {@code remove_chat_signing} (an {@code @Overwrite}; this inject still applies on
 * top of it). feature_writeup/privacy.md 4.2.
 *
 * <p>SRG {@code m_243398_} = {@code ProfileKeyPairManager.readOrFetchProfileKeyPair(Optional)}, {@code remap = false}.
 */
@Mixin(value = ProfileKeyPairManager.class, remap = false)
public class ProfileKeyNoSigningMixin {

	@Inject(method = "m_243398_", at = @At("HEAD"), cancellable = true)
	private void creativecommon$noKey(Optional<?> cached, CallbackInfoReturnable<CompletableFuture<Optional<?>>> cir) {
		if (MojangReporting.blocked()) {
			cir.setReturnValue(CompletableFuture.completedFuture(Optional.empty()));
		}
	}
}
