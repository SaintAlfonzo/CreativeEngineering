package com.SaintAlfonzo.creativedelight.mixin.solapplepie;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lunchbox contents are food only. Spice of Life's lunchbox screen slot (FoodSlot) already refuses an item that is not
 * edible, but the box's item handler (the anonymous handler in FoodContainerCapabilityProvider) only refuses another
 * lunchbox, so anything inserting through the capability (commands, other mods' inventory tools) could put a non-food
 * such as the pack's placeable pumpkin pie into a slot (scratch run r1115, SQ-FDI-04). The handler now uses the same
 * food check as the slot: {@link ItemStack#isEdible()}, not a tag (plan_post_scratch_1115.md D1, feature_writeup/lunchboxes.md).
 */
@Mixin(targets = "com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerCapabilityProvider$1", remap = false)
public abstract class LunchboxFoodOnlyMixin {

	@Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
	private void ce$foodOnly(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!stack.isEdible()) cir.setReturnValue(false);
	}
}
