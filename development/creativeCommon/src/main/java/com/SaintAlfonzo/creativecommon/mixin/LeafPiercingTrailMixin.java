package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.SaintAlfonzo.creativecommon.effect.LeafPiercingTrail;

import net.minecraft.world.entity.projectile.AbstractArrow;

/** Leaf Piercing particles ({@code food-tuning #52}, {@link LeafPiercingTrail}) at the end of every arrow tick. */
@Mixin(value = AbstractArrow.class, remap = false)
public abstract class LeafPiercingTrailMixin {

	@Unique
	private LeafPiercingTrail.State creativecommon$trail;

	@Inject(method = "m_8119_", at = @At("TAIL"), remap = false)
	private void creativecommon$leafTrail(CallbackInfo ci) {
		if (creativecommon$trail == null) creativecommon$trail = new LeafPiercingTrail.State();
		LeafPiercingTrail.tick((AbstractArrow) (Object) this, creativecommon$trail);
	}
}
