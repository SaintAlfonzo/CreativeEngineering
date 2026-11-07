package com.SaintAlfonzo.creativecommon.effect;

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
 *   <li>Farmer's Respite's Caffeinated adds a hidden Phantom Bane: Creative Delight's
 *       {@code compat.farmersrespite.CaffeinatedPhantomBane} and its CaffeinatedEffectMixin (moved 2026-11-07).</li>
 *   <li>When Phantom Bane ends (runs out or is removed) on a player, Time Since Rest goes back to 0, as after sleeping.</li>
 *   <li>Only the players with Phantom Bane are skipped by the phantom spawner, not everyone after them in the player list
 *       ({@code mixin/better_beacons/PhantomSpawnerBaneSkipMixin}, {@code potions/effects #13}).</li>
 * </ul>
 * The effect is looked up lazily: Better Beacons registers it.
 */
public final class PhantomBaneRules {

    private static final ResourceLocation PHANTOM_BANE_ID = new ResourceLocation("better_beacons", "phantom_bane");

    private static MobEffect phantomBane;
    private static boolean lookedUp;

    private PhantomBaneRules() {}

    private static void lookUp() {
        if (lookedUp) return;
        phantomBane = ForgeRegistries.MOB_EFFECTS.getValue(PHANTOM_BANE_ID);
        lookedUp = true;
    }

    /** True when the player has Better Beacons' Phantom Bane (the phantom spawner skips them, mixin.better_beacons). */
    public static boolean hasPhantomBane(net.minecraft.world.entity.player.Player player) {
        lookUp();
        return phantomBane != null && player.hasEffect(phantomBane);
    }

    public static boolean anyHasPhantomBane(java.util.List<? extends net.minecraft.world.entity.player.Player> players) {
        for (net.minecraft.world.entity.player.Player p : players) {
            if (hasPhantomBane(p)) return true;
        }
        return false;
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
