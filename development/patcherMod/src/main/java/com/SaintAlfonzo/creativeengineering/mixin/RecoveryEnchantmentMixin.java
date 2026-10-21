package com.SaintAlfonzo.creativeengineering.mixin;

import com.itayfeder.nock_enough_arrows.enchantments.RecoveryEnchantment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RecoveryEnchantment.class)
public class RecoveryEnchantmentMixin {

    // Vanilla Enchantment#isDiscoverable: SRG name, since this jar ships no refmap and the method is obfuscated at
    // runtime (the two below are Forge methods and keep their names).
    public boolean m_6592_() {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    public boolean isAllowedOnBooks() {
        return false;
    }
}
