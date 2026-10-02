package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Upstream caps {@code getMaxHeadRotationRelativeToBody()} (1.20.2+) at 20 while a camera is active, so the body
 * follows the head. 1.19.2 hard-codes the 75 degree limit in {@code tickHeadTurn}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @ModifyConstant(method = "tickHeadTurn(FF)F", constant = @Constant(floatValue = 75.0F))
   private float onMaxHeadRotation(float original) {
      return exposure$limit(original);
   }

   @ModifyConstant(method = "tickHeadTurn(FF)F", constant = @Constant(floatValue = -75.0F))
   private float onMinHeadRotation(float original) {
      return -exposure$limit(-original);
   }

   private float exposure$limit(float original) {
      if (this instanceof CameraOperator operator && ((CameraOperator)operator).getActiveExposureCamera() != null) {
         return Math.min(original, 20.0F);
      }
      return original;
   }
}
