package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;

/**
 * {@code [debug.passthrough] timing} (plan_passthrough.md 7.3): the real cost of the pass-through hook in normal play.
 * While on, every call of {@link PassThrough#afterInsideBlocks} from {@code mixin/InsideBlocksMixin} is timed, and every
 * 200 ticks one line per side goes to the log:
 *
 * <pre>[passthrough-timing] side=server ticks=200 calls=51234 total_us=8123 max_us=41.2 inside=3120 inside_peak=24 counts=2890 tick_us=612345</pre>
 *
 * {@code calls}: hook runs (every entity that moved, every tick); {@code total_us} / {@code max_us}: their summed and
 * longest time; {@code inside}: entity-ticks spent in a tier block (or stuck in a web), {@code inside_peak} the most in
 * one tick; {@code counts}: neighbor counts run (ring or cube) by any pass-through code on that side; {@code tick_us}:
 * the summed tick time (START to END of the server tick, or of the client tick, which excludes rendering), so
 * {@code total_us / tick_us} is the hook's share. {@code tools/perf/passthrough_timing.py} reads the lines.
 *
 * <p>Server = the server thread (dedicated or integrated), client = the client thread; each has its own counters, so
 * single player logs both. Off: one boolean read per hook call.
 */
public final class PassTiming {

    private PassTiming() {}

    public static final String TAG = "[passthrough-timing]";
    private static final int WINDOW = 200;

    /** Mirrors the config, refreshed at every tick end (cheap to read in the hook). */
    public static volatile boolean on;

    private static final Side SERVER = new Side("server");
    private static final Side CLIENT = new Side("client");

    private static final class Side {
        final String name;
        int ticks, tickInside, insidePeak;
        long calls, totalNs, maxNs, inside, counts, tickNs, tickStart;

        Side(String name) {
            this.name = name;
        }

        void reset() {
            ticks = 0;
            tickInside = 0;
            insidePeak = 0;
            calls = 0;
            totalNs = 0;
            maxNs = 0;
            inside = 0;
            counts = 0;
            tickNs = 0;
        }
    }

    private static Side side(boolean client) {
        return client ? CLIENT : SERVER;
    }

    /** One timed hook call ({@code ns} long) for {@code e}, after it ran. */
    public static void hook(Entity e, long ns) {
        Side s = side(e.level.isClientSide);
        s.calls++;
        s.totalNs += ns;
        if (ns > s.maxNs) s.maxNs = ns;
        PassState st = PassThrough.state(e, false);
        if (st != null && (st.result.any() || st.itemStuck)) {
            s.inside++;
            s.tickInside++;
        }
    }

    /** One neighbor count ({@link PassThrough#neighbours}). Only levels know their side; others are not counted. */
    static void count(BlockGetter level) {
        if (level instanceof Level l) side(l.isClientSide).counts++;
    }

    /** Forge bus, server tick START and END. */
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        tick(SERVER, event.phase);
    }

    /** Forge bus (client only), client tick START and END. */
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        tick(CLIENT, event.phase);
    }

    private static void tick(Side s, TickEvent.Phase phase) {
        if (phase == TickEvent.Phase.START) {
            s.tickStart = on ? System.nanoTime() : 0L;
            return;
        }
        boolean was = on;
        on = CompatConfig.SPEC.isLoaded() && CompatConfig.DEBUG.passthroughTiming.get();
        if (!was) {
            s.reset();
            return;
        }
        if (s.tickStart != 0L) s.tickNs += System.nanoTime() - s.tickStart;
        if (s.tickInside > s.insidePeak) s.insidePeak = s.tickInside;
        s.tickInside = 0;
        if (++s.ticks >= WINDOW) {
            CreativeCompat.LOGGER.info("{} side={} ticks={} calls={} total_us={} max_us={} inside={} inside_peak={} counts={} tick_us={}",
                    TAG, s.name, s.ticks, s.calls, s.totalNs / 1000, String.format(java.util.Locale.ROOT, "%.1f", s.maxNs / 1000.0),
                    s.inside, s.insidePeak, s.counts, s.tickNs / 1000);
            s.reset();
        }
    }
}
