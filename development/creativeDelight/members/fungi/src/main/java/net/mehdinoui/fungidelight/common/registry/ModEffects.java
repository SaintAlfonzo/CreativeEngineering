package net.mehdinoui.fungidelight.common.registry;

import net.mehdinoui.fungidelight.common.effect.BurrowingEffect;
import net.mehdinoui.fungidelight.common.effect.WeakStomachEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
   public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "fungidelight");
   public static final RegistryObject<MobEffect> BURROWING = EFFECTS.register("burrowing", BurrowingEffect::new);
   public static final RegistryObject<MobEffect> WEAK_STOMACH = EFFECTS.register("weak_stomach", WeakStomachEffect::new);

   public static void register(IEventBus eventBus) {
      EFFECTS.register(eventBus);
   }
}
