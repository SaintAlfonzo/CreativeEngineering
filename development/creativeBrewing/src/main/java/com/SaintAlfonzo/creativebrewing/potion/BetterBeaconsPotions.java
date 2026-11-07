package com.SaintAlfonzo.creativebrewing.potion;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * Potions for Better Beacons' effects: creativebrewing:long_reach (base, long_, strong_) and creativebrewing:phantom_bane
 * (base, long_; levelless, Better Beacons only tests hasEffect). Moved from Creative Common's CommonPotions 2026-11-07
 * (owner: Better Beacons potions belong to Creative Brewing; the creativecommon: ids are remapped by
 * data/creativebrewing/swaps/better_beacons_potions.json). Brew mixes: kubejs_startup/creativebrewing/potions.js.
 *
 * Why Java: KubeJS evaluates potion registry scripts before any MobEffect is registered, so a potion of a modded
 * effect built there silently never exists. A DeferredRegister supplier runs at the real RegisterEvent&lt;Potion&gt;,
 * after every MobEffect. Better Beacons' spawner check compares its own registered MobEffect by identity, so the potion
 * must carry the real object. Skipped entirely without Better Beacons.
 */
public final class BetterBeaconsPotions {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int S = 20;
    private static final int BASE_DURATION = 180 * S;
    private static final int LONG_DURATION = 480 * S;
    private static final int STRONG_DURATION = 90 * S;
    private static final int STRONG_AMPLIFIER = 1;

    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, CreativeBrewing.MOD_ID);

    private BetterBeaconsPotions() {}

    public static void register(IEventBus modEventBus) {
        if (!ModList.get().isLoaded("better_beacons")) {
            LOGGER.info("[CB potions] better_beacons not present - skipping the Long Reach and Phantom Bane potions");
            return;
        }
        trio("long_reach", true);
        trio("phantom_bane", false);
        POTIONS.register(modEventBus);
    }

    private static void trio(String name, boolean levels) {
        ResourceLocation effect = new ResourceLocation("better_beacons", name);
        add(effect, name, BASE_DURATION, 0);
        add(effect, "long_" + name, LONG_DURATION, 0);
        if (levels) add(effect, "strong_" + name, STRONG_DURATION, STRONG_AMPLIFIER);
    }

    private static void add(ResourceLocation effectId, String name, int duration, int amplifier) {
        POTIONS.register(name, () -> {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
            if (effect == null) {
                LOGGER.error("[CB potions] {} not found while building creativebrewing:{} - is Cerbon's Better Beacons installed?", effectId, name);
                return new Potion(new MobEffectInstance[0]);
            }
            return new Potion(new MobEffectInstance(effect, duration, amplifier));
        });
    }
}
