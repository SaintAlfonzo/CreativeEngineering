package com.SaintAlfonzo.creativeworldgen.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MoveControl.tick (m_8126_) jumps whenever the block at the mob's feet has a collision shape above its feet, which a
 * ladder always has, and a jumping mob on a climbable block climbs. So a mob in a ladder can only go up. When a villager
 * on a climbable block is moving to a point more than half a block below it and less than half a block away sideways,
 * it neither jumps nor walks (walking into the wall behind the ladder also climbs): it slides down the ladder at
 * vanilla's 0.15 blocks a tick. Part of the villager ladder change, {@code McaVillagerPathLaddersMixin}.
 */
@Mixin(value = MoveControl.class, remap = false)
public abstract class MoveControlVillagerLadderMixin {

    @Shadow(remap = false)
    @Final
    protected Mob f_24974_;

    @Inject(method = "m_8126_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$slideDownLadder(CallbackInfo ci) {
        Mob mob = this.f_24974_;
        if (!(mob instanceof Villager)) return;
        MoveControl self = (MoveControl) (Object) this;
        if (!self.hasWanted() || self.getWantedY() > mob.getY() - 0.5 || !mob.onClimbable()) return;
        double dx = self.getWantedX() - mob.getX();
        double dz = self.getWantedZ() - mob.getZ();
        if (dx * dx + dz * dz >= 0.25) return;
        mob.setZza(0.0F);
        mob.setSpeed(0.0F);
        ci.cancel();
    }
}
