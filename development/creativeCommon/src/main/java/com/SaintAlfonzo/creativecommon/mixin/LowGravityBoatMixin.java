package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Low gravity for boats out of water: the (double) -0.04F pull of floatBoat (two copies in the bytecode, one an unused
 * local). In water that pull balances the boat's buoyancy, so a boat touching water keeps vanilla's value (a lower pull
 * would lift it out of the water and make it bob). Runs on the client too: the client moves a boat its player steers.
 */
@Mixin(value = Boat.class, remap = false)
public abstract class LowGravityBoatMixin {

	@ModifyConstant(method = "m_38395_", constant = @Constant(doubleValue = (double) -0.04F), remap = false, require = 1)
	private double creativecommon$lowGravity(double pull) {
		Entity self = (Entity) (Object) this;
		if (self.isInWater() || self.isUnderWater()) return pull;
		return pull * LowGravity.pullScale(self);
	}
}
