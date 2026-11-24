// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.data.DietAdjustment;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Carries the Adjustment entry's value ({@link DietAdjustment}) and lets Apple Pie decode the Adjustment item: its
 * {@code decode} drops any item that is not edible, and the Adjustment item is not.
 */
@Mixin(value = FoodInstance.class, remap = false)
public abstract class FoodInstanceMixin implements DietAdjustment {
	@Unique
	private Double solrp$value;

	@Override
	public Double solrp$value() {
		return solrp$value;
	}

	@Override
	public void solrp$setValue(Double value) {
		solrp$value = value;
	}

	@Inject(method = "decode", at = @At("HEAD"), cancellable = true)
	private static void solrp$decodeAdjustment(String encoded, CallbackInfoReturnable<FoodInstance> cir) {
		if (SolRaspberryPie.ADJUSTMENT_ID.equals(ResourceLocation.tryParse(encoded))) {
			cir.setReturnValue(new FoodInstance(SolRaspberryPie.ADJUSTMENT.get()));
		}
	}
}
