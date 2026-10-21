package com.SaintAlfonzo.creative_labels.net;

import com.SaintAlfonzo.creative_labels.CreativeLabels;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * {@code creative_labels:main}, protocol version "1". Packets register in {@link #register()} (called once from
 * common setup): {@link NoteTextPacket} (C2S, the note screen's Done).
 */
public final class CLNetwork {

    public static final String VERSION = "1";
    public static final ResourceLocation ID = new ResourceLocation(CreativeLabels.ID, "main");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ID, () -> VERSION,
            VERSION::equals, VERSION::equals);

    private static int nextId = 0;
    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(NoteTextPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(NoteTextPacket::encode)
                .decoder(NoteTextPacket::decode)
                .consumerMainThread(NoteTextPacket::handle)
                .add();
    }

    /** The next free packet id. */
    public static int nextId() {
        return nextId++;
    }

    private CLNetwork() {}
}
