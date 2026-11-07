package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Farmer's Respite's Caffeinated is a compound with Phantom Bane as a part ({@code potions/effects #10}, decided
 * 2026-10-31): adding Caffeinated adds a hidden Better Beacons Phantom Bane for the same time, so the phantom spawner
 * skips the player (Common's {@code effect.PhantomBaneRules} and {@code PhantomSpawnerBaneSkipMixin}). Caffeinated's own
 * tick, which pushed Time Since Rest to -24000 every tick, is cancelled by {@code mixin.farmersrespite.CaffeinatedEffectMixin}.
 * Moved from Creative Common's PhantomBaneRules 2026-11-07. Registered only with Farmer's Respite loaded; with Better
 * Beacons absent it does nothing (both effects are looked up lazily by id).
 */
public final class CaffeinatedPhantomBane {

    private static final ResourceLocation CAFFEINATED_ID = new ResourceLocation("farmersrespite", "caffeinated");
    private static final ResourceLocation PHANTOM_BANE_ID = new ResourceLocation("better_beacons", "phantom_bane");

    private static MobEffect caffeinated;
    private static MobEffect phantomBane;
    private static boolean lookedUp;

    private CaffeinatedPhantomBane() {}

    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        if (!lookedUp) {
            caffeinated = ForgeRegistries.MOB_EFFECTS.getValue(CAFFEINATED_ID);
            phantomBane = ForgeRegistries.MOB_EFFECTS.getValue(PHANTOM_BANE_ID);
            lookedUp = true;
        }
        MobEffectInstance added = event.getEffectInstance();
        LivingEntity entity = event.getEntity();
        if (caffeinated == null || phantomBane == null || entity.level.isClientSide || added.getEffect() != caffeinated) return;
        entity.addEffect(new MobEffectInstance(phantomBane, added.getDuration(), 0, added.isAmbient(), false, false));
    }
}
