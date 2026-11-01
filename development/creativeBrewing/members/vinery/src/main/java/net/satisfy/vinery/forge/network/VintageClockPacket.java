package net.satisfy.vinery.forge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.satisfy.vinery.core.wine.VintageClock;

import java.util.function.Supplier;

/** Server -> client: the vintage clock and the aging settings (B4.5). */
public record VintageClockPacket(VintageClock.Snapshot snapshot) {
    public void encode(FriendlyByteBuf buf) {
        VintageClock.Snapshot s = snapshot;
        buf.writeLong(s.ticks());
        buf.writeDouble(s.ticksPerDay());
        buf.writeVarInt(s.daysPerYear());
        buf.writeVarInt(s.yearsPerEffectLevel());
        buf.writeVarInt(s.maxLevel());
        buf.writeVarInt(s.durationPerYear());
        buf.writeVarInt(s.maxDuration());
        buf.writeVarInt(s.cellarPercent());
        buf.writeVarInt(s.coldPercent());
        buf.writeVarInt(s.warmPercent());
        buf.writeBoolean(s.ageOnlyInStorage());
        buf.writeBoolean(s.agingEnabled());
        buf.writeDouble(s.rate());
        buf.writeDouble(s.measuredDayTicks());
    }

    public static VintageClockPacket decode(FriendlyByteBuf buf) {
        return new VintageClockPacket(new VintageClock.Snapshot(buf.readLong(), buf.readDouble(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(),
                buf.readDouble(), buf.readDouble()));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft minecraft = Minecraft.getInstance();
            VintageClock.receive(snapshot, minecraft.level == null ? 0 : minecraft.level.getGameTime());
        });
        context.get().setPacketHandled(true);
    }
}
