package com.SaintAlfonzo.creativecompat.mixin;

import net.blay09.mods.trashslot.api.SlotRenderStyle;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TrashSlot 12.1.0 clamps the slot's Y position against the screen <em>width</em>, so a slot under
 * a tall panel (big backpacks at a high GUI scale) can sit below the bottom of the window. This
 * re-clamps it against the height so it always stays reachable.
 */
@Mixin(value = TrashSlotComponent.class, remap = false)
public abstract class TrashSlotComponentMixin {
    @Shadow @Final private AbstractContainerScreen<?> screen;
    @Shadow private SlotRenderStyle renderStyle;

    @Inject(method = "getAnchoredY", at = @At("RETURN"), cancellable = true)
    private void creativeengineering$clampToHeight(CallbackInfoReturnable<Integer> cir) {
        int max = Math.max(0, screen.height - renderStyle.getRenderHeight());
        if (cir.getReturnValueI() > max) {
            cir.setReturnValue(max);
        }
    }
}
