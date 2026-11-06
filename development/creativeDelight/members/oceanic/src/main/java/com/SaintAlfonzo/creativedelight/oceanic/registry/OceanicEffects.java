package com.SaintAlfonzo.creativedelight.oceanic.registry;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.SaintAlfonzo.creativedelight.oceanic.content.InstantBreathEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Instant Breath and the three Mining Fatigue potions (upstream ids). */
public final class OceanicEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, OceanicDelightMod.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, OceanicDelightMod.MODID);

    public static final RegistryObject<MobEffect> INSTANT_BREATH = EFFECTS.register("instant_breath", InstantBreathEffect::new);

    /** 2:00, 6:00, and level II for 1:00. */
    public static final RegistryObject<Potion> MINING_FATIGUE = POTIONS.register("mining_fatigue",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 2400, 0)));
    public static final RegistryObject<Potion> MINING_FATIGUE_EXTENDED = POTIONS.register("mining_fatigue_extended",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 7200, 0)));
    public static final RegistryObject<Potion> MINING_FATIGUE_TWO = POTIONS.register("mining_fatigue_two",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 1)));

    private OceanicEffects() {
    }
}
