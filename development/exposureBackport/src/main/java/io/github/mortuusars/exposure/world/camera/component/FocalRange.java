package io.github.mortuusars.exposure.world.camera.component;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.util.Fov;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public final class FocalRange implements StringRepresentable {
   public static final Codec<FocalRange> RANGE_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Codec.INT.fieldOf("min").forGetter(FocalRange::min), Codec.INT.fieldOf("max").forGetter(FocalRange::max))
            .apply(instance, FocalRange::new)
   );
   public static final Codec<FocalRange> PRIME_CODEC = Codec.INT.xmap(FocalRange::new, FocalRange::min);
   public static final Codec<FocalRange> CODEC = Codec.either(PRIME_CODEC, RANGE_CODEC)
      .xmap(BackportCodecs::unwrap, focalRange -> focalRange.isPrime() ? Either.left(focalRange) : Either.right(focalRange));
   public static final StreamCodec<ByteBuf, FocalRange> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, FocalRange::min, ByteBufCodecs.VAR_INT, FocalRange::max, FocalRange::new
   );
   public static final FocalRange REGULAR = new FocalRange(18, 55);
   public static final int ALLOWED_MIN = 10;
   public static final int ALLOWED_MAX = 300;
   private final int min;
   private final int max;

   public FocalRange(int min, int max) {
      Preconditions.checkArgument(10 <= min && min <= 300, min + " is not in allowed range for 'min'.");
      Preconditions.checkArgument(10 <= max && max <= 300, max + " is not in allowed range for 'max'.");
      Preconditions.checkArgument(min <= max, "'min' should not be larger than 'max'. min: " + min + ", max: " + max);
      this.min = min;
      this.max = max;
   }

   public FocalRange(int primeValue) {
      Preconditions.checkArgument(10 <= primeValue && primeValue <= 300, primeValue + " is not in allowed range: 10-300");
      this.min = primeValue;
      this.max = primeValue;
   }

   public boolean isPrime() {
      return this.min == this.max;
   }

   public double focalLengthFromZoom(double zoom) {
      zoom = Mth.clamp(zoom, 0.0, 1.0);
      return Mth.map(zoom, 0.0, 1.0, (double)this.min, (double)this.max);
   }

   public double fovFromZoom(double zoom) {
      return Fov.focalLengthToFov(this.focalLengthFromZoom(zoom));
   }

   public double zoomFromFov(double fov) {
      double focalLength = Fov.fovToFocalLength(fov);
      return Mth.map(focalLength, (double)this.min(), (double)this.max(), 0.0, 1.0);
   }

   public double clampFocalLength(double focalLength) {
      return Mth.clamp(focalLength, (double)this.min, (double)this.max);
   }

   public double clampFov(double fov) {
      return Mth.clamp(fov, Fov.focalLengthToFov((double)this.max), Fov.focalLengthToFov((double)this.min));
   }

   @NotNull
   public static FocalRange getDefault() {
      return parse((String)Config.Server.CAMERA_DEFAULT_FOCAL_RANGE.get());
   }

   @NotNull
   public String getSerializedName() {
      return this.isPrime() ? Integer.toString(this.min) : this.min + "-" + this.max;
   }

   public static FocalRange parse(String value) {
      int dashIndex = value.indexOf("-");
      if (dashIndex == -1) {
         int prime = Integer.parseInt(value);
         return new FocalRange(prime);
      } else {
         int min = Integer.parseInt(value.substring(0, dashIndex));
         int max = Integer.parseInt(value.substring(dashIndex + 1));
         return new FocalRange(min, max);
      }
   }

   public static FocalRange fromJson(@Nullable JsonElement json) {
      if (json == null || json.isJsonNull()) {
         throw new JsonSyntaxException("Item cannot be null");
      } else if (json.isJsonPrimitive()) {
         int fixedValue = json.getAsInt();
         return new FocalRange(fixedValue);
      } else if (json.isJsonObject()) {
         JsonObject obj = json.getAsJsonObject();
         int min = obj.get("min").getAsInt();
         int max = obj.get("max").getAsInt();
         return new FocalRange(min, max);
      } else {
         throw new JsonSyntaxException("Invalid FocalRange json. Expected a number or json object with 'min' and 'max'.");
      }
   }

   public int min() {
      return this.min;
   }

   public int max() {
      return this.max;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         FocalRange that = (FocalRange)obj;
         return this.min == that.min && this.max == that.max;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max);
   }

   @Override
   public String toString() {
      return this.isPrime() ? "FocalRange[fixed=" + this.min + "]" : "FocalRange[min=" + this.min + ", max=" + this.max + "]";
   }
}
