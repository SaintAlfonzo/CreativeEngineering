package umpaz.brewinandchewin.common.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;

// Port: CODEC -> save / load (the Forge capability writes NBT).
public class TipsyHeartsAttachment {
   public static final ResourceLocation ID = BrewinAndChewin.asResource("tipsy_hearts");
   private float numbedHealth;
   private int ticksUntilDamage;

   public TipsyHeartsAttachment(float numbedHealth, int ticksUntilDamage) {
      this.numbedHealth = numbedHealth;
      this.ticksUntilDamage = ticksUntilDamage;
   }

   public float getNumbedHealth() {
      return this.numbedHealth;
   }

   public void setNumbedHealth(float value) {
      this.numbedHealth = value;
   }

   public int getTicksUntilDamage() {
      return this.ticksUntilDamage;
   }

   public void setTicksUntilDamage(int value) {
      this.ticksUntilDamage = value;
   }

   public void setFrom(TipsyHeartsAttachment cap) {
      if (cap != null) {
         this.numbedHealth = cap.numbedHealth;
         this.ticksUntilDamage = cap.ticksUntilDamage;
      }
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putFloat("numbed_health", this.numbedHealth);
      tag.putInt("ticks_until_damage", this.ticksUntilDamage);
      return tag;
   }

   public static TipsyHeartsAttachment load(CompoundTag tag) {
      return new TipsyHeartsAttachment(tag.getFloat("numbed_health"), tag.getInt("ticks_until_damage"));
   }
}
