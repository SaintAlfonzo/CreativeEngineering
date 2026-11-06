package com.SaintAlfonzo.creativemagic.mixin;

import com.SaintAlfonzo.creativemagic.compat.TrinketCaseRules;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Trinket Case's item-backed inventory ({@code BaubleBoxItem.getInventory}'s anonymous class).
 * Its {@code canPlaceItem} governs clicking a stack onto the case item and Quark's sort / transfer.
 */
@Mixin(targets = "vazkii.botania.common.item.BaubleBoxItem$1", remap = false)
public abstract class TrinketCaseInventoryMixin {

    @Inject(method = "m_7013_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$anyItem(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (TrinketCaseRules.isOpenCase((Container) (Object) this, stack)) cir.setReturnValue(false);
        else if (TrinketCaseRules.allowed(stack)) cir.setReturnValue(true);
    }
}
