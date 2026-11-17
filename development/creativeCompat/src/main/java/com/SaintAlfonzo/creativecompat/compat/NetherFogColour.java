package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * One fog color for the whole Nether, a deep red ({@link #RGB}), instead of each biome's own (perf.md 19.3, owner).
 * Set on Forge's fog color event, which comes after vanilla's biome sample, so the clear color, the terrain fog
 * and Distant Horizons' world-colored far fog all take it. Uses the level being drawn, so a Nether seen through an
 * Immersive Portals portal is red too. Left alone: in lava, water or powder snow, and with Blindness or Darkness
 * (vanilla darkens the color for those). Runs LOW, after other mods' fog color handlers.
 */
public final class NetherFogColour {

    /** #3F0905 */
    public static final int RGB = 0x3F0905;
    private static final float R = ((RGB >> 16) & 0xFF) / 255F, G = ((RGB >> 8) & 0xFF) / 255F, B = (RGB & 0xFF) / 255F;

    private NetherFogColour() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFogColour(ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.NETHER) return;
        if (event.getCamera().getFluidInCamera() != FogType.NONE) return;
        Entity entity = event.getCamera().getEntity();
        if (entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) return;
        event.setRed(R);
        event.setGreen(G);
        event.setBlue(B);
    }
}
