package com.SaintAlfonzo.creativeengineering.wateringcan;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Hearth and Harvest's Watering Can, ahead of the full H&H port (feature_writeup/hearth_and_harvest.md). Registered
 * under {@code hearthandharvest:} so the full port can take the id over without a conversion; the texture, model
 * and lang ship in this jar under the same namespace. Recipe is KubeJS ({@code server_scripts/equipment/watering_can.js}),
 * the fertiliser tag {@code #hearthandharvest:bonemeal_substitutes} is filled there too.
 */
public final class WateringCanItems {

    public static final String HNH = "hearthandharvest";

    public static final TagKey<Item> BONEMEAL_SUBSTITUTES =
            TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(HNH, "bonemeal_substitutes"));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HNH);

    public static final RegistryObject<Item> WATERING_CAN = ITEMS.register("watering_can",
            () -> new WateringCanItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));

    private WateringCanItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
