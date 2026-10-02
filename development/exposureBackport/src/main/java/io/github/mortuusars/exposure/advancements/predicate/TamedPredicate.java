package io.github.mortuusars.exposure.advancements.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code "type_specific": {"type": "exposure:tamed", "is_tamed": true}}. 1.19.2's sub-predicate types are a fixed
 * map, so {@code mixin.EntitySubPredicateMixin} hands this type id to {@link #TYPE}.
 */
public record TamedPredicate(boolean isTamed) implements EntitySubPredicate {
   public static final String TYPE_ID = "exposure:tamed";
   public static final EntitySubPredicate.Type TYPE = json -> new TamedPredicate(GsonHelper.getAsBoolean(json, "is_tamed"));

   public boolean matches(@NotNull Entity entity, @NotNull ServerLevel level, @Nullable Vec3 position) {
      return entity instanceof TamableAnimal animal && animal.isTame() == this.isTamed;
   }

   public @NotNull JsonObject serializeCustomData() {
      JsonObject json = new JsonObject();
      json.addProperty("is_tamed", this.isTamed);
      return json;
   }

   public @NotNull JsonElement serialize() {
      JsonObject json = this.serializeCustomData();
      json.addProperty("type", TYPE_ID);
      return json;
   }

   public EntitySubPredicate.@NotNull Type type() {
      return TYPE;
   }
}
