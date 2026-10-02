package io.github.mortuusars.exposure_polaroid.network.packet;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.TypeAndCodec;

public class C2SPackets {
   public static List<TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
      return List.of();
   }
}
