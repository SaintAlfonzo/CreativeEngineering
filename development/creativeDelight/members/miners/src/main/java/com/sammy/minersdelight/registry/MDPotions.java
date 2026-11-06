package com.sammy.minersdelight.registry;

import com.sammy.minersdelight.MinersDelightMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Haste and Mining Fatigue potions (D7): Copper Carrot into an awkward potion, then the usual redstone, glowstone and
 * fermented spider eye steps.
 */
public final class MDPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MinersDelightMod.MODID);

    public static final RegistryObject<Potion> HASTE = potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 3600));
    public static final RegistryObject<Potion> LONG_HASTE = potion("long_haste", new MobEffectInstance(MobEffects.DIG_SPEED, 9600));
    public static final RegistryObject<Potion> STRONG_HASTE = potion("strong_haste", new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1));
    public static final RegistryObject<Potion> MINING_FATIGUE = potion("mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3600));
    public static final RegistryObject<Potion> LONG_MINING_FATIGUE = potion("long_mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 9600));
    public static final RegistryObject<Potion> STRONG_MINING_FATIGUE = potion("strong_mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1800, 1));

    private MDPotions() {
    }

    private static RegistryObject<Potion> potion(String name, MobEffectInstance effect) {
        return POTIONS.register(name, () -> new Potion(name, effect));
    }

    /**
     * Upstream also lists long and strong haste + fermented spider eye giving the same potion back (a step that only
     * eats the eye); those two are left out.
     */
    public static void registerBrewing() {
        PotionBrewing.addMix(Potions.WATER, MDItems.COPPER_CARROT.get(), Potions.MUNDANE);
        PotionBrewing.addMix(Potions.AWKWARD, MDItems.COPPER_CARROT.get(), HASTE.get());
        PotionBrewing.addMix(HASTE.get(), Items.REDSTONE, LONG_HASTE.get());
        PotionBrewing.addMix(HASTE.get(), Items.GLOWSTONE_DUST, STRONG_HASTE.get());
        PotionBrewing.addMix(HASTE.get(), Items.FERMENTED_SPIDER_EYE, MINING_FATIGUE.get());
        PotionBrewing.addMix(MINING_FATIGUE.get(), Items.REDSTONE, LONG_MINING_FATIGUE.get());
        PotionBrewing.addMix(MINING_FATIGUE.get(), Items.GLOWSTONE_DUST, STRONG_MINING_FATIGUE.get());
    }
}
