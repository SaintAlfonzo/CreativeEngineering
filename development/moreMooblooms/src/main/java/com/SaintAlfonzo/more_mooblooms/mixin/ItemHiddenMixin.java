package com.SaintAlfonzo.more_mooblooms.mixin;

import com.SaintAlfonzo.more_mooblooms.compat.BuzzierBeesSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps hidden items (the buttercup, Buzzier Bees' moobloom egg) out of every creative tab and the creative search,
 * by answering false from {@code Item.allowedIn} (m_220152_). Vanilla's and Blueprint's fillItemCategory both
 * check it. Only applied when Buzzier Bees is installed (MoreMoobloomsMixinPlugin).
 */
@Mixin(value = Item.class, remap = false)
public abstract class ItemHiddenMixin {

    @Inject(method = "m_220152_", at = @At("HEAD"), cancellable = true, remap = false)
    private void more_mooblooms$hidden(CreativeModeTab tab, CallbackInfoReturnable<Boolean> cir) {
        if (BuzzierBeesSet.isHidden((Item) (Object) this)) cir.setReturnValue(false);
    }
}
