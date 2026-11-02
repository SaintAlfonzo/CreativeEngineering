package com.SaintAlfonzo.creativebrewing.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Phantom prevention, one rule for the pack ({@code potions/effects #10}, decided 2026-10-31):
 * <ul>
 *   <li>Farmer's Respite's Caffeinated is a compound with Phantom Bane as a part: adding Caffeinated adds a hidden Phantom
 *       Bane for the same time (Better Beacons' spawner check reads it). Caffeinated's own tick, which set the player's
 *       Time Since Rest to -24000 every tick and kept phantoms away for days after it ended, is cancelled by
 *       {@code mixin/CaffeinatedEffectMixin}.</li>
 *   <li>When Phantom Bane ends (runs out or is removed) on a player, Time Since Rest goes back to 0, as after sleeping.</li>
 * </ul>
 * Effects are looked up lazily: both are registered by other mods.
 */
public final class PhantomBaneRules {

    private static final ResourceLocation CAFFEINATED_ID = new ResourceLocation("farmersrespite", "caffeinated");
    private static final ResourceLocation PHANTOM_BANE_ID = new ResourceLocation("better_beacons", "phantom_bane");

    private static MobEffect caffeinated;
    private static MobEffect phantomBane;
    private static boolean lookedUp;

    private PhantomBaneRules() {}

    private static void lookUp() {
        if (lookedUp) return;
        caffeinated = ForgeRegistries.MOB_EFFECTS.getValue(CAFFEINATED_ID);
        phantomBane = ForgeRegistries.MOB_EFFECTS.getValue(PHANTOM_BANE_ID);
        lookedUp = true;
    }

    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        lookUp();
        MobEffectInstance added = event.getEffectInstance();
        LivingEntity entity = event.getEntity();
        if (caffeinated == null || phantomBane == null || entity.level.isClientSide || added.getEffect() != caffeinated) return;
        entity.addEffect(new MobEffectInstance(phantomBane, added.getDuration(), 0, added.isAmbient(), false, false));
    }

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null) resetRest(event.getEntity(), instance.getEffect());
    }

    @SubscribeEvent
    public static void onRemove(MobEffectEvent.Remove event) {
        resetRest(event.getEntity(), event.getEffect());
    }

    private static void resetRest(LivingEntity entity, MobEffect effect) {
        lookUp();
        if (phantomBane == null || effect != phantomBane || !(entity instanceof ServerPlayer player)) return;
        player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
    }
}
