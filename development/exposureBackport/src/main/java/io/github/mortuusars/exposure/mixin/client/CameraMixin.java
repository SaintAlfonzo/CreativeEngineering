package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   private float xRot;
   @Shadow
   private float yRot;

   @Shadow
   protected abstract void move(double x, double y, double z);

   @Shadow
   protected abstract void setRotation(float yRot, float xRot);

   @Inject(method = "getMaxZoom(D)D", at = @At("RETURN"), cancellable = true)
   private void getMaxZoom(double maxZoom, CallbackInfoReturnable<Double> cir) {
      if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
         cir.setReturnValue(Math.min((double)CameraClient.viewfinder().selfie().getMaxCameraDistance(), cir.getReturnValue()));
      }
   }

   @Inject(method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", at = @At("RETURN"))
   private void onSetup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
      if (CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough()) {
         if (detached && thirdPersonReverse && CameraClient.viewfinder().camera().inSelfieMode()) {
            this.setRotation(
               (float)((double)this.yRot + CameraClient.viewfinder().selfie().getCameraYRot()),
               (float)((double)this.xRot + CameraClient.viewfinder().selfie().getCameraXRot())
            );
         }

         if (!detached) {
            double yOffset = CameraClient.viewfinder().camera().map(CameraItem::getYPositionOffset).orElse(0.0);
            this.move(0.0, yOffset, 0.0);
         }
      }
   }
}
