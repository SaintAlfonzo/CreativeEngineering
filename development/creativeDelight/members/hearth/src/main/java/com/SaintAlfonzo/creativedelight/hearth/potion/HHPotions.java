package com.SaintAlfonzo.creativedelight.hearth.potion;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.effect.HHEffects;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Pungent, Tempting and Bad Omen potions (D22, plan P5). The brewing mixes are in Creative Brewing's potions.js so every
 * machine reads them; no long Bad Omen (3.1: raid on demand). Times are 3:00 base, 8:00 long, 1:30 at II.
 */
public final class HHPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, HearthAndHarvestMod.MODID);
    public static final RegistryObject<Potion> PUNGENT = potion("pungent", HHEffects.PUNGENT::get, 3600, 0);
    public static final RegistryObject<Potion> LONG_PUNGENT = potion("long_pungent", HHEffects.PUNGENT::get, 9600, 0);
    public static final RegistryObject<Potion> STRONG_PUNGENT = potion("strong_pungent", HHEffects.PUNGENT::get, 1800, 1);
    public static final RegistryObject<Potion> TEMPTING = potion("tempting", HHEffects.TEMPTING::get, 3600, 0);
    public static final RegistryObject<Potion> LONG_TEMPTING = potion("long_tempting", HHEffects.TEMPTING::get, 9600, 0);
    public static final RegistryObject<Potion> STRONG_TEMPTING = potion("strong_tempting", HHEffects.TEMPTING::get, 1800, 1);
    public static final RegistryObject<Potion> BAD_OMEN = potion("bad_omen", () -> MobEffects.BAD_OMEN, 3600, 0);
    public static final RegistryObject<Potion> STRONG_BAD_OMEN = potion("strong_bad_omen", () -> MobEffects.BAD_OMEN, 1800, 1);

    private HHPotions() {
    }

    /** Long and strong share the base potion's display name, as vanilla's do. */
    private static String baseName(String id) {
        return id.replace("long_", "").replace("strong_", "");
    }

    private static RegistryObject<Potion> potion(String id, Supplier<MobEffect> effect, int ticks, int amplifier) {
        return POTIONS.register(id, () -> new Potion(baseName(id), new MobEffectInstance(effect.get(), ticks, amplifier)));
    }
}
