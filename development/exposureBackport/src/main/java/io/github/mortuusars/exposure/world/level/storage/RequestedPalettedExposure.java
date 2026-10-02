package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.ExposureRequester;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class RequestedPalettedExposure {
   public static final RequestedPalettedExposure NOT_REQUESTED = status(RequestedExposureStatus.NOT_REQUESTED);
   public static final RequestedPalettedExposure AWAITING = status(RequestedExposureStatus.AWAITED);
   public static final RequestedPalettedExposure TIMED_OUT = status(RequestedExposureStatus.TIMED_OUT);
   public static final RequestedPalettedExposure INVALID_ID = status(RequestedExposureStatus.INVALID_ID);
   public static final RequestedPalettedExposure NOT_FOUND = status(RequestedExposureStatus.NOT_FOUND);
   public static final RequestedPalettedExposure CANNOT_LOAD = status(RequestedExposureStatus.CANNOT_LOAD);
   public static final StreamCodec<FriendlyByteBuf, RequestedPalettedExposure> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(ExposureData.STREAM_CODEC),
      RequestedPalettedExposure::getData,
      RequestedExposureStatus.STREAM_CODEC,
      RequestedPalettedExposure::getStatus,
      RequestedPalettedExposure::fromOptional
   );
   @Nullable
   protected final ExposureData exposure;
   protected final RequestedExposureStatus status;

   protected RequestedPalettedExposure(@Nullable ExposureData exposure, RequestedExposureStatus status) {
      this.exposure = exposure;
      this.status = status;
   }

   private static RequestedPalettedExposure fromOptional(Optional<ExposureData> data, RequestedExposureStatus status) {
      return new RequestedPalettedExposure(data.orElse(null), status);
   }

   private static RequestedPalettedExposure status(RequestedExposureStatus status) {
      Preconditions.checkArgument(
         status != RequestedExposureStatus.SUCCESS && status != RequestedExposureStatus.NEEDS_REFRESH, "Successful result cannot be created without data."
      );
      return new RequestedPalettedExposure(null, status);
   }

   public static RequestedPalettedExposure success(ExposureData data) {
      Preconditions.checkNotNull(data, "Successful result cannot be created without data.");
      if (data.equals(ExposureData.EMPTY)) {
         Exposure.LOGGER.warn("ExposureData.EMPTY is used to create successful ExposureResult. This is probably not intentional.");
      }

      return new RequestedPalettedExposure(data, RequestedExposureStatus.SUCCESS);
   }

   public static RequestedPalettedExposure needsRefresh(RequestedPalettedExposure result) {
      Preconditions.checkArgument(result.is(RequestedExposureStatus.SUCCESS));
      return new RequestedPalettedExposure(result.exposure, RequestedExposureStatus.NEEDS_REFRESH);
   }

   public static RequestedPalettedExposure fromRequestStatus(ExposureRequester.Status status) {
      return switch (status) {
         default -> throw new IllegalArgumentException(status + " is unexpected.");
         case AWAITING -> AWAITING;
         case TIMED_OUT -> TIMED_OUT;
      };
   }

   public Optional<ExposureData> getData() {
      return Optional.ofNullable(this.exposure);
   }

   public <T> T map(Function<ExposureData, T> ifPresent, T orElse) {
      return (T)(this.exposure != null ? ifPresent.apply(this.exposure) : orElse);
   }

   public ExposureData orElse(ExposureData orElse) {
      return this.exposure != null ? this.exposure : orElse;
   }

   public RequestedExposureStatus getStatus() {
      return this.status;
   }

   public boolean is(RequestedExposureStatus status) {
      return this.status.equals(status);
   }

   public boolean isError() {
      return this.status != RequestedExposureStatus.SUCCESS
         && this.status != RequestedExposureStatus.NEEDS_REFRESH
         && this.status != RequestedExposureStatus.AWAITED
         && this.status != RequestedExposureStatus.NOT_REQUESTED;
   }
}
