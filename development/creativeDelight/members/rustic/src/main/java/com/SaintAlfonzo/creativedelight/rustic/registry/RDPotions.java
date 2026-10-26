package com.SaintAlfonzo.creativedelight.rustic.registry;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The three Haste potions. Their brewing steps are not registered here: the pack brews them as item-agnostic mixes
 * (kubejs/startup_scripts/cooking/potions.js, RD_POTION_MIXES), so splash, lingering and the Create and IE mixers work.
 */
public final class RDPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, RusticDelightMod.MODID);

    public static final RegistryObject<Potion> HASTE = POTIONS.register("haste", () -> new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 3600)));
    public static final RegistryObject<Potion> LONG_HASTE = POTIONS.register("long_haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 9600)));
    public static final RegistryObject<Potion> STRONG_HASTE = POTIONS.register("strong_haste",
            () -> new Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1)));

    private RDPotions() {
    }
}
