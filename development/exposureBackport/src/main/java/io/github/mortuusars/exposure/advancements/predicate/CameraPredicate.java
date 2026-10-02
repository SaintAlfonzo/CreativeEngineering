package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.Optional;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record CameraPredicate(
   Optional<ItemPredicate> camera,
   Optional<ItemPredicate> film,
   Optional<ItemPredicate> flash,
   Optional<ItemPredicate> lens,
   Optional<ItemPredicate> filter,
   Optional<LocationPredicate> location
) {
   public static final Codec<CameraPredicate> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               BackportCodecs.ITEM_PREDICATE.optionalFieldOf("camera").forGetter(CameraPredicate::camera),
               BackportCodecs.ITEM_PREDICATE.optionalFieldOf("film").forGetter(CameraPredicate::film),
               BackportCodecs.ITEM_PREDICATE.optionalFieldOf("flash").forGetter(CameraPredicate::flash),
               BackportCodecs.ITEM_PREDICATE.optionalFieldOf("lens").forGetter(CameraPredicate::lens),
               BackportCodecs.ITEM_PREDICATE.optionalFieldOf("filter").forGetter(CameraPredicate::filter),
               BackportCodecs.LOCATION_PREDICATE.optionalFieldOf("location").forGetter(CameraPredicate::location)
            )
            .apply(instance, CameraPredicate::new)
   );

   public boolean matches(ServerLevel level, ItemStack cameraStack, Vec3 cameraLocation) {
      return !(cameraStack.getItem() instanceof CameraItem)
         ? false
         : (this.camera.isEmpty() || this.camera.get().matches(cameraStack))
            && (this.film.isEmpty() || this.film.get().matches(Attachment.FILM.get(cameraStack).getForReading()))
            && (this.flash.isEmpty() || this.flash.get().matches(Attachment.FLASH.get(cameraStack).getForReading()))
            && (this.lens.isEmpty() || this.lens.get().matches(Attachment.LENS.get(cameraStack).getForReading()))
            && (this.filter.isEmpty() || this.filter.get().matches(Attachment.FILTER.get(cameraStack).getForReading()))
            && (this.location.isEmpty() || this.location.get().matches(level, cameraLocation.x, cameraLocation.y, cameraLocation.z));
   }
}
