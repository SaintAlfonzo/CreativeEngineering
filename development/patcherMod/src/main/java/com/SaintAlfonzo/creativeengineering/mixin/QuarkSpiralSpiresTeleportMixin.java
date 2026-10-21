package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.content.world.module.SpiralSpiresModule;

/**
 * Quark's Myalite viaduct hook runs on every teleport (/tp, ender pearls, chorus fruit) and reads the
 * blocks at the destination, which forces a synchronous chunk generation on the server thread when the
 * target is unexplored (a /tp into new terrain hit the 60 s watchdog). Spiral Spires and their Myalite
 * only generate in the End, so the hook is skipped in every other dimension.
 */
@Mixin(value = SpiralSpiresModule.class, remap = false)
public abstract class QuarkSpiralSpiresTeleportMixin {

    @Inject(method = "onTeleport", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$endOnly(EntityTeleportEvent event, CallbackInfo ci) {
        Entity entity = event.getEntity();
        if (entity == null || entity.level.dimension() != Level.END) ci.cancel();
    }
}
