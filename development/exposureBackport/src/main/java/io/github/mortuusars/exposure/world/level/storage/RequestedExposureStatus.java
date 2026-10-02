package io.github.mortuusars.exposure.world.level.storage;

import io.netty.buffer.ByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;

public enum RequestedExposureStatus {
   NOT_REQUESTED,
   AWAITED,
   TIMED_OUT,
   INVALID_ID,
   NOT_FOUND,
   CANNOT_LOAD,
   SUCCESS,
   NEEDS_REFRESH;

   public static final StreamCodec<ByteBuf, RequestedExposureStatus> STREAM_CODEC = ByteBufCodecs.idMapper(id -> values()[id], Enum::ordinal);
}
