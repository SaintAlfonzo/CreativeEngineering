package com.SaintAlfonzo.creativeengineering.potion;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * kubejs/startup_scripts/cooking/potions.js can't build long_reach / phantom_bane potions
 * itself: KubeJS evaluates StartupEvents.registry('potion', ...) scripts in one early pass
 * (to gather everything before real Forge registration begins), which runs well before
 * better_beacons' own DeferredRegister<MobEffect> RegisterEvent has fired - so
 * Registry.MOB_EFFECT / ForgeRegistries.MOB_EFFECTS.getValue("better_beacons:...") is
 * always null at that point, and the potion silently never gets created.
 *
 * A real DeferredRegister<Potion>'s supplier, by contrast, is only invoked by Forge at the
 * genuine RegisterEvent<Potion> - which fires after RegisterEvent<MobEffect> completes for
 * every mod, including better_beacons - so the lookup below succeeds. This also matters
 * for phantom_bane specifically: better_beacons' PhantomSpawnerMixin checks
 * player.hasEffect(BBEffects.PHANTOM_BANE.get()) by exact object identity to suppress
 * phantom spawns, so the potion must carry the mod's real registered MobEffect object, not
 * a lookalike.
 *
 * potions.js's own MoreJSEvents.registerPotionBrewing adds the brewing-stand / mixer mixes
 * for these potions once they exist here, the same way it already does for Alex's Mobs'
 * own real potions (AM_POTION_MIXES).
 */
public final class BetterBeaconsPotions {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String BB_MODID = "better_beacons";

    private static final int S = 20; // ticks per second
    private static final int BASE_DURATION = 180 * S;
    private static final int LONG_DURATION = 480 * S;
    private static final int STRONG_DURATION = 90 * S;
    private static final int STRONG_AMPLIFIER = 1;

    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, CreativeEngineering.MODID);

    public static final RegistryObject<Potion> LONG_REACH =
            register("long_reach", "long_reach", BASE_DURATION, 0);
    public static final RegistryObject<Potion> LONG_LONG_REACH =
            register("long_long_reach", "long_reach", LONG_DURATION, 0);
    public static final RegistryObject<Potion> STRONG_LONG_REACH =
            register("strong_long_reach", "long_reach", STRONG_DURATION, STRONG_AMPLIFIER);

    public static final RegistryObject<Potion> PHANTOM_BANE =
            register("phantom_bane", "phantom_bane", BASE_DURATION, 0);
    public static final RegistryObject<Potion> LONG_PHANTOM_BANE =
            register("long_phantom_bane", "phantom_bane", LONG_DURATION, 0);
    public static final RegistryObject<Potion> STRONG_PHANTOM_BANE =
            register("strong_phantom_bane", "phantom_bane", STRONG_DURATION, STRONG_AMPLIFIER);

    private BetterBeaconsPotions() {}

    private static RegistryObject<Potion> register(String name, String bbEffectName, int duration, int amplifier) {
        return POTIONS.register(name, () -> {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(BB_MODID, bbEffectName));
            if (effect == null) {
                LOGGER.error("[CE potions] better_beacons:{} not found while building creativeengineering:{} - is Cerbon's Better Beacons installed?", bbEffectName, name);
                return new Potion(new MobEffectInstance[0]);
            }
            return new Potion(new MobEffectInstance(effect, duration, amplifier));
        });
    }

    public static void register(IEventBus modEventBus) {
        if (!ModList.get().isLoaded(BB_MODID)) {
            LOGGER.info("[CE potions] better_beacons not present - skipping long_reach/phantom_bane potions");
            return;
        }
        POTIONS.register(modEventBus);
    }
}
