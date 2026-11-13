package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.net.S2CPreview;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import net.minecraft.client.Minecraft;

/** What the server last sent: the ladder (login) and the player's diet state (every change). */
public final class ClientState {
    public static Ladder ladder;
    public static S2CState state;

    public static void setLadder(Ladder l) {
        ladder = l;
    }

    public static void setState(S2CState s) {
        state = s;
        if (Minecraft.getInstance().screen instanceof EffectScreen screen) screen.refresh();
    }

    public static void setPreview(S2CPreview p) {
        if (Minecraft.getInstance().screen instanceof EffectScreen screen) screen.showPreview(p);
    }

    public static void reset() {
        ladder = null;
        state = null;
        MessageQueue.clear();
    }

    public static boolean ready() {
        return ladder != null && state != null;
    }

    private ClientState() {}
}
