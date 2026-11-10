package com.SaintAlfonzo.creativedelight.hearth;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The one 1.4.1 sound with its own file that the port uses so far; the throw and hit sounds are vanilla events upstream too. */
public final class HHSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HearthAndHarvestMod.MODID);
    public static final RegistryObject<SoundEvent> FART = SOUNDS.register("entities.fart",
            () -> new SoundEvent(new ResourceLocation(HearthAndHarvestMod.MODID, "entities.fart")));

    private HHSounds() {
    }
}
