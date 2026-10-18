package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.QuarkSafeSort;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.base.handler.SortingHandler;

/**
 * Every Quark sort (player inventory, containers, fridges, backpacks) funnels into this overload.
 * Its extract-all-then-reinsert loop deletes items when a slot holds more than the item's max stack
 * size; {@link QuarkSafeSort} replaces it with a version that splits and validates first.
 */
@Mixin(value = SortingHandler.class, remap = false)
public abstract class QuarkSortingHandlerMixin {

    @Inject(method = "sortInventory(Lnet/minecraftforge/items/IItemHandler;II[I)V", at = @At("HEAD"),
            cancellable = true, remap = false)
    private static void creativeengineering$safeSort(IItemHandler handler, int iStart, int iEnd, int[] lockedSlots,
                                                     CallbackInfo ci) {
        QuarkSafeSort.sort(handler, iStart, iEnd, lockedSlots);
        ci.cancel();
    }
}
