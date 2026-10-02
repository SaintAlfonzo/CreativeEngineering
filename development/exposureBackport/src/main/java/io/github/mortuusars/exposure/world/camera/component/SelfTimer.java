package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import io.github.mortuusars.exposure.backport.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import io.github.mortuusars.exposure.backport.util.ByIdMap.OutOfBoundsStrategy;
import org.jetbrains.annotations.NotNull;

public enum SelfTimer implements StringRepresentable {
   OFF("off", 0),
   ONE("one", 1),
   TWO("two", 2),
   FIVE("five", 5),
   TEN("ten", 10);

   public static final Codec<SelfTimer> CODEC = StringRepresentable.fromEnum(SelfTimer::values);
   public static final StreamCodec<ByteBuf, SelfTimer> STREAM_CODEC = ByteBufCodecs.idMapper(
      ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO), Enum::ordinal
   );
   private final String name;
   private final int seconds;

   private SelfTimer(String name, int seconds) {
      this.name = name;
      this.seconds = seconds;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }

   public int getSeconds() {
      return this.seconds;
   }

   public int getTicks() {
      return this.seconds * 20;
   }

   public MutableComponent translate() {
      return Component.translatable("gui.exposure.self_timer." + this.name);
   }
}
