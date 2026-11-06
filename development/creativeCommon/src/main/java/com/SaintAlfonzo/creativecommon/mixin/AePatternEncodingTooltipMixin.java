package com.SaintAlfonzo.creativecommon.mixin;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import com.SaintAlfonzo.creativecommon.compat.AeTooltipStacks;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AE2 pattern encoding terminal: slots whose amount can be set draw the item's tooltip (plus amount lines) with
 * {@code drawTooltip}; the hovered slot's item (unwrapped from AE2's wrapper item) becomes the pending stack,
 * see {@link AeTooltipStacks}.
 */
@Mixin(value = PatternEncodingTermScreen.class, remap = false)
public class AePatternEncodingTooltipMixin {

    @Inject(method = "m_7025_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lappeng/client/gui/me/items/PatternEncodingTermScreen;drawTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/util/List;)V"))
    private void creativecommon$pendingSlotStack(CallbackInfo ci) {
        Slot slot = ((AbstractContainerScreen<?>) (Object) this).getSlotUnderMouse();
        if (slot == null) return;
        ItemStack item = slot.getItem();
        GenericStack unwrapped = GenericStack.fromItemStack(item);
        AeTooltipStacks.setPending(unwrapped != null ? AeTooltipStacks.itemOf(unwrapped) : item);
    }
}
