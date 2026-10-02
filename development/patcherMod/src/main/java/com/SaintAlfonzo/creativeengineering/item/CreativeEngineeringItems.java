package com.SaintAlfonzo.creativeengineering.items;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.CreativeModeTab;

public class CreativeEngineeringItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEngineering.MODID);

    public static final RegistryObject<Item> PRECISION_SAWBLADE = ITEMS.register("precision_sawblade",
        () -> new PrecisionSawbladeItem());

    public static final RegistryObject<Item> CREOSOTE_SOAKED_SAWDUST = ITEMS.register("creosote_soaked_sawdust",
        () -> new CreosoteSoakedSawdustItem());

    // arrow_fletching, zinc_grit, brass_grit, cast_iron_grit, spool, transmitter,
    // prepared_flesh_block, rose_gold_nugget, rose_gold_rod, silver_wire, nickel_wire,
    // iron_wire, zinc_wire, brass_wire, gold_wire, zinc_plate migrated to KubeJS - see
    // kubejs/startup_scripts/items/misc_items.js.

    // Plate storage blocks (aluminum/cast_iron/constantan/electrum/lead/netherite/nickel/
    // silver/steel/uranium/zinc) migrated to KubeJS - see
    // kubejs/startup_scripts/items/plate_blocks.js.

		public static final RegistryObject<Item> FLINT_AND_TERRASTEEL = ITEMS.register("flint_and_terrasteel",
        () -> new FlintAndTerraSteelItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).durability(65)));
        
    // Dust pile blocks (aluminum/copper/gold/iron/lead/nickel/silver/uranium/zinc/diamond,
    // plus rose_gold/cast_iron/electrum/steel/constantan) migrated to KubeJS - see
    // kubejs/startup_scripts/items/dust_piles.js.

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
