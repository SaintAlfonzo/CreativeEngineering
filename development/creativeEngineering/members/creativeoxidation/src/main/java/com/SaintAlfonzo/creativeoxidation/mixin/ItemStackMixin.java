// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation.mixin;

import com.SaintAlfonzo.creativeoxidation.OxidationItemData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Staged item names (R3): "Waxed Weathered Fluid Pipe" is computed from the stage keys on every read, nothing is
 * stored. A custom name (anvil rename) is returned as it is.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
	private void creativeoxidation$stagedName(CallbackInfoReturnable<Component> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		if (stack.getTag() == null || stack.hasCustomHoverName()) return;
		Component named = OxidationItemData.displayName(stack, cir.getReturnValue());
		if (named != cir.getReturnValue()) cir.setReturnValue(named);
	}
}
