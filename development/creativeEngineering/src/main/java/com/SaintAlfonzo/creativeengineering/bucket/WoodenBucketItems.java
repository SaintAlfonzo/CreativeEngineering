package com.SaintAlfonzo.creativeengineering.bucket;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * {@code creativeengineering:wooden_bucket} and {@code creativeengineering:wooden_water_bucket}: an early
 * bucket that only ever picks up and places water. Recipe is KubeJS ({@code server_scripts/engineering/process.js});
 * the texture is cech12's Wooden Bucket (MIT) with a painted water surface.
 */
public final class WoodenBucketItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEngineering.MODID);

    public static final RegistryObject<Item> EMPTY = ITEMS.register("wooden_bucket",
            () -> new WoodenBucketItem(false, new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(16)));

    public static final RegistryObject<Item> WATER = ITEMS.register("wooden_water_bucket",
            () -> new WoodenBucketItem(true, new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1)));

    private WoodenBucketItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
