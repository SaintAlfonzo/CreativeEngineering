package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.LightmansTooltipStacks;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Lightman's Currency (2.2.1.3g) draws every tooltip, item tooltips included, through
 * {@code EasyGuiGraphics.renderComponentTooltip(List, x, y)}, which calls the stackless
 * {@code Screen.renderComponentTooltip(pose, lines, x, y)}. Forge then fires
 * {@code RenderTooltipEvent.GatherComponents} with {@code ItemStack.EMPTY}, so AppleSkin adds nothing in
 * trader screens and storage tabs.
 *
 * <p>When the list holds lines tagged by {@link LCEasyScreenHelperMixin}, this draws it with Forge's
 * {@code renderComponentTooltip(pose, lines, x, y, stack)} instead: same lines, same position, same
 * empty-list check, plus the stack. Lists with no tagged line (buttons, notifications) are left alone.
 */
@Mixin(value = EasyGuiGraphics.class, remap = false)
public abstract class LCEasyGuiGraphicsMixin {

    @Shadow @Final private PoseStack pose;
    @Shadow @Final private LazyOptional<Screen> screen;
    @Shadow private ScreenPosition offset;

    @Inject(method = "renderComponentTooltip(Ljava/util/List;II)V", remap = false, require = 1,
            at = @At("HEAD"), cancellable = true)
    private void creativeengineering$renderWithStack(List<Component> tooltip, int x, int y, CallbackInfo ci) {
        if (tooltip.isEmpty()) return;
        ItemStack stack = LightmansTooltipStacks.find(tooltip);
        if (stack.isEmpty()) return;
        int screenX = this.offset.x + x;
        int screenY = this.offset.y + y;
        this.screen.ifPresent(s -> s.renderComponentTooltip(this.pose, tooltip, screenX, screenY, stack));
        ci.cancel();
    }
}
