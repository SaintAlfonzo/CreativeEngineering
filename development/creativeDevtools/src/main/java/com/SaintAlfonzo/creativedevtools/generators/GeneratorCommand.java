package com.SaintAlfonzo.creativedevtools.generators;

import com.SaintAlfonzo.creativedevtools.CreativeDevtools;
import com.SaintAlfonzo.creativedevtools.DevtoolsConfig;
import com.SaintAlfonzo.creativedevtools.generators.GeneratorRunner.Generator;
import com.SaintAlfonzo.creativedevtools.generators.GeneratorRunner.Result;
import com.SaintAlfonzo.creativedevtools.generators.GeneratorRunner.Status;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * /cegen - runs the pack's Python generators (tools/generators.json, see {@link GeneratorRunner}) from
 * in game, on a background thread, reporting each script to chat as it finishes:
 * <pre>
 *   /cegen                        list generators, groups and the last run
 *   /cegen run [target] [dumps]   target = all | group | id (default: [debug.generators] target);
 *                                 "dumps" first runs the /dump* commands those scripts read
 *   /cegen status                 what's running, or the last run's results
 *   /cegen cancel                 kill the running script and skip the rest
 * </pre>
 * With [debug.generators] runAtStartup, the same target also runs as the game starts: kicked off in
 * common setup, waited for at FMLLoadCompleteEvent, which is before any datapack loads on both the
 * client (a world is opened later) and the dedicated server, so the first world load reads fresh files.
 * The results are posted once to the first player to join.
 */
