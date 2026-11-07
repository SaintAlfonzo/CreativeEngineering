package com.SaintAlfonzo.creativecompat.shrapnel;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Fires a {@link ShrapnelSpawner} burst whenever a primed TNT entity (block TNT that's been lit,
 * or a TNT minecart) actually detonates. {@code ExplosionEvent.Detonate} runs after the
 * explosion's block/entity damage has already been calculated, so this only adds the shrapnel
 * on top - it never changes what the explosion itself destroys.
 *
 * <p>Gated on the direct source entity being TNT specifically (not creepers, beds, respawn
 * anchors, etc.) - {@link Explosion#getExploder()} is the entity that actually caused the blast,
 * as opposed to {@link Explosion#getSourceMob()} which is whoever/whatever is "responsible" for
 * it (e.g. the player who lit the fuse).
 */
public final class TntShrapnelHandler {

    private TntShrapnelHandler() {}

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        Explosion explosion = event.getExplosion();
        Entity direct = explosion.getExploder();
        if (!(direct instanceof PrimedTnt) && !(direct instanceof MinecartTNT)) return;

        ShrapnelSpawner.burst(level, explosion.getPosition(), explosion.getSourceMob());
    }
}
