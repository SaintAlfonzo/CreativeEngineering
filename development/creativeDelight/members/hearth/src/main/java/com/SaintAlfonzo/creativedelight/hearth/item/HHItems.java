package com.SaintAlfonzo.creativedelight.hearth.item;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.block.HHBlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/**
 * Items, ids as 1.4.1, in Farmer's Delight's creative tab (ModItems.basicItem(), as the other members). With Berry Good
 * loaded 1.4.1 makes the Raspberry plain food and the Raspberry Pips the bush's planting item; the pack always has
 * Berry Good, so only that form exists here.
 */
public final class HHItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HearthAndHarvestMod.MODID);

    /** 1.4.1 HHFoodValues.RASPBERRY: 2 hunger, 0.4 saturation modifier, eaten fast. */
    public static final FoodProperties RASPBERRY_FOOD = new FoodProperties.Builder().nutrition(2).saturationMod(0.4F).fast().build();

    public static final RegistryObject<Item> RASPBERRY = ITEMS.register("raspberry",
            () -> new Item(ModItems.basicItem().food(RASPBERRY_FOOD)));
    public static final RegistryObject<Item> RASPBERRY_PIPS = ITEMS.register("raspberry_pips",
            () -> new ItemNameBlockItem(HHBlocks.RASPBERRY_BUSH.get(), ModItems.basicItem()));

    // Watering cans (hearth_and_harvest.md 4.0): copper keeps 1.4.1's id; the higher tiers are pack additions. Tools tab.
    public static final RegistryObject<Item> WATERING_CAN = can("watering_can", WateringCanTier.COPPER, false);
    public static final RegistryObject<Item> IRON_WATERING_CAN = can("iron_watering_can", WateringCanTier.IRON, false);
    public static final RegistryObject<Item> GOLDEN_WATERING_CAN = can("golden_watering_can", WateringCanTier.GOLD, false);
    public static final RegistryObject<Item> DIAMOND_WATERING_CAN = can("diamond_watering_can", WateringCanTier.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_WATERING_CAN = can("netherite_watering_can", WateringCanTier.NETHERITE, true);
    public static final RegistryObject<Item> DRAGON_WATERING_CAN = can("dragon_watering_can", WateringCanTier.DRAGON, true);

    private static RegistryObject<Item> can(String id, WateringCanTier tier, boolean fireResistant) {
        return ITEMS.register(id, () -> {
            Item.Properties props = new Item.Properties().tab(CreativeModeTab.TAB_TOOLS);
            if (fireResistant) props.fireResistant();
            return new WateringCanItem(tier, props);
        });
    }

    private HHItems() {
    }
}