@Mod.EventBusSubscriber(modid = CreativeDevtools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GeneratorCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Object LOCK = new Object();
    private static GeneratorRunner active;
    private static Thread activeThread;
    private static String activeLabel;
    private static long activeStart;
    private static String lastLabel;
    private static List<Result> lastResults;
    private static boolean startupReportPending;

    private GeneratorCommand() {}

    // -------------------------------------------------------------------------
    // Startup run (MOD bus listeners, added in CreativeDevtools' constructor)
    // -------------------------------------------------------------------------

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!DevtoolsConfig.DEBUG.runGeneratorsAtStartup.get()) return;
        String target = DevtoolsConfig.DEBUG.generatorTarget.get();
        String error = start("startup:" + target, target, new Reporter() {
            @Override
            public void done(List<Result> results) {
                synchronized (LOCK) {
                    startupReportPending = true;
                }
            }
        });
        if (error != null) LOGGER.warn("[cegen] startup run skipped: {}", error);
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        Thread t;
        synchronized (LOCK) {
            t = activeLabel != null && activeLabel.startsWith("startup:") ? activeThread : null;
        }
        if (t == null) return;
        int wait = DevtoolsConfig.DEBUG.startupWaitSeconds.get();
        if (wait <= 0) return;
        long started = System.currentTimeMillis();
        try {
            t.join(wait * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (t.isAlive()) {
            LOGGER.warn("[cegen] startup run still going after {}s; loading continues, files may change after the first world loads", wait);
        } else {
            LOGGER.info("[cegen] waited {} ms for the startup run", System.currentTimeMillis() - started);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null || !(server.isSingleplayer() || player.hasPermissions(2))) return;
        List<Result> results;
        String label;
        synchronized (LOCK) {
            if (!startupReportPending) return;
            startupReportPending = false;
            results = lastResults;
            label = lastLabel;
        }
        if (results == null) return;
        player.sendSystemMessage(prefix().append(Component.literal("generators ran at startup (" + label + "):").withStyle(ChatFormatting.GRAY)));
        results.forEach(r -> describe(r, player::sendSystemMessage));
        player.sendSystemMessage(summary(results));
    }

    // -------------------------------------------------------------------------
    // Command
    // -------------------------------------------------------------------------

    private static final SuggestionProvider<CommandSourceStack> TARGETS = (ctx, builder) -> {
        try {
            return SharedSuggestionProvider.suggest(GeneratorRunner.targets(GeneratorRunner.load(FMLPaths.GAMEDIR.get())), builder);
        } catch (IOException | IllegalStateException e) {
            return builder.buildFuture();
        }
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cegen")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> list(ctx.getSource()))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("cancel").executes(ctx -> cancel(ctx.getSource())))
                .then(Commands.literal("run")
                        .executes(ctx -> run(ctx.getSource(), DevtoolsConfig.DEBUG.generatorTarget.get(), false))
                        .then(Commands.argument("target", StringArgumentType.word()).suggests(TARGETS)
                                .executes(ctx -> run(ctx.getSource(), StringArgumentType.getString(ctx, "target"), false))
                                .then(Commands.literal("dumps")
                                        .executes(ctx -> run(ctx.getSource(), StringArgumentType.getString(ctx, "target"), true))))));
    }

    private static int list(CommandSourceStack source) {
        List<Generator> all;
        try {
            all = GeneratorRunner.load(FMLPaths.GAMEDIR.get());
        } catch (IOException | IllegalStateException e) {
            source.sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
        source.sendSuccess(prefix().append(Component.literal(all.size() + " generators in " + GeneratorRunner.MANIFEST
                + " (default target: " + DevtoolsConfig.DEBUG.generatorTarget.get() + ")").withStyle(ChatFormatting.GRAY)), false);
        for (Generator g : all) {
            String dumps = g.dumps().isEmpty() ? "" : "  reads /" + String.join(", /", g.dumps());
            source.sendSuccess(Component.literal("  " + g.id()).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("  [" + String.join(", ", g.groups()) + "]" + dumps).withStyle(ChatFormatting.DARK_GRAY)), false);
        }
        source.sendSuccess(Component.literal("  /cegen run <all|group|id> [dumps]").withStyle(ChatFormatting.GRAY), false);
        return all.size();
    }

    private static int status(CommandSourceStack source) {
        synchronized (LOCK) {
            if (active != null) {
                Generator cur = active.current();
                source.sendSuccess(prefix().append(Component.literal("running " + activeLabel + ": " + (cur == null ? "-" : cur.id())
                        + ", " + (System.currentTimeMillis() - activeStart) / 1000 + "s so far").withStyle(ChatFormatting.YELLOW)), false);
                return 1;
            }
            if (lastResults == null) {
                source.sendSuccess(prefix().append(Component.literal("nothing has run since the game started").withStyle(ChatFormatting.GRAY)), false);
                return 0;
            }
            source.sendSuccess(prefix().append(Component.literal("last run (" + lastLabel + "):").withStyle(ChatFormatting.GRAY)), false);
            lastResults.forEach(r -> describe(r, c -> source.sendSuccess(c, false)));
            source.sendSuccess(summary(lastResults), false);
            return lastResults.size();
        }
    }

    private static int cancel(CommandSourceStack source) {
        synchronized (LOCK) {
            if (active == null) {
                source.sendFailure(Component.literal("[cegen] nothing is running"));
                return 0;
            }
            active.cancel();
        }
        source.sendSuccess(prefix().append(Component.literal("cancelling").withStyle(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private static int run(CommandSourceStack source, String target, boolean withDumps) {
        MinecraftServer server = source.getServer();
        List<Generator> selection;
        try {
            List<Generator> all = GeneratorRunner.load(FMLPaths.GAMEDIR.get());
            selection = GeneratorRunner.select(all, target);
            if (selection.isEmpty()) {
                source.sendFailure(Component.literal("[cegen] unknown target '" + target + "'; one of: " + String.join(", ", GeneratorRunner.targets(all))));
                return 0;
            }
        } catch (IOException | IllegalStateException e) {
            source.sendFailure(Component.literal("[cegen] " + e.getMessage()));
            return 0;
        }
        synchronized (LOCK) {
            if (active != null) {
                source.sendFailure(Component.literal("[cegen] " + activeLabel + " is still running - /cegen status, /cegen cancel"));
                return 0;
            }
        }

        if (withDumps) {
            // On the server thread, before the scripts start: the dumps read live registries.
            for (String cmd : GeneratorRunner.dumps(selection)) {
                source.sendSuccess(Component.literal("[cegen] /" + cmd).withStyle(ChatFormatting.GRAY), false);
                int ok;
                try {
                    ok = server.getCommands().performPrefixedCommand(source, cmd);
                } catch (Throwable t) {
                    LOGGER.error("[cegen] /{} threw", cmd, t);
                    ok = 0;
                }
                if (ok <= 0) {
                    source.sendFailure(Component.literal("[cegen] /" + cmd + " failed; its scripts will read the previous dump"));
                }
            }
        }

        Consumer<Component> chat = c -> server.execute(() -> source.sendSuccess(c, false));
        String error = start(target, target, new Reporter() {
            @Override
            public void started(Generator gen, int index, int total) {
                chat.accept(Component.literal("[cegen] " + (index + 1) + "/" + total + " " + gen.id() + "...").withStyle(ChatFormatting.DARK_GRAY));
            }

            @Override
            public void finished(Result r) {
                describe(r, chat);
            }

            @Override
            public void done(List<Result> results) {
                chat.accept(summary(results));
            }
        });
        if (error != null) {
            source.sendFailure(Component.literal("[cegen] " + error));
            return 0;
        }
        source.sendSuccess(prefix().append(Component.literal("running " + selection.size() + " generator(s) for '" + target
                + "' in the background; full output in " + GeneratorRunner.LOG_DIR + "/").withStyle(ChatFormatting.GRAY)), false);
        return selection.size();
    }

    // -------------------------------------------------------------------------
    // Shared runner lifecycle
    // -------------------------------------------------------------------------

    private interface Reporter extends GeneratorRunner.Listener {
        default void done(List<Result> results) {}
    }

    /** Starts target on a background thread. Returns an error message, or null once started. */
    private static String start(String label, String target, Reporter reporter) {
        List<Generator> selection;
        try {
            selection = GeneratorRunner.select(GeneratorRunner.load(FMLPaths.GAMEDIR.get()), target);
        } catch (IOException | IllegalStateException e) {
            return e.getMessage();
        }
        if (selection.isEmpty()) return "unknown target '" + target + "'";

        GeneratorRunner runner = new GeneratorRunner(FMLPaths.GAMEDIR.get(), DevtoolsConfig.DEBUG.python.get(), DevtoolsConfig.DEBUG.timeoutSeconds.get());
        Thread thread = new Thread(() -> {
            List<Result> results = new ArrayList<>();
            try {
                results = runner.run(selection, reporter);
            } catch (Throwable t) {
                LOGGER.error("[cegen] {} crashed", label, t);
            } finally {
                synchronized (LOCK) {
                    active = null;
                    activeThread = null;
                    activeLabel = null;
                    lastLabel = label;
                    lastResults = List.copyOf(results);
                }
                LOGGER.info("[cegen] {} done: {}", label, summary(results).getString());
                reporter.done(results);
            }
        }, "cegen-runner");
        thread.setDaemon(true);
        synchronized (LOCK) {
            if (active != null) return activeLabel + " is still running";
            active = runner;
            activeThread = thread;
            activeLabel = label;
            activeStart = System.currentTimeMillis();
        }
        LOGGER.info("[cegen] {}: running {}", label, selection.stream().map(Generator::id).toList());
        thread.start();
        return null;
    }

    // -------------------------------------------------------------------------
    // Chat formatting
    // -------------------------------------------------------------------------

    private static final int MAX_HIGHLIGHT_LINES = 8;

    private static net.minecraft.network.chat.MutableComponent prefix() {
        return Component.literal("[cegen] ").withStyle(ChatFormatting.GOLD);
    }

    /** One result: a status line, then highlighted output, then (on failure) the output tail. */
    private static void describe(Result r, Consumer<Component> out) {
        ChatFormatting color = switch (r.status()) {
            case OK -> ChatFormatting.GREEN;
            case WARN -> ChatFormatting.YELLOW;
            default -> ChatFormatting.RED;
        };
        String state = r.status() == Status.OK ? "ok" : r.status().name().toLowerCase() + (r.exitCode() > 0 ? " (exit " + r.exitCode() + ")" : "");
        String changed = r.gen().outputs().isEmpty() ? ""
                : r.changed().isEmpty() ? " - no changes" : " - changed: " + String.join(", ", r.changed());
        out.accept(Component.literal(r.gen().id() + " ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(state).withStyle(color))
                .append(Component.literal(String.format(" %.1fs", r.seconds()) + changed).withStyle(ChatFormatting.GRAY)));
        if (!r.changed().isEmpty() && !r.gen().apply().isEmpty()) {
            out.accept(Component.literal("   apply: " + r.gen().apply()).withStyle(ChatFormatting.AQUA));
        }
        if (r.error() != null) {
            out.accept(Component.literal("   " + r.error()).withStyle(ChatFormatting.RED));
        }
        List<String> lines = r.highlight();
        for (int i = 0; i < Math.min(lines.size(), MAX_HIGHLIGHT_LINES); i++) {
            out.accept(Component.literal("   > " + lines.get(i).strip()).withStyle(ChatFormatting.YELLOW));
        }
        if (lines.size() > MAX_HIGHLIGHT_LINES) {
            out.accept(Component.literal("   ... " + (lines.size() - MAX_HIGHLIGHT_LINES) + " more in " + GeneratorRunner.LOG_DIR + "/" + r.gen().id() + ".log").withStyle(ChatFormatting.YELLOW));
        }
        if (r.status() == Status.FAILED || r.status() == Status.TIMEOUT) {
            r.tail().stream().filter(l -> !lines.contains(l))
                    .forEach(l -> out.accept(Component.literal("   | " + l).withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    private static Component summary(List<Result> results) {
        long ok = results.stream().filter(r -> r.status() == Status.OK).count();
        long warn = results.stream().filter(r -> r.status() == Status.WARN).count();
        long bad = results.size() - ok - warn;
        long changed = results.stream().filter(r -> !r.changed().isEmpty()).count();
        double secs = results.stream().mapToDouble(Result::seconds).sum();
        String text = String.format("%d ok, %d with findings, %d failed; %d changed output(s); %.1fs", ok, warn, bad, changed, secs);
        return prefix().append(Component.literal(text).withStyle(bad > 0 ? ChatFormatting.RED : warn > 0 ? ChatFormatting.YELLOW : ChatFormatting.GREEN));
    }
}
