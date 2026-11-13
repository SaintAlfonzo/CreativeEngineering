package com.SaintAlfonzo.solraspberrypie.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * One line for the overlay message queue (10). Kinds: GAINED / LOST (the "You gained/lost a bonus due to your diet"
 * line around a name), LINE (a whole line), AMOUNT (hearts or an attribute, merged with waiting lines of the same kind
 * and direction), CLEAR (drop the queue: death).
 */
public final class S2CMessage {
    public static final int GREEN = 0x55FF55, BLUE = 0x55AAFF, RED = 0xFF5555, YELLOW = 0xFFFF55;
    public enum Kind { GAINED, LOST, LINE, AMOUNT, CLEAR }

    public final Kind kind;
    public final int color;
    public final Component text;
    public final ResourceLocation attribute;
    public final double amount;

    private S2CMessage(Kind kind, int color, Component text, ResourceLocation attribute, double amount) {
        this.kind = kind;
        this.color = color;
        this.text = text;
        this.attribute = attribute;
        this.amount = amount;
    }

    public static S2CMessage gained(int color, Component name) {
        return new S2CMessage(Kind.GAINED, color, name, null, 0);
    }

    public static S2CMessage lost(Component name) {
        return new S2CMessage(Kind.LOST, RED, name, null, 0);
    }

    public static S2CMessage line(int color, Component line) {
        return new S2CMessage(Kind.LINE, color, line, null, 0);
    }

    public static S2CMessage amount(int color, ResourceLocation attribute, double signedAmount) {
        return new S2CMessage(Kind.AMOUNT, color, Component.empty(), attribute, signedAmount);
    }

    public static S2CMessage clear() {
        return new S2CMessage(Kind.CLEAR, 0, Component.empty(), null, 0);
    }

    public static void encode(S2CMessage m, FriendlyByteBuf buf) {
        buf.writeEnum(m.kind);
        buf.writeInt(m.color);
        buf.writeComponent(m.text);
        buf.writeBoolean(m.attribute != null);
        if (m.attribute != null) buf.writeResourceLocation(m.attribute);
        buf.writeDouble(m.amount);
    }

    public static S2CMessage decode(FriendlyByteBuf buf) {
        Kind kind = buf.readEnum(Kind.class);
        int color = buf.readInt();
        Component text = buf.readComponent();
        ResourceLocation attr = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new S2CMessage(kind, color, text, attr, buf.readDouble());
    }

    public static void handle(S2CMessage m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.SaintAlfonzo.solraspberrypie.client.MessageQueue.receive(m)));
        ctx.get().setPacketHandled(true);
    }
}
