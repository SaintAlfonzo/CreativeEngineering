package com.SaintAlfonzo.creativecommon.util;

import net.minecraft.core.IdMap;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;

/**
 * Hook for a block-state sync probe, called from the client packet mixins ({@code SectionUpdateNullStateMixin},
 * {@code BlockUpdateNullStateMixin}). Creative Devtools' {@code BlockStateSyncDebug} registers the listener
 * (feature_writeup/creative_devtools.md 1); with none registered every call is a plain {@code byId}.
 * This class never references Devtools.
 */
public final class BlockStateLookupHook {

    /** What a probe sees while a block-update packet is decoded on the client. */
    public interface Listener {
        /** A state id the client could not resolve (the packet is still being decoded). */
        void unresolved(int id);

        /** The section packet is built; report what {@link #unresolved} collected for it. */
        void finishSection(ClientboundSectionBlocksUpdatePacket packet);

        /** The single-block packet is built; report what {@link #unresolved} collected for it. */
        void finishSingle(ClientboundBlockUpdatePacket packet);
    }

    private static volatile Listener listener;

    private BlockStateLookupHook() {}

    public static void register(Listener l) {
        listener = l;
    }

    /** Stand-in for {@code IdMap#byId} inside the packet constructors: same result, tells the listener about nulls. */
    public static Object lookup(IdMap<?> map, int id) {
        Object state = map.byId(id);
        Listener l = listener;
        if (state == null && l != null) l.unresolved(id);
        return state;
    }

    public static void finishSection(ClientboundSectionBlocksUpdatePacket packet) {
        Listener l = listener;
        if (l != null) l.finishSection(packet);
    }

    public static void finishSingle(ClientboundBlockUpdatePacket packet) {
        Listener l = listener;
        if (l != null) l.finishSingle(packet);
    }
}
