package io.github.mortuusars.exposure.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import io.github.mortuusars.exposure.backport.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

public class ExtraData extends CompoundTag {
   public static final Codec<ExtraData> CODEC = CompoundTag.CODEC.xmap(ExtraData::new, data -> data);
   public static final StreamCodec<ByteBuf, ExtraData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(ExtraData::new, data -> data);
   public static final ExtraData EMPTY = new ExtraData(Collections.emptyMap());
   private final Map<String, Tag> tags;

   protected ExtraData(Map<String, Tag> tags) {
      super(tags);
      this.tags = tags;
   }

   public ExtraData() {
      this(new HashMap<>());
   }

   public ExtraData(CompoundTag tag) {
      this(new HashMap<>());
      this.merge(tag);
   }

   public <T> Optional<T> get(@NotNull ExtraData.Type<T> type) {
      if (!this.contains(type.key())) {
         return Optional.empty();
      } else {
         try {
            return Optional.ofNullable(type.getter().apply(this, type.key()));
         } catch (Exception var3) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", var3.getMessage());
            return Optional.empty();
         }
      }
   }

   public <T> T getOrDefault(@NotNull ExtraData.Type<T> type, T defaultValue) {
      if (!this.contains(type.key())) {
         return defaultValue;
      } else {
         try {
            T value = type.getter().apply(this, type.key());
            return value != null ? value : defaultValue;
         } catch (Exception var4) {
            Exposure.LOGGER.error("Cannot get ExtraData entry: {}", var4.getMessage());
            return defaultValue;
         }
      }
   }

   public <T> void put(ExtraData.Type<T> type, @NotNull T value) {
      Preconditions.checkNotNull(value, "value");
      type.setter().accept(this, type.key(), value);
   }

   public <T> void remove(ExtraData.Type<T> type) {
      this.remove(type.key());
   }

   @NotNull
   public ExtraData copy() {
      Map<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
      return new ExtraData(map);
   }

   @NotNull
   public ExtraData merge(CompoundTag other) {
      for (String key : other.getAllKeys()) {
         Tag tag = other.get(key);

         assert tag != null;

         if (tag.getId() == 10) {
            if (this.contains(key, 10)) {
               CompoundTag compoundTag = this.getCompound(key);
               compoundTag.merge((CompoundTag)tag);
            } else {
               this.put(key, tag.copy());
            }
         } else {
            this.put(key, tag.copy());
         }
      }

      return this;
   }

   public static record Type<T>(String key, BiFunction<ExtraData, String, T> getter, TriConsumer<ExtraData, String, T> setter) {
      public static ExtraData.Type<String> string(String key) {
         return new ExtraData.Type<>(key, CompoundTag::getString, CompoundTag::putString);
      }

      public static ExtraData.Type<Boolean> bool(String key) {
         return new ExtraData.Type<>(key, CompoundTag::getBoolean, CompoundTag::putBoolean);
      }

      public static ExtraData.Type<Integer> intVal(String key) {
         return new ExtraData.Type<>(key, CompoundTag::getInt, CompoundTag::putInt);
      }

      public static ExtraData.Type<Long> longVal(String key) {
         return new ExtraData.Type<>(key, CompoundTag::getLong, CompoundTag::putLong);
      }

      public static ExtraData.Type<Float> floatVal(String key) {
         return new ExtraData.Type<>(key, CompoundTag::getFloat, CompoundTag::putFloat);
      }

      public static ExtraData.Type<Double> doubleVal(String key) {
         return new ExtraData.Type<>(key, CompoundTag::getDouble, CompoundTag::putDouble);
      }

      public static <T extends StringRepresentable> ExtraData.Type<T> stringRepresentable(String key, Function<String, T> deserializeFunction) {
         return new ExtraData.Type<>(
            key, (data, k) -> deserializeFunction.apply(data.getString(k)), (data, k, value) -> data.putString(k, value.getSerializedName())
         );
      }

      public static ExtraData.Type<Vec3> vec3(String key) {
         return new ExtraData.Type<>(key, (data, k) -> {
            ListTag pos = data.getList(k, 6);
            return new Vec3(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
         }, (data, k, value) -> {
            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(value.x()));
            pos.add(DoubleTag.valueOf(value.y()));
            pos.add(DoubleTag.valueOf(value.z()));
            data.put(k, pos);
         });
      }

      public static ExtraData.Type<ResourceLocation> resourceLocation(String key) {
         return new ExtraData.Type<>(key, (data, k) -> new ResourceLocation(data.getString(k)), (data, k, value) -> data.putString(k, value.toString()));
      }

      public static <T> ExtraData.Type<List<T>> list(String key, int tagType, Function<Tag, T> extractFunc, Function<T, Tag> packFunc) {
         return new ExtraData.Type<>(key, (data, k) -> data.getList(k, tagType).stream().map(extractFunc).toList(), (data, k, value) -> {
            ListTag list = new ListTag();
            list.addAll(value.stream().map(packFunc).toList());
            data.put(k, list);
         });
      }

      public static <T> ExtraData.Type<List<T>> stringBasedList(String key, Function<String, T> extractFunc, Function<T, String> packFunc) {
         return list(key, 8, tag -> extractFunc.apply(tag.getAsString()), value -> StringTag.valueOf(packFunc.apply(value)));
      }
   }
}
