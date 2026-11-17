package com.SaintAlfonzo.creativedevtools.compat;

import com.SaintAlfonzo.creativecompat.CompatConfigReload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * {@code /cdevreload compat} (server, permission 3) and {@code /cdevreloadclient compat} (client command, the client's
 * own copy of the config): Creative Compat's {@link CompatConfigReload#reload()}, which re-reads
 * {@code creativecompat-common.toml} and fires the readers as a watched save would. Forge's config watcher misses a
 * save by rename ({@code sed -i}, atomic-save editors) and folded writes (plan_post_scratch_1115b.md E1, E2).
 * Loaded only when Creative Compat is (CreativeDevtools checks ModList first): this class links Compat's classes.
 */
public final class CompatReloadCommand {

	private CompatReloadCommand() {
	}

	public static void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("cdevreload").requires(s -> s.hasPermission(3))
				.then(Commands.literal("compat").executes(c -> run(c.getSource()))));
	}

	static int run(CommandSourceStack src) {
		String result;
		try {
			result = CompatConfigReload.reload();
		} catch (VirtualMachineError e) {
			throw e;
		} catch (Throwable t) {
			result = "reload failed: " + t;
		}
		src.sendSuccess(Component.literal("[cdevreload] compat: " + result), true);
		return 1;
	}
}
