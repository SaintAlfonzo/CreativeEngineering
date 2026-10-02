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

public enum FlashMode implements StringRepresentable {
   OFF("off"),
   ON("on"),
   AUTO("auto");

   public static final Codec<FlashMode> CODEC = StringRepresentable.fromEnum(FlashMode::values);
   public static final StreamCodec<ByteBuf, FlashMode> STREAM_CODEC = ByteBufCodecs.idMapper(
      ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO), Enum::ordinal
   );
   private final String name;

   private FlashMode(String name) {
      this.name = name;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }

   public MutableComponent translate() {
      return Component.translatable("gui.exposure.flash_mode." + this.name);
   }
}
