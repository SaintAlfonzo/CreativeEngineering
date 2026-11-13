package com.SaintAlfonzo.solraspberrypie.net;

import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** The server's ladder, sent at login (3). */
public final class S2CLadder {
    final Ladder ladder;

    public S2CLadder(Ladder ladder) {
        this.ladder = ladder;
    }

    public static void encode(S2CLadder m, FriendlyByteBuf buf) {
        m.ladder.write(buf);
    }

    public static S2CLadder decode(FriendlyByteBuf buf) {
        return new S2CLadder(Ladder.read(buf));
    }

    public static void handle(S2CLadder m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.SaintAlfonzo.solraspberrypie.client.ClientState.setLadder(m.ladder)));
        ctx.get().setPacketHandled(true);
    }
}
