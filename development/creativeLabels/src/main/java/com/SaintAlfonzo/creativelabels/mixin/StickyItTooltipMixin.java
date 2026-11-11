package com.SaintAlfonzo.creativelabels.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Switches Sticky It's tooltip handler off (sticky_notes.md 3.13): its {@code ItemTooltipEvent} listener wrote
 * "Label:" lines from the old {@code LabelName1..3} tags, created an empty {@code {}} tag on every hovered item
 * (the bowl stacking bug) and added "[Shift]" text to its own note and eraser. Both of its {@code execute}
 * overloads end in the private static {@code execute(Event, ItemStack, List)}, cancelled here at its head; the
 * public two-argument overload is left alone so other callers still link. Items already carrying the old tags
 * keep them.
 * <p>
 * Pseudo and by name: Sticky It is an art donor until phase 5 and may be removed; {@code CLMixinPlugin} only
 * applies this mixin while {@code sticky_it} is loaded. The target is an MCreator class with no obfuscation, so
 * nothing here is remapped.
 */
@Pseudo
@Mixin(targets = "net.mcreator.sticky_it.procedures.TooltipRenderProcedure", remap = false)
public class StickyItTooltipMixin {

    @Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void creativelabels$cancelTooltip(Event event, ItemStack stack, List<Component> tooltip, CallbackInfo ci) {
        ci.cancel();
    }
}
