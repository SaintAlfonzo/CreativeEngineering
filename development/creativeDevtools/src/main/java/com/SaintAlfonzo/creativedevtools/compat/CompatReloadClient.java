package com.SaintAlfonzo.creativedevtools.compat;

import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

/** {@code /cdevreloadclient compat}: the client half of {@link CompatReloadCommand}, in its own class so a dedicated
 * server never links the client event. Registered by CreativeDevtools on a client with Creative Compat only. */
public final class CompatReloadClient {

	private CompatReloadClient() {
	}

	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("cdevreloadclient")
				.then(Commands.literal("compat").executes(c -> CompatReloadCommand.run(c.getSource()))));
	}
}
