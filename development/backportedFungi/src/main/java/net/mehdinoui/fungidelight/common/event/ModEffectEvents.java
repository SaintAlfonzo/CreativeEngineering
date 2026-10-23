package net.mehdinoui.fungidelight.common.event;

import java.util.Objects;
import net.mehdinoui.fungidelight.common.registry.ModEffects;
import net.mehdinoui.fungidelight.common.tag.FungiDelightTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "fungidelight"
)
// Port change: no config switches; the configured extra item list is gone. Alcohol is the pack's tag
// (FungiDelightTags.ALCOHOL); any non-water potion still triggers the reaction.
public class ModEffectEvents {
   @SubscribeEvent
   public static void onBreakSpeed(BreakSpeed event) {
      if (event.getEntity().hasEffect((MobEffect)ModEffects.BURROWING.get())) {
         if (event.getState().is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            int amplifier = Objects.requireNonNull(event.getEntity().getEffect((MobEffect)ModEffects.BURROWING.get())).getAmplifier();
            float multiplier = 1.5F + 0.5F * (float)amplifier;
            event.setNewSpeed(event.getOriginalSpeed() * multiplier);
         }
      }
   }

   @SubscribeEvent
   public static void onFoodEaten(Finish event) {
      if (!event.getEntity().getLevel().isClientSide) {
         ItemStack consumedItem = event.getItem();
         if (event.getEntity().hasEffect((MobEffect)ModEffects.WEAK_STOMACH.get())) {
            boolean isPotion = consumedItem.getItem() instanceof PotionItem && PotionUtils.getPotion(consumedItem) != Potions.WATER;
            boolean isAlcohol = consumedItem.is(FungiDelightTags.ALCOHOL);
            if (isPotion || isAlcohol) {
               triggerReaction(event.getEntity());
            }
         } else {
            boolean isCoprine = consumedItem.is(FungiDelightTags.ALCOHOL_REACTION);
            if (isCoprine) {
               event.getEntity().addEffect(new MobEffectInstance((MobEffect)ModEffects.WEAK_STOMACH.get(), 1200, 0));
            }
         }
      }
   }

   private static void triggerReaction(LivingEntity entity) {
      entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
      entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
      entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
   }
}
