package com.SaintAlfonzo.ceperfbot;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.AttributeKey;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.network.NetworkConstants;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * A connection with no client. Packets are dropped before encoding (so the suite does not measure packet encoding
 * or compression, perf_suite.md 4); send listeners still run, so a disconnect goes through the normal logout path.
 * Forge reads the channel's FML version attribute (a null one throws in {@code ConnectionType.forVersionFlag}),
 * and some mods read the remote address, so both are set.
 */
final class BotConnection extends Connection {

    BotConnection() {
        super(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel();
        // NetworkConstants.FML_NETVERSION is package-private; AttributeKey.valueOf returns the same pooled key
        channel.attr(AttributeKey.<String>valueOf("fml:netversion")).set(NetworkConstants.NETVERSION);
        setField(Channel.class, channel);
        setField(SocketAddress.class, new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
    }

    // Connection's fields are private and their names differ between dev and SRG, so they are found by type
    private void setField(Class<?> type, Object value) {
        for (Field f : Connection.class.getDeclaredFields()) {
            if (f.getType() == type) {
                try {
                    f.setAccessible(true);
                    f.set(this, value);
                    return;
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("ceperfbot: can't set Connection's " + type.getSimpleName(), e);
                }
            }
        }
        throw new IllegalStateException("ceperfbot: Connection has no " + type.getSimpleName() + " field");
    }

    @Override
    public void send(Packet<?> packet) {
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
        if (listener != null) listener.onSuccess();
    }
}
