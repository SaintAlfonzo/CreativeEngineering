package net.mehdinoui.fungidelight.mixin;

import net.mehdinoui.fungidelight.common.tag.FungiDelightTags;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Hoglin.class})
public class HoglinMixin {
   @Inject(
      method = {"isFood"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void fungiDelight$isFood(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
      if (pStack.is(FungiDelightTags.TRUFFLES)) {
         cir.setReturnValue(true);
      }
   }
}
