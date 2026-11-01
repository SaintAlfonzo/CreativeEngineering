package com.SaintAlfonzo.creativedelight.mixin.immersiveengineering;

import com.SaintAlfonzo.creativedelight.treatedstick.TreatedSticks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placeable treated sticks (feature_writeup/treated_sticks.md): Item#useOn of Immersive Engineering's treated stick
 * places {@code creative_delight:treated_stick}. useOn runs only after the clicked block's own use passed, the same
 * order as Supplementaries' placeable sticks. IE's item class (IEBaseItem) has no useOn of its own.
 */
@Mixin(Item.class)
public abstract class TreatedStickPlaceMixin {

	@Inject(method = "m_6225_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeDelight$placeTreatedStick(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
		if ((Object) this != TreatedSticks.item() || !TreatedSticks.isTreatedStick(ctx.getItemInHand())) return;
		InteractionResult result = TreatedSticks.useOn(ctx);
		if (result != InteractionResult.PASS) cir.setReturnValue(result);
	}
}
