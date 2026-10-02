package com.SaintAlfonzo.creativeengineering.mixin;

import cy.jdkdigital.everythingcopper.common.item.CopperPickaxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.SaintAlfonzo.creativeengineering.CETiers;

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
