package com.axperty.storagedelight.registry;

import com.axperty.storagedelight.item.FuelBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** One block item per {@link BlockRegistry#ALL} entry: 300 ticks of fuel, none for the nether woods. */
public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "storagedelight");
    /** The creative tab icon (CreativeTabRegistry). */
    public static final RegistryObject<Item> OAK_DRAWER;

    static {
        RegistryObject<Item> icon = null;
        for (var entry : BlockRegistry.ALL.entrySet()) {
            String name = entry.getKey();
            RegistryObject<Item> item = ITEMS.register(name, () -> {
                boolean nether = name.contains("crimson") || name.contains("warped");
                return nether
                        ? new BlockItem(entry.getValue().get(), basicItem())
                        : new FuelBlockItem(entry.getValue().get(), basicItem(), 300);
            });
            if (name.equals("oak_drawer")) {
                icon = item;
            }
        }
        OAK_DRAWER = icon;
    }

    public static Properties basicItem() {
        return new Properties().tab(CreativeTabRegistry.CREATIVE_TAB);
    }
}
