package io.github.mortuusars.exposure.network.packet;

import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraInHandSetS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraOnStandSetS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraRemoveS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandSetRotationsS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CameraStandStopControllingS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartDebugRGBS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CaptureStartS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ClearRenderingCacheS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.CreateChromaticExposureS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExportS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExportStopS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExposureDataChangedS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ExposureDataResponseS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ShaderApplyS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ShowExposureCommandS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.ShutterOpenedS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.UniqueSoundPlayS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.UniqueSoundPlayShutterTickingS2CP;
import io.github.mortuusars.exposure.network.packet.clientbound.UniqueSoundStopS2CP;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.TypeAndCodec;

public class S2CPackets {
   public static List<TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> getDefinitions() {
      return List.of(
         new TypeAndCodec(ActiveCameraRemoveS2CP.TYPE, ActiveCameraRemoveS2CP.STREAM_CODEC),
         new TypeAndCodec(ActiveCameraInHandSetS2CP.TYPE, ActiveCameraInHandSetS2CP.STREAM_CODEC),
         new TypeAndCodec(ActiveCameraOnStandSetS2CP.TYPE, ActiveCameraOnStandSetS2CP.STREAM_CODEC),
         new TypeAndCodec(CameraStandSetRotationsS2CP.TYPE, CameraStandSetRotationsS2CP.STREAM_CODEC),
         new TypeAndCodec(CameraStandStopControllingS2CP.TYPE, CameraStandStopControllingS2CP.STREAM_CODEC),
         new TypeAndCodec(ShaderApplyS2CP.TYPE, ShaderApplyS2CP.STREAM_CODEC),
         new TypeAndCodec(ClearRenderingCacheS2CP.TYPE, ClearRenderingCacheS2CP.STREAM_CODEC),
         new TypeAndCodec(CreateChromaticExposureS2CP.TYPE, CreateChromaticExposureS2CP.STREAM_CODEC),
         new TypeAndCodec(ExposureDataChangedS2CP.TYPE, ExposureDataChangedS2CP.STREAM_CODEC),
         new TypeAndCodec(UniqueSoundPlayS2CP.TYPE, UniqueSoundPlayS2CP.STREAM_CODEC),
         new TypeAndCodec(UniqueSoundPlayShutterTickingS2CP.TYPE, UniqueSoundPlayShutterTickingS2CP.STREAM_CODEC),
         new TypeAndCodec(UniqueSoundStopS2CP.TYPE, UniqueSoundStopS2CP.STREAM_CODEC),
         new TypeAndCodec(ShowExposureCommandS2CP.TYPE, ShowExposureCommandS2CP.STREAM_CODEC),
         new TypeAndCodec(ExposureDataResponseS2CP.TYPE, ExposureDataResponseS2CP.STREAM_CODEC),
         new TypeAndCodec(ShutterOpenedS2CP.TYPE, ShutterOpenedS2CP.STREAM_CODEC),
         new TypeAndCodec(CaptureStartS2CP.TYPE, CaptureStartS2CP.STREAM_CODEC),
         new TypeAndCodec(CaptureStartDebugRGBS2CP.TYPE, CaptureStartDebugRGBS2CP.STREAM_CODEC),
         new TypeAndCodec(ExportS2CP.TYPE, ExportS2CP.STREAM_CODEC),
         new TypeAndCodec(ExportStopS2CP.TYPE, ExportStopS2CP.STREAM_CODEC)
      );
   }
}
