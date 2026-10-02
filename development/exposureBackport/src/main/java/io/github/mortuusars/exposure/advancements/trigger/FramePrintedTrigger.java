package io.github.mortuusars.exposure.advancements.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.exposure.advancements.predicate.FramePredicate;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import java.util.Optional;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** 1.19.2 shape of upstream's codec-based trigger: same JSON fields ({@code location, frame, item}). */
public class FramePrintedTrigger extends SimpleCriterionTrigger<FramePrintedTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public FramePrintedTrigger(ResourceLocation id) {
      this.id = id;
   }

   public @NotNull ResourceLocation getId() {
      return this.id;
   }

   protected @NotNull TriggerInstance createInstance(JsonObject json, EntityPredicate.@NotNull Composite player, @NotNull DeserializationContext context) {
      Optional<LocationPredicate> location = json.has("location") ? Optional.of(LocationPredicate.fromJson(json.get("location"))) : Optional.empty();
      Optional<FramePredicate> frame = FrameExposedTrigger.parseOptional(json, "frame", FramePredicate.CODEC);
      Optional<ItemPredicate> item = json.has("item") ? Optional.of(BackportCodecs.itemPredicateFromJson(json.get("item"))) : Optional.empty();
      return new TriggerInstance(this.id, player, location, frame, item);
   }

   public void trigger(ServerPlayer player, BlockPos pos, Frame frame, ItemStack result) {
      this.trigger(player, (triggerInstance) -> triggerInstance.matches(player, pos, frame, result));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final Optional<LocationPredicate> location;
      private final Optional<FramePredicate> frame;
      private final Optional<ItemPredicate> item;

      public TriggerInstance(ResourceLocation id, EntityPredicate.Composite player, Optional<LocationPredicate> location, Optional<FramePredicate> frame, Optional<ItemPredicate> item) {
         super(id, player);
         this.location = location;
         this.frame = frame;
         this.item = item;
      }

      public Optional<LocationPredicate> location() {
         return this.location;
      }

      public Optional<FramePredicate> frame() {
         return this.frame;
      }

      public Optional<ItemPredicate> item() {
         return this.item;
      }

      public boolean matches(ServerPlayer player, BlockPos pos, Frame frame, ItemStack result) {
         return (this.location.isEmpty() || this.location.get().matches(player.getLevel(), (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D)) && (this.frame.isEmpty() || this.frame.get().matches(frame)) && (this.item.isEmpty() || this.item.get().matches(result));
      }

      public @NotNull JsonObject serializeToJson(@NotNull SerializationContext context) {
         JsonObject json = super.serializeToJson(context);
         this.location.ifPresent(l -> json.add("location", l.serializeToJson()));
         this.frame.ifPresent(f -> json.add("frame", FramePredicate.CODEC.encodeStart(JsonOps.INSTANCE, f).result().orElseThrow()));
         this.item.ifPresent(i -> json.add("item", i.serializeToJson()));
         return json;
      }
   }
}
