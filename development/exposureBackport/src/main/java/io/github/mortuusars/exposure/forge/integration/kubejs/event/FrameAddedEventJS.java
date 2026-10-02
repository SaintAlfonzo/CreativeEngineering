package io.github.mortuusars.exposure.forge.integration.kubejs.event;

import dev.latvian.mods.kubejs.entity.EntityEventJS;
import io.github.mortuusars.exposure.forge.api.event.FrameAddedEvent;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** KubeJS 1902 event (extends EventJS) exposing the Forge event's getters, as upstream's KubeJS 7 event does. */
public class FrameAddedEventJS extends EntityEventJS {
   private final FrameAddedEvent event;

   public FrameAddedEventJS(FrameAddedEvent event) {
      this.event = event;
   }

   @Override
   public Entity getEntity() {
      return this.event.getCameraHolderEntity();
   }

   public CameraHolder getCameraHolder() {
      return this.event.getCameraHolder();
   }

   public Entity getCameraHolderEntity() {
      return this.event.getCameraHolderEntity();
   }

   public ItemStack getCamera() {
      return this.event.getCamera();
   }

   public Frame getFrame() {
      return this.event.getFrame();
   }

   public List<BlockPos> getPositionsInFrame() {
      return this.event.getPositionsInFrame();
   }

   public List<LivingEntity> getEntitiesInFrame() {
      return this.event.getEntitiesInFrame();
   }
}
