package com.SaintAlfonzo.creativedelight.hearth;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 1.4.1 sounds the port uses: the fart and the crow's four (two with their own files, two pointing at the parrot's). */
public final class HHSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HearthAndHarvestMod.MODID);
    public static final RegistryObject<SoundEvent> FART = SOUNDS.register("entities.fart",
            () -> new SoundEvent(new ResourceLocation(HearthAndHarvestMod.MODID, "entities.fart")));
    public static final RegistryObject<SoundEvent> CROW_SQUAWK = sound("entities.crow.squawking");
    public static final RegistryObject<SoundEvent> CROW_HURT = sound("entities.crow.hurt");
    public static final RegistryObject<SoundEvent> CROW_EAT = sound("entities.crow.eat");
    public static final RegistryObject<SoundEvent> CROW_STEP = sound("entities.crow.step");

    private static RegistryObject<SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(HearthAndHarvestMod.MODID, id)));
    }

    private HHSounds() {
    }
}
