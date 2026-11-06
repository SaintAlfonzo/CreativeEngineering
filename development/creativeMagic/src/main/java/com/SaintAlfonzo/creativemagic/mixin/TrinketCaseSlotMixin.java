package com.SaintAlfonzo.creativemagic.mixin;

import com.SaintAlfonzo.creativemagic.compat.TrinketCaseRules;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The 24 case slots of the Trinket Case menu (anonymous {@code Slot} in {@code BaubleBoxContainer}). */
@Mixin(targets = "vazkii.botania.client.gui.box.BaubleBoxContainer$1", remap = false)
public abstract class TrinketCaseSlotMixin {

    @Inject(method = "m_5857_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$anyItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (TrinketCaseRules.isOpenCase(((Slot) (Object) this).container, stack)) cir.setReturnValue(false);
        else if (TrinketCaseRules.allowed(stack)) cir.setReturnValue(true);
    }
}
