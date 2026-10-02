package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.MinMaxBounds;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record FramePredicate(Optional<ExposureIdentifier> identifier, Optional<String> type, Optional<String> photographer, Optional<ShutterSpeedPredicate> shutterSpeed, Optional<MinMaxBounds.Ints> focalLength, Optional<MinMaxBounds.Ints> lightLevel, Optional<MinMaxBounds.Ints> dayTime, Optional<MinMaxBounds.Ints> entitiesInFrameCount, Optional<List<EntityInFramePredicate>> entitiesInFrame, Optional<ExtraDataPredicate> extraData) {
   public static final Codec<FramePredicate> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ExposureIdentifier.CODEC.optionalFieldOf("identifier").forGetter(FramePredicate::identifier), Codec.STRING.optionalFieldOf("type").forGetter(FramePredicate::type), Codec.STRING.optionalFieldOf("photographer").forGetter(FramePredicate::photographer), ShutterSpeedPredicate.CODEC.optionalFieldOf("shutter_speed").forGetter(FramePredicate::shutterSpeed), BackportCodecs.INTS.optionalFieldOf("focal_length").forGetter(FramePredicate::focalLength), BackportCodecs.INTS.optionalFieldOf("light_level").forGetter(FramePredicate::lightLevel), BackportCodecs.INTS.optionalFieldOf("day_time").forGetter(FramePredicate::dayTime), BackportCodecs.INTS.optionalFieldOf("entities_in_frame_count").forGetter(FramePredicate::entitiesInFrameCount), EntityInFramePredicate.CODEC.listOf().optionalFieldOf("entities_in_frame").forGetter(FramePredicate::entitiesInFrame), ExtraDataPredicate.CODEC.optionalFieldOf("extra_data").forGetter(FramePredicate::extraData)).apply(instance, FramePredicate::new));

   public @NotNull DataComponentType<Frame> componentType() {
      return Exposure.DataComponents.PHOTOGRAPH_FRAME;
   }

   /**
    * Forge custom item predicate (1.21: item sub-predicate {@code "predicates": {"exposure:frame": {...}}}):
    * {@code {"type": "exposure:frame", "frame": {...}, <vanilla item predicate fields>}}. Matches stacks whose
    * {@code photograph_frame} component passes this predicate and the vanilla part.
    */
   public static ItemPredicate fromItemPredicateJson(JsonObject json) {
      JsonObject vanillaPart = json.deepCopy();
      vanillaPart.remove("type");
      JsonElement frameJson = vanillaPart.remove("frame");
      FramePredicate frame = frameJson == null ? new FramePredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
              : CODEC.parse(JsonOps.INSTANCE, frameJson).getOrThrow(false, err -> {
                 throw new JsonSyntaxException("Invalid exposure:frame predicate: " + err);
              });
      ItemPredicate vanilla = vanillaPart.size() > 0 ? ItemPredicate.fromJson(vanillaPart) : ItemPredicate.ANY;
      return new ItemPredicate() {
         @Override
         public boolean matches(@NotNull ItemStack stack) {
            if (!vanilla.matches(stack)) {
               return false;
            }
            Frame stackFrame = Exposure.DataComponents.PHOTOGRAPH_FRAME.get(stack);
            return stackFrame != null && frame.matches(stack, stackFrame);
         }

         @Override
         public @NotNull JsonElement serializeToJson() {
            JsonElement vanillaJson = vanilla.serializeToJson();
            JsonObject out = vanillaJson.isJsonObject() ? vanillaJson.getAsJsonObject() : new JsonObject();
            out.addProperty("type", Exposure.ItemSubPredicates.FRAME.toString());
            out.add("frame", CODEC.encodeStart(JsonOps.INSTANCE, frame).result().orElseGet(JsonObject::new));
            return out;
         }
      };
   }

   public boolean matches(ItemStack stack, Frame frame) {
      return this.matches(frame);
   }

   public boolean matches(Frame frame) {
      return (this.identifier.isEmpty() || ((ExposureIdentifier)this.identifier.get()).equals(frame.identifier())) && (this.type.isEmpty() || ((String)this.type.get()).equals(frame.type().getSerializedName())) && (this.photographer.isEmpty() || ((String)this.photographer.get()).equals(frame.photographer().name())) && (this.shutterSpeed.isEmpty() || ((ShutterSpeedPredicate)this.shutterSpeed.get()).matches(frame.extraData().get(Frame.SHUTTER_SPEED))) && (this.focalLength.isEmpty() || frame.extraData().get(Frame.FOCAL_LENGTH).map(((MinMaxBounds.Ints)this.focalLength.get())::matches).orElse(false)) && (this.lightLevel.isEmpty() || frame.extraData().get(Frame.LIGHT_LEVEL).map(((MinMaxBounds.Ints)this.lightLevel.get())::matches).orElse(false)) && (this.dayTime.isEmpty() || frame.extraData().get(Frame.DAY_TIME).map(((MinMaxBounds.Ints)this.dayTime.get())::matches).orElse(false)) && (this.entitiesInFrameCount.isEmpty() || ((MinMaxBounds.Ints)this.entitiesInFrameCount.get()).matches(frame.entitiesInFrame().size())) && (this.entitiesInFrame.isEmpty() || (this.entitiesInFrame.get()).stream().allMatch((predicate) -> predicate.matches(frame.entitiesInFrame()))) && (this.extraData.isEmpty() || ((ExtraDataPredicate)this.extraData.get()).matches(frame.extraData()));
   }
}
