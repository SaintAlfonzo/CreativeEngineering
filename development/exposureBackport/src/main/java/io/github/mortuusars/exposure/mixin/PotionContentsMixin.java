package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.Config;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Distinct colors for the effect-less developing potions (mundane, awkward, thick), which vanilla draws in the water
 * color. Upstream hooks 1.20.5's {@code PotionContents.getColor()}; 1.19.2 colors potions in {@code PotionUtils}.
 */
@Mixin(PotionUtils.class)
public abstract class PotionContentsMixin {
   private static final int EXPOSURE$WATER_COLOR = 0x385DC6;

   @Inject(method = "getColor(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
   private static void onGetStackColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
      exposure$recolor(PotionUtils.getPotion(stack), cir);
   }

   @Inject(method = "getColor(Lnet/minecraft/world/item/alchemy/Potion;)I", at = @At("RETURN"), cancellable = true)
   private static void onGetPotionColor(Potion potion, CallbackInfoReturnable<Integer> cir) {
      exposure$recolor(potion, cir);
   }

   private static void exposure$recolor(Potion potion, CallbackInfoReturnable<Integer> cir) {
      if (!Config.Common.SPEC.isLoaded() || !Config.Common.DIFFERENT_DEVELOPING_POTION_COLORS.get()
              || (cir.getReturnValue() & 0xFFFFFF) != EXPOSURE$WATER_COLOR) {
         return;
      }
      // upstream's ARGB colors, as 1.19.2's RGB
      if (potion == Potions.MUNDANE) {
         cir.setReturnValue(-12431985 & 0xFFFFFF);
      } else if (potion == Potions.AWKWARD) {
         cir.setReturnValue(-10144364 & 0xFFFFFF);
      } else if (potion == Potions.THICK) {
         cir.setReturnValue(-12683390 & 0xFFFFFF);
      }
   }
}
