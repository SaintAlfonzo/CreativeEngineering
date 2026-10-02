package com.SaintAlfonzo.creativeengineering.mixin;

import com.itayfeder.nock_enough_arrows.enchantments.RecoveryEnchantment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RecoveryEnchantment.class)
public class RecoveryEnchantmentMixin {

    public boolean isDiscoverable() {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    public boolean isAllowedOnBooks() {
        return false;
    }
}
