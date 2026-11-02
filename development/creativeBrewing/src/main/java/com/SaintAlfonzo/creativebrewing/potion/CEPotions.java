package com.SaintAlfonzo.creativebrewing.potion;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.List;

/**
 * The potions patcherMod registers for effects KubeJS cannot build potions for, one table for all of them.
 *
 * Why Java: kubejs/startup_scripts/cooking/potions.js can't build these itself. KubeJS evaluates
 * StartupEvents.registry('potion', ...) scripts in one early pass (to gather everything before real Forge
 * registration begins), well before any RegisterEvent&lt;MobEffect&gt; has fired, including KubeJS's own
 * StartupEvents.registry('mob_effect', ...) (creativeengineering:magic_resistance, {@link MagicResistanceEffect}) and
 * better_beacons' DeferredRegister. So ForgeRegistries.MOB_EFFECTS.getValue(...) is always null at that point and the
 * potion silently never gets created. A DeferredRegister&lt;Potion&gt;'s supplier, by contrast, is only invoked at the
 * genuine RegisterEvent&lt;Potion&gt;, which fires after RegisterEvent&lt;MobEffect&gt; completes for every mod, so the
 * lookup below succeeds.
 *
 * This also matters for phantom_bane: better_beacons' PhantomSpawnerMixin checks
 * player.hasEffect(BBEffects.PHANTOM_BANE.get()) by exact object identity to suppress phantom spawns, so the potion
 * must carry the mod's real registered MobEffect object, not a lookalike.
 *
 * potions.js's MoreJSEvents.registerPotionBrewing adds the brewing-stand / mixer mixes once these exist
 * (CE_POTION_MIXES), the same way it does for Alex's Mobs' own potions (AM_POTION_MIXES).
 *
 * Each effect gets the base / long / strong trio (a levelless one only base / long); a row with a {@code needsMod} is skipped when that mod is absent.
 */
public final class CEPotions {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int S = 20; // ticks per second
    private static final int BASE_DURATION = 180 * S;
    private static final int LONG_DURATION = 480 * S;
    private static final int STRONG_DURATION = 90 * S;
    private static final int STRONG_AMPLIFIER = 1;

    /**
     * One effect: registers {@code <name>}, {@code long_<name>} and, for an effect with levels, {@code strong_<name>}
     * in creativeengineering.
     *
     * @param needsMod mod that must be loaded for the potions to be registered, or null
     * @param missing  appended to the error when the effect is not found at potion registration
     * @param levels   false for a levelless effect (no strong potion, like Night Vision)
     */
    private record Row(String name, ResourceLocation effect, String needsMod, String missing, boolean levels) {
    }

    /** Registration order kept from the two former registrars (Better Beacons first). */
    private static final List<Row> ROWS = List.of(
            new Row("long_reach", new ResourceLocation("better_beacons", "long_reach"), "better_beacons",
                    "is Cerbon's Better Beacons installed?", true),
            // levelless: Better Beacons only tests hasEffect (armor_effects.md F7)
            new Row("phantom_bane", new ResourceLocation("better_beacons", "phantom_bane"), "better_beacons",
                    "is Cerbon's Better Beacons installed?", false),
            new Row("magic_resistance", new ResourceLocation(CreativeBrewing.NS, "magic_resistance"), null,
                    "did magic_resistance.js fail to register it?", true));

    private static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, CreativeBrewing.NS);

    private CEPotions() {}

    public static void register(IEventBus modEventBus) {
        for (Row row : ROWS) {
            if (row.needsMod() != null && !ModList.get().isLoaded(row.needsMod())) {
                LOGGER.info("[CE potions] {} not present - skipping the {} potions", row.needsMod(), row.name());
                continue;
            }
            add(row, row.name(), BASE_DURATION, 0);
            add(row, "long_" + row.name(), LONG_DURATION, 0);
            if (row.levels()) add(row, "strong_" + row.name(), STRONG_DURATION, STRONG_AMPLIFIER);
        }
        POTIONS.register(modEventBus);
    }

    private static void add(Row row, String name, int duration, int amplifier) {
        POTIONS.register(name, () -> {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(row.effect());
            if (effect == null) {
                LOGGER.error("[CE potions] {} not found while building creativeengineering:{} - {}", row.effect(), name, row.missing());
                return new Potion(new MobEffectInstance[0]);
            }
            return new Potion(new MobEffectInstance(effect, duration, amplifier));
        });
    }
}
