package com.SaintAlfonzo.creativemagic.mixin;

import com.itayfeder.nock_enough_arrows.enchantments.StockpileEnchantment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StockpileEnchantment.class)
public class StockpileEnchantmentMixin {

    // Vanilla Enchantment#isDiscoverable: SRG name, since this jar ships no refmap and the method is obfuscated at
    // runtime (the two below are Forge methods and keep their names).
    public boolean m_6592_() {
        return false;
    }

    // Vanilla Enchantment#isTradeable (SRG, same reason): keeps the book out of every trade that picks from tradeable
    // enchantments (vanilla and Dynamic Villager Trades librarians, Lightman's Currency cashiers).
    public boolean m_6594_() {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    public boolean isAllowedOnBooks() {
        return false;
    }
}
