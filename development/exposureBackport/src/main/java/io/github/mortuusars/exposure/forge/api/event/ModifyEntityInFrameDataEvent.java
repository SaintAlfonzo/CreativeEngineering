package io.github.mortuusars.exposure.forge.api.event;

import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ModifyEntityInFrameDataEvent extends Event {
   private final CameraHolder cameraHolder;
   private final ItemStack camera;
   private final LivingEntity entityInFrame;
   private final ExtraData data;

   public ModifyEntityInFrameDataEvent(CameraHolder cameraHolder, ItemStack camera, LivingEntity entityInFrame, ExtraData data) {
      this.cameraHolder = cameraHolder;
      this.camera = camera;
      this.entityInFrame = entityInFrame;
      this.data = data;
   }

   public CameraHolder getCameraHolder() {
      return this.cameraHolder;
   }

   public Entity getCameraHolderEntity() {
      return this.cameraHolder.asHolderEntity();
   }

   public ItemStack getCamera() {
      return this.camera;
   }

   public LivingEntity getEntityInFrame() {
      return this.entityInFrame;
   }

   public ExtraData getData() {
      return this.data;
   }
}
