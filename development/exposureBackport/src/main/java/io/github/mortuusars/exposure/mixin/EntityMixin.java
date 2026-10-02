package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.CameraStandTurnC2SP;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public class EntityMixin {
   @Inject(
      method = {"turn(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onTurn(double yRot, double xRot, CallbackInfo ci) {
      if (this instanceof CameraOperator operator) {
         if (((CameraOperator)operator).getActiveExposureCamera() instanceof CameraOnStand cameraOnStand) {
            CameraStandEntity stand = cameraOnStand.getStand();
            stand.turn(yRot, xRot);
            Packets.sendToServer(new CameraStandTurnC2SP(stand.getId(), yRot, xRot));
            ci.cancel();
         }
      }
   }
}
