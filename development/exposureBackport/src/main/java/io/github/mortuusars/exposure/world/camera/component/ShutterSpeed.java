package io.github.mortuusars.exposure.world.camera.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class ShutterSpeed implements StringRepresentable {
   public static final ShutterSpeed DEFAULT = new ShutterSpeed("1/60");
   public static final Codec<ShutterSpeed> CODEC = Codec.STRING.comapFlatMap(str -> {
      try {
         return DataResult.success(new ShutterSpeed(str));
      } catch (Exception var2) {
         return DataResult.error(var2.getMessage());
      }
   }, ShutterSpeed::getNotation);
   public static final StreamCodec<ByteBuf, ShutterSpeed> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ShutterSpeed::new, ShutterSpeed::getNotation);
   private final float valueMilliseconds;
   private final String notation;

   public ShutterSpeed(String notation) {
      notation = notation.trim();
      if (notation.endsWith("\"")) {
         this.valueMilliseconds = (float)(Integer.parseInt(notation.replace("\"", "")) * 1000);
         this.notation = notation;
      } else {
         if (!notation.contains("1/")) {
            throw new IllegalArgumentException("'%s' is not a valid shutter speed. Format should be 1/60, 2\", etc.".formatted(notation));
         }

         this.valueMilliseconds = 1.0F / (float)Integer.parseInt(notation.replace("1/", "")) * 1000.0F;
         this.notation = notation;
      }
   }

   public String getNotation() {
      return this.notation;
   }

   public float getDurationMilliseconds() {
      return this.valueMilliseconds;
   }

   public int getDurationTicks() {
      return Math.max(1, (int)(this.valueMilliseconds / 50.0F));
   }

   public boolean shouldCauseTickingSound() {
      return this.valueMilliseconds > 999.0F;
   }

   public float getStopsDifference(ShutterSpeed relative) {
      return (float)(Math.log((double)(this.valueMilliseconds / relative.getDurationMilliseconds())) / Math.log(2.0));
   }

   public float getStops() {
      return this.getStopsDifference(DEFAULT);
   }

   public float getBrightness() {
      return 1.0F + this.getStops() * 0.2F;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ShutterSpeed that = (ShutterSpeed)o;
         return Float.compare(this.valueMilliseconds, that.valueMilliseconds) == 0 && Objects.equals(this.notation, that.notation);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.valueMilliseconds, this.notation);
   }

   @NotNull
   public String getSerializedName() {
      return this.getNotation();
   }
}
