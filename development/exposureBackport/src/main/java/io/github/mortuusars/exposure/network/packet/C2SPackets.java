package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraReleaseC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraSetSettingC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.AlbumSignC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.AlbumSyncNoteC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.CameraStandTurnC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureDataC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.ExposureRequestC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.InterplanarProjectionFinishedC2SP;
import io.github.mortuusars.exposure.network.packet.serverbound.OpenCameraAttachmentsInCreativePacketC2SP;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.TypeAndCodec;

public class C2SPackets {
   public static List<TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
      return List.of(
         new TypeAndCodec(AlbumSignC2SP.TYPE, AlbumSignC2SP.STREAM_CODEC),
         new TypeAndCodec(AlbumSyncNoteC2SP.TYPE, AlbumSyncNoteC2SP.STREAM_CODEC),
         new TypeAndCodec(ActiveCameraSetSettingC2SP.TYPE, ActiveCameraSetSettingC2SP.STREAM_CODEC),
         new TypeAndCodec(OpenCameraAttachmentsInCreativePacketC2SP.TYPE, OpenCameraAttachmentsInCreativePacketC2SP.STREAM_CODEC),
         new TypeAndCodec(ExposureRequestC2SP.TYPE, ExposureRequestC2SP.STREAM_CODEC),
         new TypeAndCodec(ActiveCameraReleaseC2SP.TYPE, ActiveCameraReleaseC2SP.STREAM_CODEC),
         new TypeAndCodec(InterplanarProjectionFinishedC2SP.TYPE, InterplanarProjectionFinishedC2SP.STREAM_CODEC),
         new TypeAndCodec(ExposureDataC2SP.TYPE, ExposureDataC2SP.STREAM_CODEC),
         new TypeAndCodec(CameraStandTurnC2SP.TYPE, CameraStandTurnC2SP.STREAM_CODEC)
      );
   }
}
