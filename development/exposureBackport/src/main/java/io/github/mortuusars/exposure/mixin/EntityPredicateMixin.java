package io.github.mortuusars.exposure.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.mortuusars.exposure.advancements.predicate.TamedPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1.19.2 entity sub-predicate types are a fixed map; this hands {@code "type_specific": {"type": "exposure:tamed"}}
 * to {@link TamedPredicate} (upstream registers it in the 1.20.5 sub-predicate registry). Mixin 0.8.5 can't inject
 * into the interface's static {@code EntitySubPredicate.fromJson}, so its call here is redirected.
 */
@Mixin(EntityPredicate.class)
public abstract class EntityPredicateMixin {
   @Redirect(method = "fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/advancements/critereon/EntityPredicate;",
           at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/EntitySubPredicate;fromJson(Lcom/google/gson/JsonElement;)Lnet/minecraft/advancements/critereon/EntitySubPredicate;"))
   private static EntitySubPredicate onSubPredicateFromJson(JsonElement json) {
      if (json != null && json.isJsonObject()) {
         JsonObject obj = json.getAsJsonObject();
         if (TamedPredicate.TYPE_ID.equals(GsonHelper.getAsString(obj, "type", null))) {
            return TamedPredicate.TYPE.deserialize(obj);
         }
      }
      return EntitySubPredicate.fromJson(json);
   }
}
