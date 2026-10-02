package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import io.github.mortuusars.exposure.backport.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import io.github.mortuusars.exposure.backport.util.ByIdMap.OutOfBoundsStrategy;
import org.jetbrains.annotations.NotNull;

public enum DitherMode implements StringRepresentable {
   DITHERED("dithered"),
   CLEAN("clean");

   private static final IntFunction<DitherMode> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<DitherMode> CODEC = StringRepresentable.fromEnum(DitherMode::values);
   public static final StreamCodec<ByteBuf, DitherMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
   private final String name;

   private DitherMode(String name) {
      this.name = name;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }

   public Component translate() {
      return Component.translatable("item.exposure.interplanar_projector.mode." + this.getSerializedName());
   }

   public DitherMode cycle() {
      return BY_ID.apply(this.ordinal() + 1);
   }
}
