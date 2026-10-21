package umpaz.brewinandchewin.common.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.access.FoodDataEntityAccess;
import umpaz.brewinandchewin.common.registry.BnCEffects;

// Port: 1.21 FoodData.add(int, float) -> 1.19.2 FoodData.eat(int, float); the float is the saturation modifier, so a
// zero still fills hunger and gives no saturation, as upstream.
@Mixin(FoodData.class)
public class IntoxicationCancelSaturationMixin implements FoodDataEntityAccess {
   @Unique
   private LivingEntity brewinandchewin$entity;

   @ModifyVariable(method = "eat(IF)V", at = @At("HEAD"), argsOnly = true, index = 2)
   private float brewinandchewin$disableSaturation(float value) {
      return this.brewinandchewin$entity != null && this.brewinandchewin$entity.hasEffect(BnCEffects.INTOXICATION) && value > 0.0F ? 0.0F : value;
   }

   @Override
   public void brewinandchewin$setEntity(LivingEntity entity) {
      this.brewinandchewin$entity = entity;
   }
}
