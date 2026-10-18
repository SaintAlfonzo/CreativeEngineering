package umpaz.brewinandchewin.common.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public class JamJarItem extends ConsumableItem {
   public JamJarItem(Properties pProperties) {
      super(pProperties);
   }

   @Override
   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.DRINK;
   }

   @Override
   public SoundEvent getDrinkingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   @Override
   public SoundEvent getEatingSound() {
      return SoundEvents.HONEY_DRINK;
   }
}
