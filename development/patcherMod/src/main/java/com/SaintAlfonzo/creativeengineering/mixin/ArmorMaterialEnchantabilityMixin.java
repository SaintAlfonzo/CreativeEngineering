package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.util.ArmorEnchantabilityBridge;

import net.minecraft.world.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the enchanting "enchantment value" (enchantability) of whole armour materials
 * that KubeJS can't reach. Armour reads it off {@code ArmorMaterial#getEnchantmentValue()},
 * which has no {@code kjs$} accessor, so {@code kubejs/startup_scripts/func/tune_armor.js}
 * can only warn-and-skip the {@code enchantability} key.
 *
 * <p>Felsic Gear and Create: Deep Dark each hand vanilla {@code ArmorItem} an anonymous
 * {@code ArmorMaterial} whose {@code getName()} is the bare string {@code "felsic"} /
 * {@code "echo_armor"} (base enchant values 18 / 16). Neither mod overrides
 * {@code ArmorItem#getEnchantmentValue()} — its body is just
 * {@code return this.material.getEnchantmentValue()} — so a HEAD inject there, keyed by
 * material name, covers all four pieces of a set at once. Overrides live in
 * {@code CEConfig.ARMOR_ENCHANT} (via {@link ArmorEnchantabilityBridge}) — add a
 * {@code "materialName:value"} row there for more materials, no rebuild needed.
 *
 * <p>Vanilla-class target: {@code @Mixin(ArmorItem.class)} with the method matched by SRG
 * name ({@code m_6473_}) and {@code remap = false} — this project ships no Mixin refmap and
 * only ever runs reobfuscated (same idiom as {@link SkullBlockEntityMixin}). The body's
 * {@code getMaterial()} / {@code getName()} are Mojmap and reobf'd by the build. The whole
 * lookup is wrapped so a mapping surprise degrades to "leave enchantability alone" rather
 * than breaking the enchanting table.
 */
@Mixin(ArmorItem.class)
public class ArmorMaterialEnchantabilityMixin {

    @Inject(method = "m_6473_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$overrideArmorEnchantability(CallbackInfoReturnable<Integer> cir) {
        Integer override;
        try {
            override = ArmorEnchantabilityBridge.valueFor(((ArmorItem) (Object) this).getMaterial().getName());
        } catch (Throwable ignored) {
            return;
        }
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
