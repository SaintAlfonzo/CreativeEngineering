package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.crops.VineCropReplace;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link VineCropReplace}: a vine crop in {@code #creativedelight:crops/vine_crops} is not replaced by placing another
 * block into it. BlockStateBase.canBeReplaced(BlockPlaceContext) is m_60629_; other blocks pay one tag check, and only
 * while something is being placed.
 */
@Mixin(value = BlockBehaviour.BlockStateBase.class, remap = false)
public abstract class VineCropNoReplaceMixin {

	@Inject(method = "m_60629_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$vineCropNoReplace(BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
		if (VineCropReplace.refuses((BlockBehaviour.BlockStateBase) (Object) this, context)) {
			cir.setReturnValue(false);
		}
	}
}
