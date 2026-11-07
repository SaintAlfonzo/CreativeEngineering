package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.CamoSpeed;
import com.SaintAlfonzo.creativecompat.surface.StickyFacing;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes BlockRunner's per-block speeds blockstate / block-entity aware. Every time BlockRunner
 * (re)applies its speed modifier this runs {@link StickyFacing} (sticky pistons only count face
 * up) and {@link CamoSpeed} (framed / copycat blocks walk like their camo).
 *
 * <p>The target, {@code blockrunner$tryAddBlockSpeed}, is not vanilla: BlockRunner's own
 * {@code fuzs.blockrunner.mixin.LivingEntityMixin} merges it into {@code LivingEntity} (it is
 * {@code @Unique} but protected, so Mixin keeps the name). Mixin only lets an injector target a
 * method merged by another mixin of LOWER priority, hence {@code priority = 1500} (BlockRunner
 * uses the default 1000). RETURN covers every exit, including "speed factor is 1, add nothing".
 * {@code require = 0}: if BlockRunner is removed or renames the method this silently does nothing
 * rather than crashing the game on boot.
 */
@Mixin(value = LivingEntity.class, priority = 1500)
public abstract class BlockRunnerBlockStateMixin {

    @Inject(method = "blockrunner$tryAddBlockSpeed", at = @At("RETURN"), remap = false, require = 0)
    private void creativeengineering$blockStateSpeed(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        try {
            StickyFacing.afterBlockRunner(self);
        } catch (Throwable ignored) {
            // leave BlockRunner's modifier as it is
        }
        try {
            CamoSpeed.afterBlockRunner(self);
        } catch (Throwable ignored) {
            // leave BlockRunner's modifier as it is
        }
    }
}
