package com.SaintAlfonzo.creativeengineering.mixin;

import com.itayfeder.nock_enough_arrows.enchantments.CycleEnchantment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CycleEnchantment.class)
public class CycleEnchantmentMixin {

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
