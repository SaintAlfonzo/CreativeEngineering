package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.common.fluids.ConcreteFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * IE liquid concrete sets after 30 s instead of 32 (TODO jei/jei-coverage #27, the Concrete Hardening JEI tab).
 *
 * IE 9.2.4-170 {@code ConcreteFluid.tick} (m_6292_, javap): the fluid ticks every 20 ticks; each tick reads the
 * {@code INT_32} timer, sets the block once the timer is {@code >= 31}, else raises it by one (capped at 31). The
 * timer starts at 0, so the block set on the 32nd tick (32 s). Lowering the first {@code 31} (the "ready" test, not
 * the cap) to 29 sets it on the 30th tick. {@code remap = false}: mod class, no refmap in this project.
 */
@Mixin(value = ConcreteFluid.class, remap = false)
public class IEConcreteSetTimeMixin {

    @ModifyConstant(method = "m_6292_", remap = false, require = 0, constant = @Constant(intValue = 31, ordinal = 0))
    private int creativeengineering$setAfter30s(int ready) {
        return 29;
    }
}
