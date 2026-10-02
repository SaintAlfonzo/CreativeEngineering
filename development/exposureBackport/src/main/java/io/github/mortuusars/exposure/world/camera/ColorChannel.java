package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum ColorChannel implements StringRepresentable {
   RED(-2600386),
   GREEN(-8665525),
   BLUE(-11635762);

   public static final Codec<ColorChannel> CODEC = StringRepresentable.fromEnum(ColorChannel::values);
   public static final StreamCodec<ByteBuf, ColorChannel> STREAM_CODEC = ByteBufCodecs.idMapper(id -> values()[id], Enum::ordinal);
   private final int color;

   private ColorChannel(int color) {
      this.color = color;
   }

   public int getRepresentationColor() {
      return this.color;
   }

   public static Optional<ColorChannel> fromFilterStack(ItemStack stack) {
      if (stack.is(Exposure.Tags.Items.RED_FILTERS)) {
         return Optional.of(RED);
      } else if (stack.is(Exposure.Tags.Items.GREEN_FILTERS)) {
         return Optional.of(GREEN);
      } else {
         return stack.is(Exposure.Tags.Items.BLUE_FILTERS) ? Optional.of(BLUE) : Optional.empty();
      }
   }

   public static ColorChannel fromStringOrDefault(String serializedName, ColorChannel defaultValue) {
      for (ColorChannel value : values()) {
         if (value.getSerializedName().equals(serializedName)) {
            return value;
         }
      }

      return defaultValue;
   }

   public static Optional<ColorChannel> fromStringOptional(String serializedName) {
      for (ColorChannel value : values()) {
         if (value.getSerializedName().equals(serializedName)) {
            return Optional.of(value);
         }
      }

      return Optional.empty();
   }

   public static ColorChannel fromStringOrThrow(String serializedName) {
      for (ColorChannel value : values()) {
         if (value.getSerializedName().equals(serializedName)) {
            return value;
         }
      }

      throw new IllegalArgumentException(serializedName + " is not a valid ColorChannel.");
   }

   @NotNull
   public String getSerializedName() {
      return this.toString().toLowerCase();
   }

   public ResourceLocation getShader() {
      return Exposure.resource("shaders/" + this.getSerializedName() + "_filter.json");
   }
}
