package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.SophisticatedPolymorphCompat;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Opening a crafting upgrade tab recomputes its result, so Polymorph sends that grid's recipe list
 * (a backpack can hold more than one crafting upgrade). Only crafting upgrades carry
 * {@link SophisticatedPolymorphCompat.RefreshableCrafting}; the result is only worked out server side.
 */
@Mixin(value = UpgradeContainerBase.class, remap = false)
public abstract class SophisticatedUpgradeOpenMixin {

    @Inject(method = "setIsOpen", at = @At("TAIL"), remap = false, require = 1)
    private void creativeengineering$refreshOnOpen(boolean isOpen, CallbackInfo ci) {
        if (isOpen && (Object) this instanceof SophisticatedPolymorphCompat.RefreshableCrafting crafting) {
            crafting.creativeengineering$refreshCraftingResult();
        }
    }
}
