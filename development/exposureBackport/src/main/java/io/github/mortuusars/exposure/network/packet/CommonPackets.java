package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.common.ActiveCameraDeactivateCommonPacket;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.TypeAndCodec;

public class CommonPackets {
   public static List<TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
      return List.of(new TypeAndCodec(ActiveCameraDeactivateCommonPacket.TYPE, ActiveCameraDeactivateCommonPacket.STREAM_CODEC));
   }
}
