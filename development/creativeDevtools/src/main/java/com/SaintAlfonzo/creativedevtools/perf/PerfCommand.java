package com.SaintAlfonzo.creativedevtools.perf;

import com.SaintAlfonzo.creativedevtools.CreativeDevtools;
import com.SaintAlfonzo.creativeworldgen.perf.MobCapLevels;
import com.SaintAlfonzo.creativecommon.perf.PerfEvents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * /cdevperf status: the running JFR recording, which of the pack's perf events it records, and which hooks
 * have run. /cdevperf dump [tag]: saves the recording to perf/recordings/ like server/perf/dump.sh, without
 * shell access to the server (feature_writeup/perf.md 3.6). /cdevperf mobcap: each dimension's mob cap level (perf.md 17).
 */
@Mod.EventBusSubscriber(modid = CreativeDevtools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PerfCommand {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	private static volatile boolean dumping;

	private PerfCommand() {}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("cdevperf")
				.requires(src -> src.hasPermission(3))
				.executes(ctx -> status(ctx.getSource()))
				.then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
				.then(Commands.literal("mobcap").executes(ctx -> mobcap(ctx.getSource())))
				.then(Commands.literal("dump")
						.executes(ctx -> dump(ctx.getSource(), null))
						.then(Commands.argument("tag", StringArgumentType.word())
								.executes(ctx -> dump(ctx.getSource(), StringArgumentType.getString(ctx, "tag"))))));
	}

	private static MutableComponent prefix() {
		return Component.literal("[cdevperf] ").withStyle(ChatFormatting.GOLD);
	}

	private static void say(CommandSourceStack source, String text, ChatFormatting color) {
		source.sendSuccess(prefix().append(Component.literal(text).withStyle(color)), false);
	}

	/** Per dimension: spawnable chunks and mob cap level (perf.md 17). */
	private static int mobcap(CommandSourceStack source) {
		// The mob cap levels are Creative Worldgen's (an optional dependency of Devtools).
		if (!net.minecraftforge.fml.ModList.get().isLoaded("creativeworldgen")) {
			say(source, "Creative Worldgen is not loaded", ChatFormatting.YELLOW);
			return 0;
		}
		if (MobCapLevels.states().isEmpty()) say(source, "no dimension has run natural spawning yet", ChatFormatting.YELLOW);
		MobCapLevels.states().forEach((level, s) -> say(source, level.dimension().location() + ": " + s.chunks
				+ " spawnable chunks, level " + s.level, ChatFormatting.WHITE));
		return 1;
	}

	/** The pack's recording (name "ce", from the launch scripts), else any running one. */
	private static Recording findRecording() {
		if (!FlightRecorder.isAvailable() || !FlightRecorder.isInitialized()) return null;
		Recording any = null;
		for (Recording r : FlightRecorder.getFlightRecorder().getRecordings()) {
			if (r.getState() != RecordingState.RUNNING) continue;
			if ("ce".equals(r.getName())) return r;
			if (any == null) any = r;
		}
		return any;
	}

	private static int status(CommandSourceStack source) {
		Recording r = findRecording();
		if (r == null) {
			say(source, "no JFR recording is running. The server records when started with LaunchServer.sh/.bat "
					+ "(the -XX:StartFlightRecording option in JVMARGS).", ChatFormatting.RED);
		} else {
			Duration age = r.getMaxAge();
			long size = r.getMaxSize();
			say(source, "recording '" + r.getName() + "' running since " + r.getStartTime()
					+ ", keeps " + (age == null ? "everything" : age.toMinutes() + " min")
					+ (size > 0 ? " / " + size / (1024 * 1024) + " MB" : ""), ChatFormatting.GREEN);
		}
		List<String> on = new ArrayList<>(), off = new ArrayList<>();
		for (Map.Entry<String, Boolean> e : PerfEvents.enabledEvents().entrySet()) (e.getValue() ? on : off).add(e.getKey());
		if (!on.isEmpty()) say(source, "recorded: " + String.join(", ", on), ChatFormatting.GRAY);
		if (!off.isEmpty()) say(source, "not recorded: " + String.join(", ", off), ChatFormatting.DARK_GRAY);
		List<String> missing = new ArrayList<>();
		if (!PerfEvents.hookSeen(PerfEvents.HOOK_SERVER_TICK)) missing.add("server tick");
		if (!PerfEvents.hookSeen(PerfEvents.HOOK_LEVEL_TICK)) missing.add("level tick");
		if (!PerfEvents.hookSeen(PerfEvents.HOOK_ENTITY)) missing.add("entity tick");
		if (!PerfEvents.hookSeen(PerfEvents.HOOK_BLOCK_ENTITY)) missing.add("block entity tick");
		if (!PerfEvents.hookSeen(PerfEvents.HOOK_KUBEJS)) missing.add("KubeJS handlers");
		if (!PerfEvents.hookSeen(PerfEvents.HOOK_CHUNK_WAIT)) missing.add("chunk wait (none yet, or not hooked)");
		say(source, "hooks: " + (missing.isEmpty() ? "all have run" : "not run yet: " + String.join(", ", missing))
				+ "; " + PerfEvents.handlerCount() + " KubeJS handlers wrapped", missing.isEmpty() ? ChatFormatting.GRAY : ChatFormatting.YELLOW);
		return r == null ? 0 : 1;
	}

	private static int dump(CommandSourceStack source, String tag) {
		Recording r = findRecording();
		if (r == null) {
			say(source, "no JFR recording is running, nothing to save.", ChatFormatting.RED);
			return 0;
		}
		if (dumping) {
			say(source, "a dump is already being written.", ChatFormatting.YELLOW);
			return 0;
		}
		String clean = tag == null ? "" : tag.replaceAll("[^A-Za-z0-9_-]", "");
		Path out = FMLPaths.GAMEDIR.get().resolve("perf").resolve("recordings")
				.resolve("server-" + LocalDateTime.now().format(STAMP) + (clean.isEmpty() ? "" : "-" + clean) + ".jfr");
		MinecraftServer server = source.getServer();
		dumping = true;
		say(source, "saving the recording to perf/recordings/" + out.getFileName() + " ...", ChatFormatting.GRAY);
		// Copying up to maxsize of chunk files takes a moment; keep it off the server thread.
		Thread t = new Thread(() -> {
			String msg;
			boolean ok;
			try {
				Files.createDirectories(out.getParent());
				r.dump(out);
				msg = "saved " + Files.size(out) / (1024 * 1024) + " MB to perf/recordings/" + out.getFileName()
						+ ". On the dev machine: python3 tools/perf/perf_report.py perf/recordings/" + out.getFileName();
				ok = true;
				LOGGER.info("[cdevperf] recording saved to {}", out);
			} catch (Exception e) {
				msg = "dump failed: " + e;
				ok = false;
				LOGGER.warn("[cdevperf] dump to {} failed", out, e);
			} finally {
				dumping = false;
			}
			String text = msg;
			ChatFormatting color = ok ? ChatFormatting.GREEN : ChatFormatting.RED;
			server.execute(() -> say(source, text, color));
		}, "cdevperf-dump");
		t.setDaemon(true);
		t.start();
		return 1;
	}
}
