package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;

public record ColorBalance(float r, float g, float b) {
   public static final ColorBalance EMPTY = new ColorBalance(0.0F, 0.0F, 0.0F);
   public static final Codec<ColorBalance> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("red", 0.0F).forGetter(ColorBalance::r),
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("green", 0.0F).forGetter(ColorBalance::g),
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("blue", 0.0F).forGetter(ColorBalance::b)
            )
            .apply(instance, ColorBalance::new)
   );
   public static final StreamCodec<FriendlyByteBuf, ColorBalance> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT, ColorBalance::r, ByteBufCodecs.FLOAT, ColorBalance::g, ByteBufCodecs.FLOAT, ColorBalance::b, ColorBalance::new
   );

   public ColorBalance(float r, float g, float b) {
      Preconditions.checkArgument(r >= -1.0F && r <= 1.0F, "r must be in -1 to 1 range.");
      Preconditions.checkArgument(g >= -1.0F && g <= 1.0F, "g must be in -1 to 1 range.");
      Preconditions.checkArgument(b >= -1.0F && b <= 1.0F, "b must be in -1 to 1 range.");
      this.r = r;
      this.g = g;
      this.b = b;
   }

   @Override
   public String toString() {
      return "R:%s, G:%s, B:%s".formatted(this.r, this.g, this.b);
   }
}
