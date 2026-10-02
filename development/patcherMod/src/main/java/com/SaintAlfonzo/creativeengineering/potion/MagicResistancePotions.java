package com.SaintAlfonzo.creativeengineering.potion;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * kubejs/startup_scripts/cooking/potions.js can't build a "Potion of Magic Resistance" itself
 * for the same timing reason documented there and in {@link MagicResistanceEffect}: KubeJS
 * evaluates StartupEvents.registry('potion', ...) scripts in one early pass, well before its
 * OWN StartupEvents.registry('mob_effect', ...) RegisterEvent has fired, so
 * ForgeRegistries.MOB_EFFECTS.getValue("creativeengineering:magic_resistance") is always null
 * at that point - unlike registering the effect itself, which has no such problem (see
 * magic_resistance.js's header comment).
 *
 * A real DeferredRegister<Potion>'s supplier, by contrast, is only invoked by Forge at the
 * genuine RegisterEvent<Potion> - which fires after RegisterEvent<MobEffect> - so the lookup
 * below succeeds. Same trick as {@link BetterBeaconsPotions}, minus the cross-mod ModList
 * check since creativeengineering:magic_resistance is this mod's own effect and is always
 * present.
 *
 * potions.js's own MoreJSEvents.registerPotionBrewing adds the brewing-stand / mixer mixes
 * for these potions once they exist here, via CE_POTION_MIXES, the same way it already does
 * for long_reach / phantom_bane.
 */
public final class MagicResistancePotions {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation EFFECT_ID =
            new ResourceLocation(CreativeEngineering.MODID, "magic_resistance");

    private static final int S = 20; // ticks per second
    private static final int BASE_DURATION = 180 * S;
    private static final int LONG_DURATION = 480 * S;
    private static final int STRONG_DURATION = 90 * S;
    private static final int STRONG_AMPLIFIER = 1;

    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, CreativeEngineering.MODID);

    public static final RegistryObject<Potion> MAGIC_RESISTANCE =
            register("magic_resistance", BASE_DURATION, 0);
    public static final RegistryObject<Potion> LONG_MAGIC_RESISTANCE =
            register("long_magic_resistance", LONG_DURATION, 0);
    public static final RegistryObject<Potion> STRONG_MAGIC_RESISTANCE =
            register("strong_magic_resistance", STRONG_DURATION, STRONG_AMPLIFIER);

    private MagicResistancePotions() {}

    private static RegistryObject<Potion> register(String name, int duration, int amplifier) {
        return POTIONS.register(name, () -> {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(EFFECT_ID);
            if (effect == null) {
                LOGGER.error("[CE potions] creativeengineering:magic_resistance not found while building creativeengineering:{} - did magic_resistance.js fail to register it?", name);
                return new Potion(new MobEffectInstance[0]);
            }
            return new Potion(new MobEffectInstance(effect, duration, amplifier));
        });
    }

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }
}
