package io.github.mortuusars.exposure.world.camera.capture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.camera.film.properties.FilmProperties;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public record CaptureParameters(
   String exposureId,
   Optional<CameraId> cameraId,
   Optional<Integer> cameraHolderId,
   Optional<Double> fov,
   float cropFactor,
   Optional<ResourceLocation> filter,
   Optional<Projection> projection,
   Optional<ColorChannel> singleChannel,
   FilmProperties filmProperties,
   ExtraData extraData
) {
   public static final ExtraData.Type<ShutterSpeed> SHUTTER_SPEED = ExtraData.Type.stringRepresentable("shutter_speed", ShutterSpeed::new);
   public static final ExtraData.Type<Boolean> FLASH = new ExtraData.Type<>("flash", CompoundTag::getBoolean, CompoundTag::putBoolean);
   public static final ExtraData.Type<Integer> LIGHT_LEVEL = new ExtraData.Type<>("light_level", CompoundTag::getInt, CompoundTag::putInt);
   public static final Codec<CaptureParameters> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.STRING.fieldOf("id").forGetter(CaptureParameters::exposureId),
               CameraId.CODEC.optionalFieldOf("camera_id").forGetter(CaptureParameters::cameraId),
               Codec.INT.optionalFieldOf("camera_holder_id").forGetter(CaptureParameters::cameraHolderId),
               Codecs.POSITIVE_DOUBLE.optionalFieldOf("fov").forGetter(CaptureParameters::fov),
               Codecs.floatRange(0.001F, 1.0F).optionalFieldOf("crop_factor", 1.0F).forGetter(CaptureParameters::cropFactor),
               ResourceLocation.CODEC.optionalFieldOf("filter").forGetter(CaptureParameters::filter),
               Projection.CODEC.optionalFieldOf("projection").forGetter(CaptureParameters::projection),
               ColorChannel.CODEC.optionalFieldOf("single_channel").forGetter(CaptureParameters::singleChannel),
               FilmProperties.CODEC.optionalFieldOf("film", FilmProperties.EMPTY).forGetter(CaptureParameters::filmProperties),
               ExtraData.CODEC.optionalFieldOf("extra_data", ExtraData.EMPTY).forGetter(CaptureParameters::extraData)
            )
            .apply(instance, CaptureParameters::new)
   );
   public static final StreamCodec<FriendlyByteBuf, CaptureParameters> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, CaptureParameters>() {
      @NotNull
      public CaptureParameters decode(FriendlyByteBuf buffer) {
         return new CaptureParameters(
            (String)ByteBufCodecs.STRING_UTF8.decode(buffer),
            (Optional<CameraId>)ByteBufCodecs.optional(CameraId.STREAM_CODEC).decode(buffer),
            (Optional<Integer>)ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
            (Optional<Double>)ByteBufCodecs.optional(ByteBufCodecs.DOUBLE).decode(buffer),
            (Float)ByteBufCodecs.FLOAT.decode(buffer),
            (Optional<ResourceLocation>)ByteBufCodecs.optional(ByteBufCodecs.RESOURCE_LOCATION).decode(buffer),
            (Optional<Projection>)ByteBufCodecs.optional(Projection.STREAM_CODEC).decode(buffer),
            (Optional<ColorChannel>)ByteBufCodecs.optional(ColorChannel.STREAM_CODEC).decode(buffer),
            (FilmProperties)FilmProperties.STREAM_CODEC.decode(buffer),
            (ExtraData)ExtraData.STREAM_CODEC.decode(buffer)
         );
      }

      public void encode(FriendlyByteBuf buffer, CaptureParameters data) {
         ByteBufCodecs.STRING_UTF8.encode(buffer, data.exposureId());
         ByteBufCodecs.optional(CameraId.STREAM_CODEC).encode(buffer, data.cameraId());
         ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.cameraHolderId());
         ByteBufCodecs.optional(ByteBufCodecs.DOUBLE).encode(buffer, data.fov());
         ByteBufCodecs.FLOAT.encode(buffer, data.cropFactor());
         ByteBufCodecs.optional(ByteBufCodecs.RESOURCE_LOCATION).encode(buffer, data.filter());
         ByteBufCodecs.optional(Projection.STREAM_CODEC).encode(buffer, data.projection());
         ByteBufCodecs.optional(ColorChannel.STREAM_CODEC).encode(buffer, data.singleChannel());
         FilmProperties.STREAM_CODEC.encode(buffer, data.filmProperties());
         ExtraData.STREAM_CODEC.encode(buffer, data.extraData());
      }
   };

   public ShutterSpeed getShutterSpeed() {
      return this.extraData.get(SHUTTER_SPEED).orElse(ShutterSpeed.DEFAULT);
   }

   public boolean getFlash() {
      return this.extraData.get(FLASH).orElse(false);
   }

   public Optional<Integer> getLightLevel() {
      return this.extraData.get(LIGHT_LEVEL);
   }

   public CaptureParameters.Builder mutable() {
      return new CaptureParameters.Builder(this);
   }

   public static final class Builder {
      private final String exposureId;
      @Nullable
      private CameraId cameraId;
      @Nullable
      private Integer cameraHolderEntityID;
      @Nullable
      private Double fov;
      private float cropFactor = 1.0F;
      @Nullable
      private ResourceLocation filter = null;
      @Nullable
      private Projection projection;
      @Nullable
      private ColorChannel chromaticChannel;
      private FilmProperties filmProperties = FilmProperties.EMPTY;
      private final ExtraData extraData = new ExtraData();

      public Builder(String exposureId) {
         this.exposureId = exposureId;
      }

      public Builder(CaptureParameters params) {
         this.exposureId = params.exposureId();
         this.cameraId = params.cameraId().orElse(null);
         this.cameraHolderEntityID = params.cameraHolderId().orElse(null);
         this.fov = params.fov.orElse(null);
         this.cropFactor = params.cropFactor();
         this.filter = params.filter().orElse(null);
         this.projection = params.projection().orElse(null);
         this.chromaticChannel = params.singleChannel().orElse(null);
         this.filmProperties = params.filmProperties();
         this.extraData.merge(params.extraData());
      }

      public CaptureParameters.Builder setCameraID(@Nullable CameraId cameraId) {
         this.cameraId = cameraId;
         return this;
      }

      public CaptureParameters.Builder setCameraHolder(@Nullable CameraHolder holder) {
         if (holder == null) {
            this.cameraHolderEntityID = null;
         } else {
            this.cameraHolderEntityID = holder.asHolderEntity().getId();
         }

         return this;
      }

      public CaptureParameters.Builder setFilter(@Nullable ResourceLocation filter) {
         this.filter = filter;
         return this;
      }

      public CaptureParameters.Builder setFov(@Nullable Double fov) {
         this.fov = fov;
         return this;
      }

      public CaptureParameters.Builder setCropFactor(float cropFactor) {
         this.cropFactor = cropFactor;
         return this;
      }

      public CaptureParameters.Builder setProjectionInfo(@Nullable Projection projection) {
         this.projection = projection;
         return this;
      }

      public CaptureParameters.Builder setProjection(Optional<Projection> projection) {
         this.projection = projection.orElse(null);
         return this;
      }

      public CaptureParameters.Builder setChromaticChannel(@Nullable ColorChannel chromaticChannel) {
         this.chromaticChannel = chromaticChannel;
         return this;
      }

      public CaptureParameters.Builder setChromaticChannel(Optional<ColorChannel> chromaticChannel) {
         this.chromaticChannel = chromaticChannel.orElse(null);
         return this;
      }

      public CaptureParameters.Builder setFilmProperties(FilmProperties filmProperties) {
         this.filmProperties = filmProperties;
         return this;
      }

      public CaptureParameters.Builder extraData(Consumer<ExtraData> extraDataUpdater) {
         extraDataUpdater.accept(this.extraData);
         return this;
      }

      public <T> CaptureParameters.Builder extraData(ExtraData.Type<T> type, T value) {
         this.extraData.put(type, value);
         return this;
      }

      public CaptureParameters build() {
         return new CaptureParameters(
            this.exposureId,
            Optional.ofNullable(this.cameraId),
            Optional.ofNullable(this.cameraHolderEntityID),
            Optional.ofNullable(this.fov),
            this.cropFactor,
            Optional.ofNullable(this.filter),
            Optional.ofNullable(this.projection),
            Optional.ofNullable(this.chromaticChannel),
            this.filmProperties,
            this.extraData
         );
      }
   }
}
