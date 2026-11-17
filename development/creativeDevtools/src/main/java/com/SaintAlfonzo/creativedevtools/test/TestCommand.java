package com.SaintAlfonzo.creativedevtools.test;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * {@code /cdevtest <verb> [args] [key=value ...]} (permission 2): the scratch-test probe verbs in Java
 * (plan_scratch_testing_expansion.md TX-20, TX-21; creative_devtools.md section /cdevtest has the verb list).
 * Every call prints {@code [cdevtest] verb=<verb> ...} lines and ends with {@code [cdevtest] verb=<verb> done=1} (or
 * {@code error=... done=0}). Every Throwable a verb raises is caught here (a KubeJS probe's Java Error crashed the
 * server), and nothing loads a chunk. Registered by {@link com.SaintAlfonzo.creativedevtools.CreativeDevtools}.
 */
public final class TestCommand {

    @FunctionalInterface
    interface Verb {
        void run(TestCtx c) throws Exception;
    }

    /** Verb, then the bare option words it accepts (everything else is key=value). */
    private record Entry(Verb verb, String[] flags) {
    }

    private static final Map<String, Entry> VERBS = new LinkedHashMap<>();

    static void add(String name, Verb verb, String... flags) {
        VERBS.put(name, new Entry(verb, flags));
    }

    static {
        TestWorld.register();
        TestData.register();
        TestTicks.register();
        TestLayers.register();
        TestJudges.register();
        add("help", c -> {
            c.args.end();
            c.emit(c.line().kv("verbs", String.join(",", VERBS.keySet())));
        });
    }

    private TestCommand() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cdevtest").requires(s -> s.hasPermission(2))
                .then(Commands.argument("call", StringArgumentType.greedyString())
                        .suggests(TestCommand::suggest)
                        .executes(TestCommand::run)));
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> c, SuggestionsBuilder b) {
        if (b.getRemaining().contains(" ")) return b.buildFuture();
        return SharedSuggestionProvider.suggest(VERBS.keySet(), b);
    }

    private static int run(CommandContext<CommandSourceStack> c) {
        String call = StringArgumentType.getString(c, "call").trim();
        int sp = call.indexOf(' ');
        String verb = sp < 0 ? call : call.substring(0, sp);
        String rest = sp < 0 ? "" : call.substring(sp + 1);
        Entry e = VERBS.get(verb);
        TestCtx ctx = null;
        boolean ok = false;
        try {
            if (e == null) throw new TestArgs.Bad("unknown verb (try /cdevtest help)");
            ctx = new TestCtx(c.getSource(), verb, TestArgs.parse(rest, e.flags()));
            e.verb().run(ctx);
            ok = true;
        } catch (VirtualMachineError fatal) {
            throw fatal;
        } catch (Throwable t) {
            if (ctx == null) ctx = new TestCtx(c.getSource(), verb, TestArgs.parse("", new String[0]));
            String msg = t instanceof TestArgs.Bad ? t.getMessage() : t.getClass().getSimpleName() + ": " + t.getMessage();
            ctx.emit(ctx.line().kv("error", msg));
            if (!(t instanceof TestArgs.Bad)) TestCtx.LOGGER.warn("[cdevtest] {} failed", verb, t);
        }
        // async verbs (watch, tickwarp) print their own end line later; this one closes the call
        ctx.emit(ctx.line().kv("done", ok ? 1 : 0));
        return ok ? 1 : 0;
    }
}
