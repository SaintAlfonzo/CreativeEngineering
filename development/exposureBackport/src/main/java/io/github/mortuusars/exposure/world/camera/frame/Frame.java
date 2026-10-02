package io.github.mortuusars.exposure.world.camera.frame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record Frame(ExposureIdentifier identifier, ExposureType type, Photographer photographer, List<EntityInFrame> entitiesInFrame, ExtraData extraData) {
   public static final ExtraData.Type<Boolean> PROJECTED = ExtraData.Type.bool("projected");
   public static final ExtraData.Type<Boolean> CHROMATIC = ExtraData.Type.bool("chromatic");
   public static final ExtraData.Type<ColorChannel> COLOR_CHANNEL = ExtraData.Type.stringRepresentable("color_channel", ColorChannel::fromStringOrThrow);
   public static final ExtraData.Type<ShutterSpeed> SHUTTER_SPEED = ExtraData.Type.stringRepresentable("shutter_speed", ShutterSpeed::new);
   public static final ExtraData.Type<Long> TIMESTAMP = ExtraData.Type.longVal("timestamp");
   public static final ExtraData.Type<Integer> FOCAL_LENGTH = ExtraData.Type.intVal("focal_length");
   public static final ExtraData.Type<Boolean> FLASH = ExtraData.Type.bool("flash");
   public static final ExtraData.Type<Boolean> SELFIE = ExtraData.Type.bool("selfie");
   public static final ExtraData.Type<Boolean> ON_STAND = ExtraData.Type.bool("on_stand");
   public static final ExtraData.Type<Vec3> POSITION = ExtraData.Type.vec3("pos");
   public static final ExtraData.Type<Float> PITCH = ExtraData.Type.floatVal("pitch");
   public static final ExtraData.Type<Float> YAW = ExtraData.Type.floatVal("yaw");
   public static final ExtraData.Type<Integer> LIGHT_LEVEL = ExtraData.Type.intVal("light_level");
   public static final ExtraData.Type<Integer> DAY_TIME = ExtraData.Type.intVal("day_time");
   public static final ExtraData.Type<ResourceLocation> DIMENSION = ExtraData.Type.resourceLocation("dimension");
   public static final ExtraData.Type<ResourceLocation> BIOME = ExtraData.Type.resourceLocation("biome");
   public static final ExtraData.Type<String> WEATHER = ExtraData.Type.string("weather");
   public static final ExtraData.Type<Boolean> IN_CAVE = ExtraData.Type.bool("in_cave");
   public static final ExtraData.Type<Boolean> UNDERWATER = ExtraData.Type.bool("underwater");
   public static final ExtraData.Type<List<ResourceLocation>> STRUCTURES = ExtraData.Type.stringBasedList(
      "structures", ResourceLocation::new, ResourceLocation::toString
   );
   public static final Frame EMPTY = new Frame(ExposureIdentifier.EMPTY, ExposureType.COLOR, Photographer.EMPTY, Collections.emptyList(), ExtraData.EMPTY);
   public static final Codec<Frame> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ExposureIdentifier.CODEC.fieldOf("identifier").forGetter(Frame::identifier),
               ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(Frame::type),
               Photographer.CODEC.optionalFieldOf("photographer", Photographer.EMPTY).forGetter(Frame::photographer),
               BackportCodecs.listOf(EntityInFrame.CODEC, 0, 16).optionalFieldOf("entities_in_frame", Collections.emptyList()).forGetter(Frame::entitiesInFrame),
               ExtraData.CODEC.optionalFieldOf("extra_data", ExtraData.EMPTY).forGetter(Frame::extraData)
            )
            .apply(instance, Frame::new)
   );
   public static final StreamCodec<FriendlyByteBuf, Frame> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, Frame>() {
      @NotNull
      public Frame decode(FriendlyByteBuf buffer) {
         return new Frame(
            (ExposureIdentifier)ExposureIdentifier.STREAM_CODEC.decode(buffer),
            (ExposureType)ExposureType.STREAM_CODEC.decode(buffer),
            (Photographer)Photographer.STREAM_CODEC.decode(buffer),
            (List<EntityInFrame>)EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).decode(buffer),
            (ExtraData)ExtraData.STREAM_CODEC.decode(buffer)
         );
      }

      public void encode(FriendlyByteBuf buffer, Frame frame) {
         ExposureIdentifier.STREAM_CODEC.encode(buffer, frame.identifier());
         ExposureType.STREAM_CODEC.encode(buffer, frame.type());
         Photographer.STREAM_CODEC.encode(buffer, frame.photographer);
         EntityInFrame.STREAM_CODEC.apply(ByteBufCodecs.list(16)).encode(buffer, frame.entitiesInFrame());
         ExtraData.STREAM_CODEC.encode(buffer, frame.extraData());
      }
   };

   public static Frame.Mutable create() {
      return new Frame.Mutable(EMPTY);
   }

   public static Frame intersect(ExposureIdentifier identifier, List<Frame> frames) {
      Frame.Mutable result = EMPTY.toMutable().setIdentifier(identifier);
      if (frames.isEmpty()) {
         return result.toImmutable();
      } else {
         result.setType(getCommonValueOrDefault(frames, Frame::type));
         result.setPhotographer(getCommonValueOrDefault(frames, Frame::photographer));
         List<EntityInFrame> commonEntitiesInFrame = frames.stream().map(Frame::entitiesInFrame).map(HashSet::new).reduce((set1, set2) -> {
            set1.retainAll(set2);
            return set1;
         }).map(ArrayList::new).orElse(new ArrayList<>());
         result.setEntitiesInFrame(commonEntitiesInFrame);
         ExtraData mergedTag = frames.stream().map(f -> f.extraData.copy()).reduce(new ExtraData(), ExtraData::merge);
         result.setTag(mergedTag);
         return result.toImmutable();
      }
   }

   private static <V> V getCommonValueOrDefault(List<Frame> objects, Function<Frame, V> propertyGetter) {
      V referenceValue = propertyGetter.apply((Frame)objects.get(0));
      return objects.stream().allMatch(data -> propertyGetter.apply(data) == referenceValue) ? referenceValue : propertyGetter.apply(EMPTY);
   }

   private static <T, V> V getCommonValueOrElse(List<T> objects, Function<T, V> propertyGetter, V fallbackValue) {
      V referenceValue = propertyGetter.apply((T)objects.get(0));
      return objects.stream().allMatch(data -> propertyGetter.apply((T)data) == referenceValue) ? referenceValue : fallbackValue;
   }

   public boolean isTakenBy(LivingEntity entity) {
      return this.photographer().matches(entity);
   }

   public ExtraData getExtraDataForReading() {
      return this.extraData();
   }

   public boolean isProjected() {
      return this.getExtraDataForReading().get(PROJECTED).orElse(false);
   }

   public boolean isChromatic() {
      return this.getExtraDataForReading().get(CHROMATIC).orElse(false);
   }

   public Optional<ColorChannel> getColorChannel() {
      return this.getExtraDataForReading().get(COLOR_CHANNEL);
   }

   public boolean wasTakenWithChromaticFilter() {
      return this.getColorChannel().isPresent();
   }

   public Frame.Mutable toMutable() {
      return new Frame.Mutable(this);
   }

   public static class Mutable {
      private ExposureIdentifier identifier;
      private ExposureType type;
      private Photographer photographer;
      private List<EntityInFrame> entitiesInFrame;
      private ExtraData tag;

      public Mutable(Frame photographData) {
         this.identifier = photographData.identifier();
         this.type = photographData.type();
         this.photographer = photographData.photographer();
         this.entitiesInFrame = new ArrayList<>(photographData.entitiesInFrame());
         this.tag = photographData.extraData().copy();
      }

      public ExposureIdentifier getIdentifier() {
         return this.identifier;
      }

      public Frame.Mutable setIdentifier(ExposureIdentifier identifier) {
         this.identifier = identifier;
         return this;
      }

      public ExposureType getType() {
         return this.type;
      }

      public Frame.Mutable setType(ExposureType type) {
         this.type = type;
         return this;
      }

      public Photographer getPhotographer() {
         return this.photographer;
      }

      public Frame.Mutable setPhotographer(@NotNull Photographer photographer) {
         this.photographer = photographer;
         return this;
      }

      public List<EntityInFrame> getEntitiesInFrame() {
         return this.entitiesInFrame;
      }

      public Frame.Mutable setEntitiesInFrame(List<EntityInFrame> entitiesInFrame) {
         this.entitiesInFrame = entitiesInFrame;
         return this;
      }

      public ExtraData getTag() {
         return this.tag;
      }

      public Frame.Mutable setTag(ExtraData tag) {
         this.tag = tag;
         return this;
      }

      public Frame.Mutable updateExtraData(Consumer<ExtraData> updater) {
         updater.accept(this.tag);
         return this;
      }

      public <T> Frame.Mutable addExtraData(ExtraData.Type<T> type, T value) {
         this.tag.put(type, value);
         return this;
      }

      public Frame.Mutable setChromatic(boolean chromatic) {
         return this.updateExtraData(tag -> tag.put(Frame.CHROMATIC, chromatic));
      }

      public Frame toImmutable() {
         return new Frame(this.identifier, this.type, this.photographer, this.entitiesInFrame, this.tag);
      }
   }
}
