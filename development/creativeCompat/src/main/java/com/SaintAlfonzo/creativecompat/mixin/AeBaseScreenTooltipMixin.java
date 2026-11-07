package com.SaintAlfonzo.creativecompat.mixin;

import appeng.client.gui.AEBaseScreen;
import com.SaintAlfonzo.creativecompat.compat.AeTooltipStacks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/** AE2's text-only tooltip: draw with the pending item's stack when a caller set one, see {@link AeTooltipStacks}. */
@Mixin(value = AEBaseScreen.class, remap = false)
public class AeBaseScreenTooltipMixin {

    @Inject(method = "drawTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/util/List;)V", remap = false, require = 1,
            at = @At("HEAD"), cancellable = true)
    private void creativecommon$drawWithStack(PoseStack poseStack, int x, int y, List<Component> lines, CallbackInfo ci) {
        ItemStack stack = AeTooltipStacks.take();
        if (stack.isEmpty() || lines.isEmpty()) return;
        ((Screen) (Object) this).renderTooltip(poseStack, lines, Optional.empty(), x, y, stack);
        ci.cancel();
    }
}
