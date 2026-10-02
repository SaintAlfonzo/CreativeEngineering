package io.github.mortuusars.exposure.forge.api.event;

import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ModifyFrameExtraDataEvent extends Event {
   private final CameraHolder cameraHolder;
   private final ItemStack stack;
   private final CaptureParameters captureParameters;
   private final List<BlockPos> positionsInFrame;
   private final List<LivingEntity> entitiesInFrame;
   private final ExtraData data;

   public ModifyFrameExtraDataEvent(
      CameraHolder cameraHolder,
      ItemStack stack,
      CaptureParameters captureParameters,
      List<BlockPos> positionsInFrame,
      List<LivingEntity> entitiesInFrame,
      ExtraData data
   ) {
      this.cameraHolder = cameraHolder;
      this.stack = stack;
      this.captureParameters = captureParameters;
      this.positionsInFrame = positionsInFrame;
      this.entitiesInFrame = entitiesInFrame;
      this.data = data;
   }

   public CameraHolder getCameraHolder() {
      return this.cameraHolder;
   }

   public Entity getCameraHolderEntity() {
      return this.cameraHolder.asHolderEntity();
   }

   public ItemStack getCamera() {
      return this.stack;
   }

   public CaptureParameters getCaptureProperties() {
      return this.captureParameters;
   }

   public List<BlockPos> getPositionsInFrame() {
      return this.positionsInFrame;
   }

   public List<LivingEntity> getEntitiesInFrame() {
      return this.entitiesInFrame;
   }

   public ExtraData getData() {
      return this.data;
   }
}
