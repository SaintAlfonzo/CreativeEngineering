// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.privacy.MojangReporting;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hands the game authlib's own offline user API service (the one vanilla uses when not logged in) while
 * {@link MojangReporting#blocked()}. Every call the 1.19.2 client makes to Mojang's user API goes through this one
 * object: the chat signing key fetch, abuse reports, telemetry sessions and the block list. Offline, there is no key
 * (nothing is signed, so nothing is reportable), reports and telemetry go nowhere, and the block list is empty. Login
 * and skins use the session service and keep working. feature_writeup/privacy.md 4.2.
 *
 * <p>SRG {@code m_193585_} = {@code Minecraft.createUserApiService(YggdrasilAuthenticationService, GameConfig)},
 * {@code remap = false}.
 */
@Mixin(value = Minecraft.class, remap = false)
public class MojangUserApiOfflineMixin {

	@Inject(method = "m_193585_", at = @At("HEAD"), cancellable = true)
	private void creativecommon$offlineUserApi(YggdrasilAuthenticationService auth, GameConfig config,
			CallbackInfoReturnable<UserApiService> cir) {
		MojangReporting.logState();
		if (MojangReporting.blocked()) {
			cir.setReturnValue(UserApiService.OFFLINE);
		}
	}
}
