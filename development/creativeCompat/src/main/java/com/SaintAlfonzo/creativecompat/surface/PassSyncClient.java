package com.SaintAlfonzo.creativecompat.surface;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

/** Client half of {@link PassSync}: drop the server's values on disconnect, so the client's own toml applies again. */
public final class PassSyncClient {

	private PassSyncClient() {}

	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		PassValues.setRemote(null);
	}
}
