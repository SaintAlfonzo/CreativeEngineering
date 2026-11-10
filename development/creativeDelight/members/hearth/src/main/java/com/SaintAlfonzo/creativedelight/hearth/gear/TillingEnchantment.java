package com.SaintAlfonzo.creativedelight.hearth.gear;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** Tilling I to III: sneak-use tills 3x3, 4x4 or 5x5 (N23-12, N26-8, built for the eval). */
public class TillingEnchantment extends Enchantment {
    public static final EnchantmentCategory HOE = EnchantmentCategory.create("HEARTHANDHARVEST_HOE", item -> item instanceof HoeItem);

    public TillingEnchantment() {
        super(Rarity.UNCOMMON, HOE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }
}
