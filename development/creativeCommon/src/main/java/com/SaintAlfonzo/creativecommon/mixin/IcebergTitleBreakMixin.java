package com.SaintAlfonzo.creativecommon.mixin;

import com.anthonyhilyard.iceberg.IcebergClient;
import com.anthonyhilyard.iceberg.util.Tooltips;
import com.anthonyhilyard.legendarytooltips.tooltip.ItemModelComponent;
import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Legendary Tooltips 1.4.0 draws the name underline too high (through the spinning 3D model and the
 * title) on armor and tool tooltips, depending on which of two LOWEST-priority GatherComponents
 * handlers runs first.
 *
 * <p>Legendary Tooltips' handler rebuilds model tooltips as {@code [ItemModelComponent, title, " ", ...]}
 * (the blank line makes room for the model). Iceberg's handler inserts its {@code TitleBreakComponent}
 * after the first text line. {@code TooltipDecor} then counts the text lines before the break and
 * subtracts one for that blank line. If Iceberg runs first the break ends up after the blank line and
 * the count is right. If Legendary Tooltips runs first the break lands straight after the title, so the
 * subtraction leaves zero title lines and the underline sits 9px (one line) too high. The order is
 * just listener registration order, so it can change between launches.
 *
 * <p>Fix: at the end of Iceberg's handler, if the list starts with a model component and the break
 * sits directly after the title, move it one place down (after the blank line, or after the
 * {@code PaddingComponent} Legendary Tooltips uses for title-only tooltips). That is the same list
 * the Iceberg-first order produces. In the Iceberg-first order the model is not there yet, so this
 * does nothing.
 */
@Mixin(value = IcebergClient.class, remap = false)
public class IcebergTitleBreakMixin {

    @Inject(method = "onGatherComponentsEventEnd", remap = false, require = 1, at = @At("TAIL"))
    private static void creativecommon$breakAfterModelSpacer(RenderTooltipEvent.GatherComponents event, CallbackInfo ci) {
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (elements.size() < 4) return;
        if (!(elements.get(0).right().orElse(null) instanceof ItemModelComponent)) return;
        if (!(elements.get(2).right().orElse(null) instanceof Tooltips.TitleBreakComponent)) return;
        elements.add(3, elements.remove(2));
    }
}
