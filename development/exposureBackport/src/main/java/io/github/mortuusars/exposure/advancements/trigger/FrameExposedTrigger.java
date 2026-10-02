package io.github.mortuusars.exposure.advancements.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.advancements.predicate.CameraPredicate;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

/** 1.19.2 shape of upstream's codec-based trigger: same JSON fields ({@code camera, frame, location_in_frame, entities_in_frame}). */
public class FrameExposedTrigger extends SimpleCriterionTrigger<FrameExposedTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public FrameExposedTrigger(ResourceLocation id) {
      this.id = id;
   }

   public @NotNull ResourceLocation getId() {
      return this.id;
   }

   protected @NotNull TriggerInstance createInstance(JsonObject json, EntityPredicate.@NotNull Composite player, @NotNull DeserializationContext context) {
      Optional<CameraPredicate> camera = parseOptional(json, "camera", CameraPredicate.CODEC);
      Optional<FramePredicate> frame = parseOptional(json, "frame", FramePredicate.CODEC);
      Optional<LocationPredicate> locationInFrame = json.has("location_in_frame") ? Optional.of(LocationPredicate.fromJson(json.get("location_in_frame"))) : Optional.empty();
      Optional<List<EntityPredicate.Composite>> entitiesInFrame = json.has("entities_in_frame")
              ? Optional.of(Arrays.asList(EntityPredicate.Composite.fromJsonArray(json, "entities_in_frame", context)))
              : Optional.empty();
      return new TriggerInstance(this.id, player, camera, frame, locationInFrame, entitiesInFrame);
   }

   static <T> Optional<T> parseOptional(JsonObject json, String key, Codec<T> codec) {
      JsonElement element = json.get(key);
      if (element == null) {
         return Optional.empty();
      }
      return Optional.of(codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, err -> {
         throw new JsonSyntaxException("Invalid '" + key + "': " + err);
      }));
   }

   public void trigger(ServerPlayer player, CameraHolder cameraHolder, ItemStack cameraStack, Frame frame, List<BlockPos> locationsInFrame, List<LivingEntity> entitiesInFrame) {
      this.trigger(player, (triggerInstance) -> triggerInstance.matches(player, cameraHolder, cameraStack, frame, locationsInFrame, entitiesInFrame));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final Optional<CameraPredicate> camera;
      private final Optional<FramePredicate> frame;
      private final Optional<LocationPredicate> locationInFrame;
      private final Optional<List<EntityPredicate.Composite>> entitiesInFrame;

      public TriggerInstance(ResourceLocation id, EntityPredicate.Composite player, Optional<CameraPredicate> camera, Optional<FramePredicate> frame, Optional<LocationPredicate> locationInFrame, Optional<List<EntityPredicate.Composite>> entitiesInFrame) {
         super(id, player);
         this.camera = camera;
         this.frame = frame;
         this.locationInFrame = locationInFrame;
         this.entitiesInFrame = entitiesInFrame;
      }

      public Optional<CameraPredicate> camera() {
         return this.camera;
      }

      public Optional<FramePredicate> frame() {
         return this.frame;
      }

      public Optional<LocationPredicate> locationInFrame() {
         return this.locationInFrame;
      }

      public Optional<List<EntityPredicate.Composite>> entitiesInFrame() {
         return this.entitiesInFrame;
      }

      public boolean matches(ServerPlayer player, CameraHolder cameraHolder, ItemStack cameraStack, Frame frame, List<BlockPos> locationsInFrame, List<LivingEntity> entitiesInFrame) {
         return (this.camera.isEmpty() || this.camera.get().matches(player.getLevel(), cameraStack, cameraHolder.asHolderEntity().position())) && (this.frame.isEmpty() || this.frame.get().matches(frame)) && this.locationsMatch(player, locationsInFrame) && this.entitiesInFrameMatch(player, cameraHolder, entitiesInFrame);
      }

      private boolean locationsMatch(ServerPlayer player, List<BlockPos> locationsInFrame) {
         return this.locationInFrame.isEmpty() || locationsInFrame.stream().anyMatch((pos) -> this.locationInFrame.get().matches(player.getLevel(), (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D));
      }

      private boolean entitiesInFrameMatch(ServerPlayer player, CameraHolder cameraHolder, List<LivingEntity> entitiesInFrame) {
         return this.entitiesInFrame.isEmpty() || this.entitiesInFrame.get().stream().allMatch((predicate) -> entitiesInFrame.stream().anyMatch((entity) -> {
               LootContext context = createContextForHolder(player.getLevel(), cameraHolder, entity);
               return predicate.matches(context);
            }));
      }

      public static LootContext createContextForHolder(ServerLevel level, CameraHolder holder, Entity entity) {
         return new LootContext.Builder(level).withParameter(LootContextParams.THIS_ENTITY, entity)
                 .withParameter(LootContextParams.ORIGIN, holder.asHolderEntity().position())
                 .withRandom(level.getRandom()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
      }

      public @NotNull JsonObject serializeToJson(@NotNull SerializationContext context) {
         JsonObject json = super.serializeToJson(context);
         this.camera.ifPresent(c -> json.add("camera", CameraPredicate.CODEC.encodeStart(JsonOps.INSTANCE, c).result().orElseThrow()));
         this.frame.ifPresent(f -> json.add("frame", FramePredicate.CODEC.encodeStart(JsonOps.INSTANCE, f).result().orElseThrow()));
         this.locationInFrame.ifPresent(l -> json.add("location_in_frame", l.serializeToJson()));
         this.entitiesInFrame.ifPresent(list -> json.add("entities_in_frame", EntityPredicate.Composite.toJson(list.toArray(new EntityPredicate.Composite[0]), context)));
         return json;
      }
   }
}
