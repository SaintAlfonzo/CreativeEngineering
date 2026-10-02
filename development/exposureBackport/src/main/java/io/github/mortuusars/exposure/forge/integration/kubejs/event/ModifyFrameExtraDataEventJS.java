package io.github.mortuusars.exposure.forge.integration.kubejs.event;

import dev.latvian.mods.kubejs.entity.EntityEventJS;
import io.github.mortuusars.exposure.forge.api.event.ModifyFrameExtraDataEvent;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** KubeJS 1902 event exposing the Forge event's getters; {@link #getData()} is the live, modifiable data. */
public class ModifyFrameExtraDataEventJS extends EntityEventJS {
   private final ModifyFrameExtraDataEvent event;

   public ModifyFrameExtraDataEventJS(ModifyFrameExtraDataEvent event) {
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

   public CaptureParameters getCaptureProperties() {
      return this.event.getCaptureProperties();
   }

   public List<BlockPos> getPositionsInFrame() {
      return this.event.getPositionsInFrame();
   }

   public List<LivingEntity> getEntitiesInFrame() {
      return this.event.getEntitiesInFrame();
   }

   public ExtraData getData() {
      return this.event.getData();
   }
}
