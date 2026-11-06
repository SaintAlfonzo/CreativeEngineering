package net.mehdinoui.fungidelight.common.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundEvents {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "fungidelight");
   public static final RegistryObject<SoundEvent> PIG_HAPPY = SOUND_EVENTS.register(
      "pig_happy", () -> new SoundEvent(new ResourceLocation("fungidelight", "pig_happy"))
   );
   public static final RegistryObject<SoundEvent> PIG_DIG = SOUND_EVENTS.register(
      "pig_dig", () -> new SoundEvent(new ResourceLocation("fungidelight", "pig_dig"))
   );
   public static final RegistryObject<SoundEvent> PIG_DROP = SOUND_EVENTS.register(
      "pig_drop", () -> new SoundEvent(new ResourceLocation("fungidelight", "pig_drop"))
   );
   public static final RegistryObject<SoundEvent> DOG_SNIFF_TRUFFLE = SOUND_EVENTS.register(
      "dog_sniff_truffle", () -> new SoundEvent(new ResourceLocation("fungidelight", "dog_sniff_truffle"))
   );
   public static final RegistryObject<SoundEvent> DOG_FOUND_TRUFFLE = SOUND_EVENTS.register(
      "dog_found_truffle", () -> new SoundEvent(new ResourceLocation("fungidelight", "dog_found_truffle"))
   );
   public static final RegistryObject<SoundEvent> DOG_ALERT_TRUFFLE = SOUND_EVENTS.register(
      "dog_alert_truffle", () -> new SoundEvent(new ResourceLocation("fungidelight", "dog_alert_truffle"))
   );

   public static void register(IEventBus eventBus) {
      SOUND_EVENTS.register(eventBus);
   }
}
