package com.SaintAlfonzo.creativeequipment.mixin;

import cy.jdkdigital.everythingcopper.common.item.CopperPickaxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.SaintAlfonzo.creativeequipment.CETiers;

/**
 * Everything Copper builds its copper pickaxe on {@link Tiers#STONE}. This swaps that tier
 * for the pack's {@link CETiers#COPPER} (mining level 1 plus the
 * {@code creativeengineering:needs_copper_tool} block tag, 180 uses, speed 6) as the
 * constructor's tier argument comes in. Any other tier passed in is left alone.
 */
@Mixin(CopperPickaxeItem.class)
public class CopperPickaxeItemMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Tier creativeengineering$retierCopperPickaxe(Tier tier) {
        if (tier == Tiers.STONE) {
            return CETiers.COPPER;
        }
        return tier;
    }
}
