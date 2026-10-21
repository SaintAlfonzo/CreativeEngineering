package com.SaintAlfonzo.creative_labels.item;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * One cursor labelling per player per {@link #TICKS} ticks, server side (sticky_notes.md 3.8, Q3): a right-click
 * drag with a note on the cursor (Mouse Tweaks) sends one click per slot within a tick or two, so only the first
 * one labels; the later ones are dropped silently and the server's slot correction resyncs the client. The client
 * is never gated (its prediction is overwritten by the server anyway).
 */
public final class LabelGate {

    public static final int TICKS = 5;

    /** Player id -> game time of the last allowed labelling. Bounded: stale entries are swept when it grows. */
    private static final Map<UUID, Long> LAST = new HashMap<>();
    private static final int SWEEP_AT = 256;

    private LabelGate() {}

    /** True when this player may label now (and records the attempt); always true on the client. */
    public static boolean allow(Player player) {
        if (player.level.isClientSide) return true;
        long now = player.level.getGameTime();
        synchronized (LAST) {
            Long last = LAST.get(player.getUUID());
            if (last != null && now - last < TICKS && now >= last) return false;
            if (LAST.size() >= SWEEP_AT) sweep(now);
            LAST.put(player.getUUID(), now);
            return true;
        }
    }

    private static void sweep(long now) {
        Iterator<Map.Entry<UUID, Long>> it = LAST.entrySet().iterator();
        while (it.hasNext()) {
            long t = it.next().getValue();
            if (now - t >= TICKS || now < t) it.remove();
        }
    }
}
