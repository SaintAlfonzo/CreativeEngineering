package io.github.mortuusars.exposure.forge.integration.kubejs.event;

import dev.latvian.mods.kubejs.entity.EntityEventJS;
import io.github.mortuusars.exposure.forge.api.event.ModifyEntityInFrameDataEvent;
import io.github.mortuusars.exposure.util.ExtraData;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** KubeJS 1902 event exposing the Forge event's getters; {@link #getData()} is the live, modifiable data. */
public class ModifyEntityInFrameExtraDataEventJS extends EntityEventJS {
   private final ModifyEntityInFrameDataEvent event;

   public ModifyEntityInFrameExtraDataEventJS(ModifyEntityInFrameDataEvent event) {
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

   public LivingEntity getEntityInFrame() {
      return this.event.getEntityInFrame();
   }

   public ExtraData getData() {
      return this.event.getData();
   }
}
