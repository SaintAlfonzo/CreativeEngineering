package com.SaintAlfonzo.creativedelight.farm.bucket;

import java.util.LinkedHashMap;
import java.util.Map;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The wooden bucket, a farming tool (moved from Creative Equipment 2026-11-09 as {@code creativedelight:wooden_*}; the old
 * {@code creativeengineering} ids are remapped by swaps/wooden_buckets.json). It carries the farm fluids only: water, Hearth and Harvest sap, milk and Create honey. Other mods' fluids
 * are matched by id, so none of them is a dependency; a missing one simply never fills. Every filled bucket uses the
 * water bucket texture until its own art exists (TODO {@code textures/art}).
 */
public final class WoodenBuckets {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeDelight.MOD_ID);

    public static final ResourceLocation WATER_ID = new ResourceLocation("minecraft", "water");
    public static final ResourceLocation SAP_ID = new ResourceLocation("hearthandharvest", "sap");
    public static final ResourceLocation MILK_ID = new ResourceLocation("minecraft", "milk");
    public static final ResourceLocation HONEY_ID = new ResourceLocation("create", "honey");

    public static final RegistryObject<Item> EMPTY = ITEMS.register("wooden_bucket",
            () -> new WoodenBucketItem(null, new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(16)));
    public static final RegistryObject<Item> WATER = filled("wooden_water_bucket", WATER_ID);
    public static final RegistryObject<Item> SAP = filled("wooden_sap_bucket", SAP_ID);
    public static final RegistryObject<Item> MILK = filled("wooden_milk_bucket", MILK_ID);
    public static final RegistryObject<Item> HONEY = filled("wooden_honey_bucket", HONEY_ID);

    /** Fluid id to filled bucket, for the fluid handler. */
    static final Map<ResourceLocation, RegistryObject<Item>> BY_FLUID = new LinkedHashMap<>();
    static {
        BY_FLUID.put(WATER_ID, WATER);
        BY_FLUID.put(SAP_ID, SAP);
        BY_FLUID.put(MILK_ID, MILK);
        BY_FLUID.put(HONEY_ID, HONEY);
    }

    private static RegistryObject<Item> filled(String name, ResourceLocation fluid) {
        return ITEMS.register(name, () -> new WoodenBucketItem(fluid,
                new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1).craftRemainder(EMPTY.get())));
    }

    private WoodenBuckets() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        MinecraftForge.EVENT_BUS.register(WoodenBucketMilking.class);
    }
}
