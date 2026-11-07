// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/network/c2s/BackTrackLocation.kt. Translated
// to Java for Forge 1.19.2 (SimpleChannel message; DATE_BYTE_CODEC = epoch ms long).
package earth.terrarium.tempad.common.network.c2s;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.context.ContextHolder;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.config.CommonConfig;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;

public record BackTrackLocation(Date time, ContextHolder<?> ctx) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(time.getTime());
        ctx.encode(buf);
    }

    public static BackTrackLocation decode(FriendlyByteBuf buf) {
        return new BackTrackLocation(new Date(buf.readLong()), ContextHolder.decode(buf));
    }

    public void handle(ServerPlayer player) {
        SyncableContext<?> context = ctx.getCtx(player);
        int cost = CommonConfig.TimeTwister.costToBacktrack();
        ChrononHandler chronons = ChrononHandler.of(context.getStack());
        if (!player.isCreative() && (chronons == null || chronons.extract(cost, ActionType.Simulate) != cost)) return;
        context.modify(it -> {
            ChrononHandler handler = ChrononHandler.of(it);
            if (handler != null) handler.extract(cost, ActionType.Execute);
        });
        player.getCooldowns().addCooldown(context.getStack().getItem(), 40);
        TempadPlayerData.get(player).getTravelHistory().backtrackTo(player, time);
    }
}
