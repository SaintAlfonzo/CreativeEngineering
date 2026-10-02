package io.github.mortuusars.exposure.world.camera;

import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.network.packet.clientbound.ActiveCameraInHandSetS2CP;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CameraInHand extends Camera {
   protected final InteractionHand hand;

   public CameraInHand(CameraHolder holder, CameraId cameraId, InteractionHand hand) {
      super(holder, cameraId);
      this.hand = hand;
      if (!(holder instanceof LivingEntity)) {
         throw new IllegalStateException(
            "Only LivingEntity can hold camera in hand." + EntityType.getKey(holder.asHolderEntity().getType()) + " does snot have hands."
         );
      }
   }

   @Override
   public ItemStack getItemStack() {
      return ((LivingEntity)this.getHolder()).getItemInHand(this.getHand());
   }

   @Override
   public boolean deactivate() {
      if (super.deactivate()) {
         return true;
      } else {
         InteractionHand oppositeHand = this.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack itemInHand = ((LivingEntity)this.getHolder()).getItemInHand(oppositeHand);
         if (this.getId().matches(itemInHand) && itemInHand.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(itemInHand)) {
            cameraItem.deactivate(this.getHolder().asHolderEntity(), itemInHand);
            return true;
         } else {
            if (this.getHolder() instanceof Player player) {
               for (ItemStack stack : player.getInventory().items) {
                  if (this.getId().matches(stack) && stack.getItem() instanceof CameraItem cameraItem && cameraItem.isActive(stack)) {
                     cameraItem.deactivate(this.getHolder().asHolderEntity(), stack);
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   @Override
   public Packet createSyncPacket() {
      return new ActiveCameraInHandSetS2CP(this.getHolder().asHolderEntity().getId(), this.getId(), this.getHand());
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   @Nullable
   public static CameraInHand find(CameraHolder holder) {
      if (holder.asHolderEntity() instanceof LivingEntity entity) {
         for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = entity.getItemInHand(hand);
            if (itemInHand.getItem() instanceof CameraItem cameraItem) {
               return new CameraInHand(holder, cameraItem.getOrCreateId(itemInHand), hand);
            }
         }
      }

      return null;
   }
}
