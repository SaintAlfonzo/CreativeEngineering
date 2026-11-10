package com.SaintAlfonzo.creativedelight.hearth.gear;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Enchantments of the gear tranche (hearth_and_harvest.md 4.6). */
public final class HHGearRegistry {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, HearthAndHarvestMod.MODID);
    public static final RegistryObject<Enchantment> TILLING = ENCHANTMENTS.register("tilling", TillingEnchantment::new);

    private HHGearRegistry() {
    }
}
