package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.backport.network.PayloadChannel;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;

public interface Packet extends PayloadChannel.HandledPayload {
   boolean handle(PacketFlow flow, Player player);
}
