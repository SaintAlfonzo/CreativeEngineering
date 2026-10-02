package io.github.mortuusars.exposure.world.item.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.netty.buffer.ByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;

public record ShutterState(boolean isOpen, long openedAtTick, ShutterSpeed shutterSpeed) {
   public static final ShutterState CLOSED = new ShutterState(false, 0L, ShutterSpeed.DEFAULT);
   public static final Codec<ShutterState> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.BOOL.optionalFieldOf("is_open", false).forGetter(ShutterState::isOpen),
               Codec.LONG.optionalFieldOf("opened_at", 0L).forGetter(ShutterState::openedAtTick),
               ShutterSpeed.CODEC.optionalFieldOf("shutter_speed", ShutterSpeed.DEFAULT).forGetter(ShutterState::shutterSpeed)
            )
            .apply(instance, ShutterState::new)
   );
   public static final StreamCodec<ByteBuf, ShutterState> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      ShutterState::isOpen,
      ByteBufCodecs.VAR_LONG,
      ShutterState::openedAtTick,
      ShutterSpeed.STREAM_CODEC,
      ShutterState::shutterSpeed,
      ShutterState::new
   );

   public static ShutterState open(long openedAt, ShutterSpeed shutterSpeed) {
      return new ShutterState(true, openedAt, shutterSpeed);
   }

   public static ShutterState closed() {
      return CLOSED;
   }

   public long getCloseTick() {
      return this.isOpen ? this.openedAtTick + (long)this.shutterSpeed.getDurationTicks() : -1L;
   }
}
