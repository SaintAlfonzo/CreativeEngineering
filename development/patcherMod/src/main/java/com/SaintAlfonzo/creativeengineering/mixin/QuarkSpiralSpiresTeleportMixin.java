package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.core.BlockPos;
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
 * only generate in the End, so the hook is skipped in every other dimension. In the End it is also
 * skipped when the chunks around the target aren't loaded (a /tp there stalled the server 38 s); pearls
 * and chorus fruit land in loaded chunks, so the viaducts still work.
 */
@Mixin(value = SpiralSpiresModule.class, remap = false)
public abstract class QuarkSpiralSpiresTeleportMixin {

    @Inject(method = "onTeleport", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$endOnly(EntityTeleportEvent event, CallbackInfo ci) {
        Entity entity = event.getEntity();
        if (entity == null || entity.level.dimension() != Level.END) {
            ci.cancel();
            return;
        }
        // Quark reads the target, the block below and their neighbours
        BlockPos target = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        if (!entity.level.hasChunksAt(target.offset(-1, 0, -1), target.offset(1, 0, 1))) ci.cancel();
    }
}
