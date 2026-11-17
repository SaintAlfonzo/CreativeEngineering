package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassProjectiles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.content.tools.entity.TorchArrow;
import vazkii.quark.content.tools.module.TorchArrowModule;

/**
 * A Quark torch arrow hanging in a web comes back as a torch arrow (plan_passthrough.md 5.3; phase 0 brief 5.4): with
 * Quark's {@code Extinguish On Miss} on, {@code getPickupItem} ({@code m_7941_}) returns a plain arrow, but a web hang
 * is no miss. The pack runs the option off, so this only matters if it is turned on. Applies only with Quark
 * (CompatMixinPlugin).
 */
@Mixin(value = TorchArrow.class, remap = false)
public abstract class QuarkTorchArrowPickupMixin {

	@Inject(method = "m_7941_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$webTorchArrow(CallbackInfoReturnable<ItemStack> cir) {
		try {
			if (TorchArrowModule.extinguishOnMiss && TorchArrowModule.torch_arrow != null
					&& PassProjectiles.webHung((Entity) (Object) this)) {
				cir.setReturnValue(new ItemStack(TorchArrowModule.torch_arrow));
			}
		} catch (Throwable ignored) {
			// keep Quark's answer
		}
	}
}
