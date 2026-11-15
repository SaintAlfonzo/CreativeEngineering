package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassFov;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * {@code AbstractClientPlayer#getFieldOfViewModifier} ({@code m_108565_}): the float {@code f} right after vanilla's
 * {@code f *= (speed / walkSpeed + 1) / 2} (its third store, the same point BlockRunner's own client mixin uses) is
 * recomputed without block walking speeds ({@link PassFov}; plan_passthrough.md 1.3). Client only.
 */
@Mixin(value = AbstractClientPlayer.class, remap = false)
public abstract class PassFovMixin {

    @ModifyVariable(method = "m_108565_", at = @At(value = "STORE", ordinal = 2), ordinal = 0)
    private float creativecompat$noBlockSpeedFov(float f) {
        try {
            return PassFov.adjust((Player) (Object) this, f);
        } catch (Throwable t) {
            return f;
        }
    }
}
