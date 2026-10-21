package umpaz.brewinandchewin.forge.client.integration;

import net.minecraftforge.common.MinecraftForge;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;
import umpaz.brewinandchewin.common.registry.BnCEffects;

/** AppleSkin previews no saturation gain while Intoxicated. Port: the hunger-bar wobble mixin into AppleSkin is left out. */
public class IntoxicationAppleSkinCompat {
   public static void init() {
      MinecraftForge.EVENT_BUS.addListener(IntoxicationAppleSkinCompat::preventSaturationInAppleSkin);
   }

   private static void preventSaturationInAppleSkin(FoodValuesEvent event) {
      if (event.player != null && event.player.hasEffect(BnCEffects.INTOXICATION)) {
         event.modifiedFoodValues = new FoodValues(event.modifiedFoodValues.hunger, 0.0F);
      }
   }
}
