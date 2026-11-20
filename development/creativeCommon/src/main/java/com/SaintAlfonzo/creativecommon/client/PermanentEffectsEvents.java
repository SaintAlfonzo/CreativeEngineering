// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.client;

import java.util.Set;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Client-only listeners for {@link PermanentEffects} (registered from client setup). */
public final class PermanentEffectsEvents {

	private PermanentEffectsEvents() {
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		PermanentEffects.setGear(Set.of());
	}
}
