package io.github.mortuusars.exposure.forge.api.event;

import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class FrameAddedEvent extends Event {
   private final CameraHolder cameraHolder;
   private final ItemStack camera;
   private final Frame frame;
   private final List<BlockPos> positionsInFrame;
   private final List<LivingEntity> entitiesInFrame;

   public FrameAddedEvent(CameraHolder cameraHolder, ItemStack camera, Frame frame, List<BlockPos> positionsInFrame, List<LivingEntity> entitiesInFrame) {
      this.cameraHolder = cameraHolder;
      this.camera = camera;
      this.frame = frame;
      this.positionsInFrame = positionsInFrame;
      this.entitiesInFrame = entitiesInFrame;
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

   public Frame getFrame() {
      return this.frame;
   }

   public List<BlockPos> getPositionsInFrame() {
      return this.positionsInFrame;
   }

   public List<LivingEntity> getEntitiesInFrame() {
      return this.entitiesInFrame;
   }
}
