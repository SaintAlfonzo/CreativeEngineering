package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Low gravity for the entities whose tick adds a fixed -0.03 pull: XP orbs and fishing bobbers (see LowGravityPullMixin). */
@Mixin(value = {ExperienceOrb.class, FishingHook.class}, remap = false)
public abstract class LowGravitySlowPullMixin {

	@ModifyConstant(method = "m_8119_", constant = @Constant(doubleValue = -0.03D), remap = false, require = 1)
	private double creativecommon$lowGravity(double pull) {
		return pull * LowGravity.pullScale((Entity) (Object) this);
	}
}
