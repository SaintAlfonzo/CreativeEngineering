package com.SaintAlfonzo.solraspberrypie.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The confirm panel's content (5.4), built by the server from a copy of the food memory: diversity now and after, the
 * foods that will age out, and every bonus that will be lost (yellow = sustained by an existing flat bonus).
 */
public final class S2CPreview {
    public C2SAction.Op op;
    public int slot;
    public ResourceLocation effect;
    public int level;
    public int stamp;
    public double before;
    public double after;
    public boolean slotGreyedAfter;
    public boolean stale;
    public final List<ResourceLocation> aged = new ArrayList<>();
    public final List<Component> lines = new ArrayList<>();
    public final List<Boolean> yellow = new ArrayList<>();

    public static void encode(S2CPreview m, FriendlyByteBuf buf) {
        buf.writeEnum(m.op);
        buf.writeVarInt(m.slot);
        buf.writeBoolean(m.effect != null);
        if (m.effect != null) buf.writeResourceLocation(m.effect);
        buf.writeVarInt(m.level);
        buf.writeVarInt(m.stamp);
        buf.writeDouble(m.before);
        buf.writeDouble(m.after);
        buf.writeBoolean(m.slotGreyedAfter);
        buf.writeBoolean(m.stale);
        buf.writeVarInt(m.aged.size());
        for (ResourceLocation id : m.aged) buf.writeResourceLocation(id);
        buf.writeVarInt(m.lines.size());
        for (int i = 0; i < m.lines.size(); i++) {
            buf.writeComponent(m.lines.get(i));
            buf.writeBoolean(m.yellow.get(i));
        }
    }

    public static S2CPreview decode(FriendlyByteBuf buf) {
        S2CPreview m = new S2CPreview();
        m.op = buf.readEnum(C2SAction.Op.class);
        m.slot = buf.readVarInt();
        m.effect = buf.readBoolean() ? buf.readResourceLocation() : null;
        m.level = buf.readVarInt();
        m.stamp = buf.readVarInt();
        m.before = buf.readDouble();
        m.after = buf.readDouble();
        m.slotGreyedAfter = buf.readBoolean();
        m.stale = buf.readBoolean();
        int a = buf.readVarInt();
        for (int i = 0; i < a; i++) m.aged.add(buf.readResourceLocation());
        int l = buf.readVarInt();
        for (int i = 0; i < l; i++) {
            m.lines.add(buf.readComponent());
            m.yellow.add(buf.readBoolean());
        }
        return m;
    }

    public static void handle(S2CPreview m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.SaintAlfonzo.solraspberrypie.client.ClientState.setPreview(m)));
        ctx.get().setPacketHandled(true);
    }
}
