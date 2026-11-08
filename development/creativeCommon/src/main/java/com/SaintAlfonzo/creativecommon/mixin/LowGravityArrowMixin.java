package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Low gravity for arrows, spectral arrows, tridents and modded AbstractArrow subclasses: the (double) 0.05F their tick
 * subtracts after the move (see LowGravityPullMixin). The same value in onHitBlock (the bounce) is left alone.
 */
@Mixin(value = AbstractArrow.class, remap = false)
public abstract class LowGravityArrowMixin {

	@ModifyConstant(method = "m_8119_", constant = @Constant(doubleValue = (double) 0.05F), remap = false, require = 1)
	private double creativecommon$lowGravity(double pull) {
		return pull * LowGravity.pullScale((Entity) (Object) this);
	}
}
