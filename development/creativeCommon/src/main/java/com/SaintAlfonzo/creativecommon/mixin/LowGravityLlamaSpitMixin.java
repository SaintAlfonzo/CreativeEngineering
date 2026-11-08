package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.LlamaSpit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Low gravity for llama spit: the (double) -0.06F its tick adds (see LowGravityPullMixin). */
@Mixin(value = LlamaSpit.class, remap = false)
public abstract class LowGravityLlamaSpitMixin {

	@ModifyConstant(method = "m_8119_", constant = @Constant(doubleValue = (double) -0.06F), remap = false, require = 1)
	private double creativecommon$lowGravity(double pull) {
		return pull * LowGravity.pullScale((Entity) (Object) this);
	}
}
