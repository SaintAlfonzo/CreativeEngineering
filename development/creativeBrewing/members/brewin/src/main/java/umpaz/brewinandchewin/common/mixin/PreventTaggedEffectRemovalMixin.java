package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.item.HotCocoaItem;
import vectorwing.farmersdelight.common.item.MilkBottleItem;

/**
 * Farmer's Delight's milk bottle (and hot cocoa) removes one random curable effect. An effect in the matching
 * low_priority tag only counts once the consumer has no effect outside it (Tipsy goes last). Port: FD 1.2.4 builds its
 * candidate list with MobEffectInstance#isCurativeItem, where FD 1.21 uses a Set lookup.
 */
public class PreventTaggedEffectRemovalMixin {
   static boolean brewinandchewin$lowPriority(MobEffectInstance instance, LivingEntity consumer, TagKey<MobEffect> tag) {
      return Registry.MOB_EFFECT.getHolderOrThrow(Registry.MOB_EFFECT.getResourceKey(instance.getEffect()).orElseThrow()).is(tag)
         && consumer.getActiveEffects()
            .stream()
            .anyMatch(effect -> !Registry.MOB_EFFECT.getHolderOrThrow(Registry.MOB_EFFECT.getResourceKey(effect.getEffect()).orElseThrow()).is(tag));
   }

   @Mixin(HotCocoaItem.class)
   public static class HotCocoa {
      @ModifyExpressionValue(
         method = "affectConsumer",
         at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;isCurativeItem(Lnet/minecraft/world/item/ItemStack;)Z"),
         remap = false
      )
      private boolean brewinandchewin$preventLowPriorityRemoval(boolean original, @Local(argsOnly = true) LivingEntity consumer, @Local MobEffectInstance effectInstance) {
         return original && !brewinandchewin$lowPriority(effectInstance, consumer, BnCTags.Effects.HOT_COCOA_LOW_PRIORITY);
      }
   }

   @Mixin(MilkBottleItem.class)
   public static class MilkBottle {
      @ModifyExpressionValue(
         method = "affectConsumer",
         at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;isCurativeItem(Lnet/minecraft/world/item/ItemStack;)Z"),
         remap = false
      )
      private boolean brewinandchewin$preventLowPriorityRemoval(boolean original, @Local(argsOnly = true) LivingEntity consumer, @Local MobEffectInstance effectInstance) {
         return original && !brewinandchewin$lowPriority(effectInstance, consumer, BnCTags.Effects.MILK_BOTTLE_LOW_PRIORITY);
      }
   }
}
