package com.SaintAlfonzo.creativecompat.campfire;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Comfortable Campfires port (feature_writeup/feature_porting.md 2.1, jar retired 2026-10-20): players near a lit
 * campfire get Farmer's Delight's Comfort (Regeneration when the effect is missing or {@code useComfort} is off) for
 * {@code durationSeconds}, refreshed while under 3 s left. The mod did this every tick for every lit campfire; here a
 * campfire checks once per {@code comfortEveryTicks} (20), spread over campfires by position. Driven from
 * {@code mixin/CampfireRainMixin} like {@link CampfireRain}; vanilla only ticks a campfire's cook tick while lit.
 */
public final class CampfireComfort {
    private static final ResourceLocation COMFORT = new ResourceLocation("farmersdelight", "comfort");
    private static final int REFRESH_BELOW_TICKS = 60;
    private static MobEffect comfort;
    private static boolean looked;

    private CampfireComfort() {}

    public static void tick(Level level, BlockPos pos) {
        CompatConfig.Campfire cfg = CompatConfig.CAMPFIRE;
        if (!cfg.comfort.get()) return;
        int every = cfg.comfortEveryTicks.get();
        if (every > 1 && Math.floorMod(level.getGameTime() + pos.asLong(), every) != 0) return;
        int distance = cfg.comfortDistance.get();
        AABB box = new AABB(pos).inflate(distance).expandTowards(0.0, distance, 0.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, box);
        if (players.isEmpty()) return;
        MobEffect effect = effect();
        int duration = cfg.comfortSeconds.get() * 20;
        for (Player player : players) {
            MobEffectInstance current = player.getEffect(effect);
            if (current == null || current.getDuration() < REFRESH_BELOW_TICKS) {
                player.addEffect(new MobEffectInstance(effect, duration, 0, true, true));
            }
        }
    }

    private static MobEffect effect() {
        if (!CompatConfig.CAMPFIRE.useComfort.get()) return MobEffects.REGENERATION;
        if (!looked) {
            comfort = ForgeRegistries.MOB_EFFECTS.getValue(COMFORT);
            looked = true;
        }
        return comfort != null ? comfort : MobEffects.REGENERATION;
    }
}
