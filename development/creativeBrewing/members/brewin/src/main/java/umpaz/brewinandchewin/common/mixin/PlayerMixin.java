package umpaz.brewinandchewin.common.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.common.access.FoodDataEntityAccess;

@Mixin(Player.class)
public class PlayerMixin {
   @Shadow
   protected FoodData foodData;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void brewinandchewin$storePlayerIntoFoodData(CallbackInfo ci) {
      ((FoodDataEntityAccess)this.foodData).brewinandchewin$setEntity((Player)(Object)this);
   }
}
