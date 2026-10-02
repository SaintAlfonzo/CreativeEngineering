package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraOnStandSetS2CP;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.world.item.ItemStack;

public class CameraOnStand extends Camera {
   protected final CameraOperator cameraOperator;
   protected final CameraStandEntity cameraStand;

   public CameraOnStand(CameraOperator cameraOperator, CameraStandEntity cameraStand, CameraId id) {
      super(cameraStand, id);
      this.cameraOperator = cameraOperator;
      this.cameraStand = cameraStand;
   }

   public CameraOperator getOperator() {
      return this.cameraOperator;
   }

   public CameraStandEntity getStand() {
      return this.cameraStand;
   }

   @Override
   public ItemStack getItemStack() {
      return this.cameraStand.getCamera();
   }

   @Override
   public void release() {
      this.getStand().release();
   }

   @Override
   public Packet createSyncPacket() {
      return new ActiveCameraOnStandSetS2CP(this.cameraOperator.asOperatorEntity().getId(), this.cameraStand.getId(), this.id);
   }
}
