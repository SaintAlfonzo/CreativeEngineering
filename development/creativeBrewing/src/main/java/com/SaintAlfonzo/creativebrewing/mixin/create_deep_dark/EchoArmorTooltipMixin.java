package com.SaintAlfonzo.creativebrewing.mixin.create_deep_dark;

import create_deep_dark.item.EchoArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Create: Deep Dark's echo armor tooltip still advertises the ORIGINAL full-set bonus —
 * each piece hardcodes "With full set on: Resistance II (infinite) / Strength II (infinite)"
 * in its own {@code appendHoverText}. {@link EchoArmorEffectProcedureMixin} actually swaps
 * that bonus to Night Vision + {@code better_beacons:phantom_bane} ("Phantom Bane" — see its
 * en_us.json), so the tooltip was lying about what the set does. This rewrites the two
 * per-effect lines to match; both effects still reapply every tick while the full set is
 * worn, same as before, hence still "(infinite)".
 *
 * <p>{@code EchoArmorItem.Boots/Chestplate/Leggings/Helmet} each carry an identical,
 * separately-compiled copy of the three-line tooltip (no shared method), so one
 * {@code @Mixin} lists all four classes and the two {@link ModifyConstant} handlers apply
 * to each. {@code remap = false}: mod classes, no Mixin refmap in this project — see
 * [[patchermod-coremod]] / {@link EchoArmorEffectProcedureMixin}.
 */
@Mixin(value = {
        EchoArmorItem.Boots.class,
        EchoArmorItem.Chestplate.class,
        EchoArmorItem.Leggings.class,
        EchoArmorItem.Helmet.class
}, remap = false)
public class EchoArmorTooltipMixin {

    @ModifyConstant(
            method = "m_7373_",
            constant = @Constant(stringValue = "§7- §aResistance II §7(infinite)"),
            remap = false
    )
    private String creativeengineering$nightVisionLine(String original) {
        return "§7- §aNight Vision §7(infinite)";
    }

    @ModifyConstant(
            method = "m_7373_",
            constant = @Constant(stringValue = "§7- §aStrength II §7(infinite)"),
            remap = false
    )
    private String creativeengineering$phantomBaneLine(String original) {
        return "§7- §aPhantom Bane §7(infinite)";
    }
}
