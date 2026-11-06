package com.SaintAlfonzo.creativeworldgen.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GroundPathNavigation.canUpdatePath (m_7632_) is false off the ground. A climbing villager then gets no new path, and
 * PathNavigation.tick only advances a path node when the mob is above it in the same column, so on the way down it
 * skipped every rung at once and walked off toward the exit while still high up. A villager on a climbable block counts
 * as able to update its path: nodes advance one rung at a time (within 1 block) and the brain can re-path mid-ladder.
 * Part of the villager ladder change, {@code McaVillagerPathLaddersMixin}.
 */
@Mixin(value = GroundPathNavigation.class, remap = false)
public abstract class GroundPathNavigationClimbMixin extends PathNavigation {

    private GroundPathNavigationClimbMixin(Mob mob, Level level) {
        super(mob, level);
    }

    @Inject(method = "m_7632_", at = @At("RETURN"), cancellable = true, remap = false)
    private void creativeengineering$updateOnLadder(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.mob instanceof Villager && this.mob.onClimbable()) cir.setReturnValue(true);
    }
}
