package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.Codecs;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public class ExposureData extends SavedData {
   public static final Codec<ExposureData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.INT.fieldOf("width").forGetter(ExposureData::getWidth), Codec.INT.fieldOf("height").forGetter(ExposureData::getHeight), Codecs.byteArrayCodec(1, 4194304).fieldOf("pixels").forGetter(ExposureData::getPixels), ResourceLocation.CODEC.optionalFieldOf("palette", ColorPalettes.DEFAULT.location()).forGetter(ExposureData::getPaletteId), ExposureData.Tag.CODEC.optionalFieldOf("tag", ExposureData.Tag.EMPTY).forGetter(ExposureData::getTag)).apply(instance, ExposureData::new));
   public static final StreamCodec<ByteBuf, ExposureData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ExposureData::getWidth, ByteBufCodecs.VAR_INT, ExposureData::getHeight, ByteBufCodecs.byteArray(4194304), ExposureData::getPixels, ByteBufCodecs.RESOURCE_LOCATION, ExposureData::getPaletteId, ExposureData.Tag.STREAM_CODEC, ExposureData::getTag, ExposureData::new);
   public static final ExposureData EMPTY = new ExposureData(1, 1, new byte[]{0}, ColorPalettes.DEFAULT.location(), ExposureData.Tag.EMPTY);
   private final int width;
   private final int height;
   private final byte[] pixels;
   private final ResourceLocation palette;
   private final ExposureData.Tag tag;

   public ExposureData(int width, int height, byte[] pixels, ResourceLocation paletteId, ExposureData.Tag tag) {
      Preconditions.checkArgument(width > 0, "Width should be larger than 0. %s", width);
      Preconditions.checkArgument(height > 0, "Height should be larger than 0. %s ", height);
      Preconditions.checkArgument(pixels.length == width * height, "Pixel count '%s' is not correct for image dimensions of '%sx%s'. Count should be '%s'.", pixels.length, width, height, width * height);
      this.width = width;
      this.height = height;
      this.pixels = pixels;
      this.palette = paletteId;
      this.tag = tag;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public byte[] getPixels() {
      return this.pixels;
   }

   public byte getPixel(int x, int y) {
      return this.pixels[y * this.width + x];
   }

   public ResourceLocation getPaletteId() {
      return this.palette;
   }

   public ExposureData.Tag getTag() {
      return this.tag;
   }

   public ExposureData withTag(Function<ExposureData.Tag, ExposureData.Tag> updateFunction) {
      return new ExposureData(this.width, this.height, this.pixels, this.palette, (ExposureData.Tag)updateFunction.apply(this.tag));
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ExposureData that = (ExposureData)o;
         return this.width == that.width && this.height == that.height && Objects.deepEquals(this.pixels, that.pixels) && Objects.equals(this.palette, that.palette) && Objects.equals(this.tag, that.tag);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.width, this.height, Arrays.hashCode(this.pixels), this.palette, this.tag});
   }

   public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
      DataResult<net.minecraft.nbt.Tag> encodingResult = CODEC.encode(this, NbtOps.INSTANCE, tag);
      if (BackportCodecs.isSuccess(encodingResult)) {
         net.minecraft.nbt.Tag encodedTag = (net.minecraft.nbt.Tag)BackportCodecs.getOrThrow(encodingResult);
         if (encodedTag instanceof CompoundTag) {
            return (CompoundTag)encodedTag;
         }

         Exposure.LOGGER.error("Cannot save PalettedExposure: '{}'. Encoded tag is not CompoundTag but a {}", this, encodedTag.getType());
      }

      encodingResult.error().ifPresent((error) -> Exposure.LOGGER.error("Cannot save PalettedExposure: {}", error.message()));
      return tag;
   }

   public static ExposureData load(CompoundTag tag) {
      return (ExposureData)BackportCodecs.getOrThrow(CODEC.parse(NbtOps.INSTANCE, tag));
   }

   public static record Tag(ExposureType type, String creator, long unixTimestamp, boolean loaded, boolean wasPrinted) {
      public static final Codec<ExposureData.Tag> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(ExposureData.Tag::type), Codec.STRING.optionalFieldOf("creator", "").forGetter(ExposureData.Tag::creator), Codec.LONG.optionalFieldOf("timestamp", 1645494000L).forGetter(ExposureData.Tag::unixTimestamp), Codec.BOOL.optionalFieldOf("loaded", false).forGetter(ExposureData.Tag::loaded), Codec.BOOL.optionalFieldOf("was_printed", false).forGetter(ExposureData.Tag::wasPrinted)).apply(instance, ExposureData.Tag::new));
      public static final StreamCodec<ByteBuf, ExposureData.Tag> STREAM_CODEC = new StreamCodec<ByteBuf, ExposureData.Tag>() {
         @NotNull
         public ExposureData.@NotNull Tag decode(ByteBuf buffer) {
            return new ExposureData.Tag((ExposureType)ExposureType.STREAM_CODEC.decode(buffer), (String)ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.VAR_LONG.decode(buffer), ByteBufCodecs.BOOL.decode(buffer), ByteBufCodecs.BOOL.decode(buffer));
         }

         public void encode(ByteBuf buffer, ExposureData.Tag data) {
            ExposureType.STREAM_CODEC.encode(buffer, data.type());
            ByteBufCodecs.STRING_UTF8.encode(buffer, data.creator());
            ByteBufCodecs.VAR_LONG.encode(buffer, data.unixTimestamp());
            ByteBufCodecs.BOOL.encode(buffer, data.loaded());
            ByteBufCodecs.BOOL.encode(buffer, data.wasPrinted());
         }
      };
      public static final ExposureData.Tag EMPTY = new ExposureData.Tag(ExposureType.COLOR, "", 0L, false, false);

      public ExposureData.Tag withType(ExposureType type) {
         return new ExposureData.Tag(type, this.creator, this.unixTimestamp, this.loaded, this.wasPrinted);
      }

      public ExposureData.Tag withCreator(String creator) {
         return new ExposureData.Tag(this.type, creator, this.unixTimestamp, this.loaded, this.wasPrinted);
      }

      public ExposureData.Tag withTimestamp(long unixTimestamp) {
         return new ExposureData.Tag(this.type, this.creator, unixTimestamp, this.loaded, this.wasPrinted);
      }

      public ExposureData.Tag withLoadedSetTo(boolean isLoaded) {
         return new ExposureData.Tag(this.type, this.creator, this.unixTimestamp, isLoaded, this.wasPrinted);
      }

      public ExposureData.Tag withWasPrintedSetTo(boolean wasPrinted) {
         return new ExposureData.Tag(this.type, this.creator, this.unixTimestamp, this.loaded, wasPrinted);
      }

      public ExposureData.Tag setPrinted() {
         return new ExposureData.Tag(this.type, this.creator, this.unixTimestamp, this.loaded, true);
      }
   }
}
