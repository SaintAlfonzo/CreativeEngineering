package com.SaintAlfonzo.creative_labels.mixin;

import com.SaintAlfonzo.creative_labels.compat.DonorItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the donor mods' items ({@link DonorItems}: Sticky It's note and eraser, Labels' label) out of every
 * creative tab and the creative search by answering false from {@code Item.allowedIn}, which vanilla's
 * {@code fillItemCategory} checks for each tab, including the search tab (sticky_notes.md 3.13). Neither donor
 * overrides {@code allowedIn} or {@code fillItemCategory}, so this covers them. Always applied: the lookup is by
 * registry key at call time, so a missing donor simply never matches.
 */
@Mixin(Item.class)
public abstract class ItemHiddenMixin {

    @Inject(method = "allowedIn", at = @At("HEAD"), cancellable = true)
    private void creative_labels$hideDonorItems(CreativeModeTab tab, CallbackInfoReturnable<Boolean> cir) {
        if (DonorItems.isHidden((Item) (Object) this)) cir.setReturnValue(false);
    }
}
