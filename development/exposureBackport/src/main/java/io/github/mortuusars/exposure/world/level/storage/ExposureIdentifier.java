package io.github.mortuusars.exposure.world.level.storage;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExposureIdentifier {
   public static final ExposureIdentifier EMPTY = new ExposureIdentifier("", null);
   public static final Codec<ExposureIdentifier> CODEC = new Codec<ExposureIdentifier>() {
      public <T> DataResult<T> encode(ExposureIdentifier input, DynamicOps<T> ops, T prefix) {
         return input.isEmpty()
            ? DataResult.success(ops.createString(""))
            : input.map(id -> ExposureIdentifier.SIMPLE_ID_CODEC, texture -> ExposureIdentifier.FULL_CODEC).encode(input, ops, prefix);
      }

      public <T> DataResult<Pair<ExposureIdentifier, T>> decode(DynamicOps<T> ops, T input) {
         DataResult<Pair<ExposureIdentifier, T>> simpleResult = ExposureIdentifier.SIMPLE_ID_CODEC.decode(ops, input);
         return simpleResult.error().isPresent() ? ExposureIdentifier.FULL_CODEC.decode(ops, input) : simpleResult;
      }
   };
   public static final Codec<ExposureIdentifier> SIMPLE_ID_CODEC = Codec.STRING
      .flatComapMap(
         ExposureIdentifier::id,
         identifier -> identifier.id != null ? DataResult.success(identifier.id) : DataResult.error("Cannot serialize to string: id is null.")
      );
   public static final Codec<ExposureIdentifier> FULL_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.STRING.optionalFieldOf("id").forGetter(ei -> Optional.ofNullable(ei.id)),
               ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(ei -> Optional.ofNullable(ei.texture))
            )
            .apply(instance, (idOpt, textureOpt) -> new ExposureIdentifier((String)idOpt.orElse(null), (ResourceLocation)textureOpt.orElse(null)))
   );
   public static final StreamCodec<FriendlyByteBuf, ExposureIdentifier> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, ExposureIdentifier>() {
      @NotNull
      public ExposureIdentifier decode(FriendlyByteBuf buffer) {
         boolean isTexture = buffer.readBoolean();
         return isTexture ? ExposureIdentifier.texture(buffer.readResourceLocation()) : ExposureIdentifier.id(buffer.readUtf());
      }

      public void encode(FriendlyByteBuf buffer, ExposureIdentifier instance) {
         buffer.writeBoolean(instance.isTexture());
         instance.ifId(buffer::writeUtf).ifTexture(buffer::writeResourceLocation);
      }
   };
   @Nullable
   private final String id;
   @Nullable
   private final ResourceLocation texture;
   private static int sameTickCounter = 0;
   private static long lastTick = -1L;

   private ExposureIdentifier(@Nullable String id, @Nullable ResourceLocation texture) {
      Preconditions.checkArgument(id == null || texture == null, "Cannot have both id and texture defined at once. Only one of them should be present.");
      this.id = id;
      this.texture = texture;
   }

   public static ExposureIdentifier id(@NotNull String id) {
      return new ExposureIdentifier(id, null);
   }

   public static ExposureIdentifier texture(@NotNull ResourceLocation texture) {
      return new ExposureIdentifier(null, texture);
   }

   public boolean isEmpty() {
      return this == EMPTY || this.id != null && this.id.isEmpty();
   }

   @Nullable
   public String id() {
      return this.id;
   }

   public Optional<String> getId() {
      return Optional.ofNullable(this.id);
   }

   @Nullable
   public ResourceLocation texture() {
      return this.texture;
   }

   public Optional<ResourceLocation> getTexture() {
      return Optional.ofNullable(this.texture);
   }

   @Contract
   public boolean isId() {
      return this.id != null;
   }

   public boolean isTexture() {
      return this.texture != null;
   }

   public <T> T map(Function<String, T> ifId, Function<ResourceLocation, T> ifTexture) {
      return this.isId() ? ifId.apply(this.id) : ifTexture.apply(this.texture());
   }

   public ExposureIdentifier ifId(Consumer<String> idConsumer) {
      if (this.isId()) {
         idConsumer.accept(this.id);
      }

      return this;
   }

   public <T> Optional<T> mapId(Function<String, T> mappingFunc) {
      return this.isId() ? Optional.of(mappingFunc.apply(this.id)) : Optional.empty();
   }

   public ExposureIdentifier ifTexture(Consumer<ResourceLocation> textureConsumer) {
      if (this.isTexture()) {
         textureConsumer.accept(this.texture());
      }

      return this;
   }

   public <T> Optional<T> mapTexture(Function<ResourceLocation, T> mappingFunc) {
      return this.isTexture() ? Optional.of(mappingFunc.apply(this.texture)) : Optional.empty();
   }

   public String toValueString() {
      return this.map(Function.identity(), ResourceLocation::toString);
   }

   @Override
   public String toString() {
      return this.isEmpty() ? "" : this.<String>map(id -> "Id: " + id, texture -> "Texture: " + texture);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ExposureIdentifier that = (ExposureIdentifier)o;
         return Objects.equals(this.id, that.id) && Objects.equals(this.texture, that.texture);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.texture);
   }

   public static String createId(Entity entity, String... middleParts) {
      if (entity.level.getGameTime() != lastTick) {
         lastTick = entity.level.getGameTime();
         sameTickCounter = 0;
      }

      List<String> parts = new ArrayList<>();
      parts.add(entity.getName().getString());
      parts.addAll(Arrays.asList(middleParts));
      parts.add(Long.toString(entity.level.getGameTime()));
      if (sameTickCounter > 0) {
         parts.add(Integer.toString(sameTickCounter));
      }

      sameTickCounter++;
      return createId(parts.toArray(String[]::new));
   }

   public static String createId(String... parts) {
      Preconditions.checkArgument(parts.length > 0, "Cannot compose ID with 0 parts.");
      List<String> sanitizedParts = Arrays.stream(parts).filter(s -> !StringUtil.isNullOrEmpty(s)).map(s -> s.replaceAll("[^A-Za-z0-9-]", "-")).toList();
      return String.join("_", sanitizedParts);
   }
}
