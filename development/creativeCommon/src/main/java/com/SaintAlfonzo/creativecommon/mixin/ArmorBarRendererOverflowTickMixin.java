package com.SaintAlfonzo.creativecommon.mixin;

import com.redlimerl.detailab.render.ArmorBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Suppresses Detail Armor Bar's own "past 20 armor points" indicator - tiny 3px tick marks drawn
 * to the left of the bar ({@code ArmorBarRenderer#render}, guarded by {@code armorPoints.size() >
 * 20}). {@code compat.ArmorOverflowRenderer} already draws full extra rows for that overflow, so
 * the tick marks are pure visual clutter now.
 *
 * <p>{@code render} calls {@code List#size()} on {@code armorPoints} three times (decompiled
 * 2.6.4 source): once to seed {@code totalArmorPoint} (must stay real - everything else in the
 * method keys off it), once for the {@code > 20} tick-mark guard this targets (ordinal 1), and
 * once later for an unrelated low-durability bounds check (ordinal 2, must also stay real). Only
 * the ordinal-1 call is redirected, clamped so it can never read as "more than 20" - the tick-mark
 * loop's body then never runs, and nothing else in the method observes the clamped value.
 *
 * <p>{@code remap = false} since {@code ArmorBarRenderer} is an unobfuscated third-party mod
 * class (same reasoning as {@link ArmorBarRendererAccessor}); Mixin just skips this if detailab
 * isn't installed.
 */
@Mixin(value = ArmorBarRenderer.class, remap = false)
public class ArmorBarRendererOverflowTickMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 1))
    private int creativecommon$hideOverflowTicks(List<?> armorPoints) {
        return Math.min(armorPoints.size(), 20);
    }
}
