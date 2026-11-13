package com.SaintAlfonzo.solraspberrypie.net;

import com.SaintAlfonzo.solraspberrypie.logic.SlotActions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** A slot action from the effect screen (5.2). PREVIEW_* asks for a confirm panel; CONFIRM_* carries its state stamp. */
public final class C2SAction {
    public enum Op { FILL, LEVEL, PREVIEW_CHANGE, PREVIEW_DROP, CONFIRM_CHANGE, CONFIRM_DROP }

    public final Op op;
    public final int slot;
    public final ResourceLocation effect;
    public final int level;
    public final int stamp;

    public C2SAction(Op op, int slot, ResourceLocation effect, int level, int stamp) {
        this.op = op;
        this.slot = slot;
        this.effect = effect;
        this.level = level;
        this.stamp = stamp;
    }

    public static void encode(C2SAction m, FriendlyByteBuf buf) {
        buf.writeEnum(m.op);
        buf.writeVarInt(m.slot);
        buf.writeBoolean(m.effect != null);
        if (m.effect != null) buf.writeResourceLocation(m.effect);
        buf.writeVarInt(m.level);
        buf.writeVarInt(m.stamp);
    }

    public static C2SAction decode(FriendlyByteBuf buf) {
        C2SAction.Op op = buf.readEnum(Op.class);
        int slot = buf.readVarInt();
        ResourceLocation effect = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new C2SAction(op, slot, effect, buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(C2SAction m, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (player != null) SlotActions.handle(player, m);
        });
        ctx.get().setPacketHandled(true);
    }
}
