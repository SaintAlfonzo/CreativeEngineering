package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.camera.frame.EntityInFrame;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.resources.ResourceLocation;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record EntityInFramePredicate(Optional<ResourceLocation> type, Optional<String> name, Ints distance) {
   public static final Codec<EntityInFramePredicate> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ResourceLocation.CODEC.optionalFieldOf("type").forGetter(EntityInFramePredicate::type),
               Codec.STRING.optionalFieldOf("name").forGetter(EntityInFramePredicate::name),
               BackportCodecs.INTS.optionalFieldOf("distance", Ints.ANY).forGetter(EntityInFramePredicate::distance)
            )
            .apply(instance, EntityInFramePredicate::new)
   );

   public boolean matches(EntityInFrame entity) {
      if (this.type.isPresent() && !this.type.get().equals(entity.id())) {
         return false;
      } else {
         return this.name.isPresent() && !this.name.get().equals(entity.name()) ? false : this.distance().matches(entity.distance());
      }
   }

   public boolean matches(List<EntityInFrame> entitiesInFrame) {
      return entitiesInFrame.stream().anyMatch(this::matches);
   }
}
