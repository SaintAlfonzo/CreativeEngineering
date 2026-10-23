package net.mehdinoui.fungidelight.common.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {
   public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, "fungidelight");
   public static final RegistryObject<Potion> BURROWING_POTION = POTIONS.register(
      "burrowing", () -> new Potion(new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.BURROWING.get(), 3600)})
   );
   public static final RegistryObject<Potion> LONG_BURROWING_POTION = POTIONS.register(
      "long_burrowing", () -> new Potion("burrowing", new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.BURROWING.get(), 9600)})
   );
   public static final RegistryObject<Potion> STRONG_BURROWING_POTION = POTIONS.register(
      "strong_burrowing", () -> new Potion("burrowing", new MobEffectInstance[]{new MobEffectInstance((MobEffect)ModEffects.BURROWING.get(), 1800, 1)})
   );

   public static void register(IEventBus eventBus) {
      POTIONS.register(eventBus);
   }
}
