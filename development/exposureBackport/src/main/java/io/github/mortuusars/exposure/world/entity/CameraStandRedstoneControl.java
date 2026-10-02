package io.github.mortuusars.exposure.world.entity;

import net.minecraft.nbt.CompoundTag;

public class CameraStandRedstoneControl {
   public int delay = 2;
   protected CameraStandEntity stand;
   protected boolean hasSignal;
   protected int releaseDelay;

   public CameraStandRedstoneControl(CameraStandEntity stand) {
      this.stand = stand;
   }

   public boolean tick() {
      boolean hasSignal = this.stand.level.hasNeighborSignal(this.stand.blockPosition());
      if (hasSignal && !this.hasSignal && this.releaseDelay <= 0) {
         this.releaseDelay = this.delay;
      }

      boolean released = false;
      if (this.releaseDelay > 0) {
         this.releaseDelay--;
         if (this.releaseDelay == 0) {
            this.stand.release();
            released = true;
         }
      }

      this.hasSignal = hasSignal;
      return released;
   }

   public void load(CompoundTag tag) {
      this.hasSignal = tag.getBoolean("HasRedstoneSignal");
      this.releaseDelay = tag.getInt("RedstoneReleaseDelay");
   }

   public void save(CompoundTag tag) {
      tag.putBoolean("HasRedstoneSignal", this.hasSignal);
      tag.putInt("RedstoneReleaseDelay", this.releaseDelay);
   }
}
