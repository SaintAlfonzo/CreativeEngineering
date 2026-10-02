package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;

public record HSB(float hue, float saturation, float brightness) {
   public static final HSB EMPTY = new HSB(0.0F, 0.0F, 0.0F);
   public static final Codec<HSB> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("hue", 0.0F).forGetter(HSB::hue),
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("saturation", 0.0F).forGetter(HSB::saturation),
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("brightness", 0.0F).forGetter(HSB::brightness)
            )
            .apply(instance, HSB::new)
   );
   public static final StreamCodec<FriendlyByteBuf, HSB> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT, HSB::hue, ByteBufCodecs.FLOAT, HSB::saturation, ByteBufCodecs.FLOAT, HSB::brightness, HSB::new
   );

   public HSB(float hue, float saturation, float brightness) {
      Preconditions.checkArgument(hue >= -1.0F && hue <= 1.0F, "h must be in -1 to 1 range.");
      Preconditions.checkArgument(saturation >= -1.0F && saturation <= 1.0F, "s must be in -1 to 1 range.");
      Preconditions.checkArgument(brightness >= -1.0F && brightness <= 1.0F, "b must be in -1 to 1 range.");
      this.hue = hue;
      this.saturation = saturation;
      this.brightness = brightness;
   }

   @Override
   public String toString() {
      return "H:%s, S:%s, B:%s".formatted(this.hue, this.saturation, this.brightness);
   }
}
