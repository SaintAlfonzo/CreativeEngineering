package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.swap.SwapLists;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Saved capability data under a renamed id (the swap lists' {@code capabilities}) loads under its new key. On the final
 * dispatcher, not on CapabilityProvider: that is a superclass of Level, Entity and ItemStack, and a mixin there changes how
 * Mixin binds other mods' super calls in those classes (IncompatibleClassChangeError in Level, found 2026-11-10).
 */
@Mixin(value = CapabilityDispatcher.class, remap = false)
public abstract class SwapCapabilityKeysMixin {

	@Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
	private void creativecommon$renameSwappedKeys(CompoundTag tag, CallbackInfo ci) {
		SwapLists.get().renameCapabilityKeys(tag);
	}
}
