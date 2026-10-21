package com.SaintAlfonzo.creativeengineering.mixin;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.crafting.AbstractTableRenderer;
import com.SaintAlfonzo.creativeengineering.compat.AeTooltipStacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AE2 crafting CPU status and crafting plan tables: the hovered entry ({@code hoveredStack}, set just before)
 * becomes the pending stack for {@code drawTooltipWithHeader}, see {@link AeTooltipStacks}.
 */
@Mixin(value = AbstractTableRenderer.class, remap = false)
public abstract class AeTableRendererTooltipMixin {

    @Shadow
    private GenericStack hoveredStack;

    @Inject(method = "render", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lappeng/client/gui/AEBaseScreen;drawTooltipWithHeader(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/util/List;)V"))
    private void creativeengineering$pendingEntryStack(CallbackInfo ci) {
        AeTooltipStacks.setPending(AeTooltipStacks.itemOf(this.hoveredStack));
    }
}
