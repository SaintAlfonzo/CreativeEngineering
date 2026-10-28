package ce.mixin;

import ce.shared.HiddenItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps {@link HiddenItems} out of every creative tab and the creative search by answering false from
 * {@code Item.allowedIn} (SRG m_220152_, named directly so no refmap is needed). Shared source
 * (development/shared/itemhidden).
 */
@Mixin(value = Item.class, remap = false)
public abstract class ItemHiddenMixin {

    @Inject(method = "m_220152_", at = @At("HEAD"), cancellable = true, remap = false)
    private void ce$hidden(CreativeModeTab tab, CallbackInfoReturnable<Boolean> cir) {
        if (HiddenItems.isHidden((Item) (Object) this)) cir.setReturnValue(false);
    }
}
