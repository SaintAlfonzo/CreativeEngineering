package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.swap.SwapReport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stack half of {@link SwapReport}: {@code ItemStack.of} (m_41712_) returning empty for an id a swap list names. */
@Mixin(ItemStack.class)
public abstract class SwapReportItemStackMixin {

	@Inject(method = "m_41712_(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), remap = false)
	private static void creativecommon$reportUnresolved(CompoundTag tag, CallbackInfoReturnable<ItemStack> cir) {
		if (cir.getReturnValue().isEmpty()) SwapReport.noteEmptyStack(tag);
	}
}
