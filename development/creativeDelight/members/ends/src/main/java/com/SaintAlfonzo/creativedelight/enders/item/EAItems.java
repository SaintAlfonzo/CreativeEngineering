package com.SaintAlfonzo.creativedelight.enders.item;

import com.SaintAlfonzo.creativedelight.enders.EndersAdditionsMod;
import com.SaintAlfonzo.creativedelight.enders.block.EABlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;

/**
 * The 1.1.4 items, under Ender's Delight's namespace. All in Farmer's Delight's creative tab (ModItems.basicItem(), as
 * 1.1.4's block items).
 */
public final class EAItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EndersAdditionsMod.NAMESPACE);

    public static final RegistryObject<Item> AMBERVEIL = ITEMS.register("amberveil",
            () -> new BlockItem(EABlocks.AMBERVEIL_MUSHROOM.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> ETHEREAL_SAFFRON = ITEMS.register("ethereal_saffron",
            () -> new BlockItem(EABlocks.ETHEREAL_SAFFRON_BUSH.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> CHORUSFLAME = ITEMS.register("chorusflame",
            () -> new BlockItem(EABlocks.CHORUS_FLAME_BLOCK.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> VOIDPEPPER = ITEMS.register("voidpepper",
            () -> new BlockItem(EABlocks.VOIDPEPPER_BLOCK.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> CRUSHED_VOIDPEPPER = ITEMS.register("crushed_voidpepper",
            () -> new Item(ModItems.basicItem()));

    public static final RegistryObject<Item> AMBERVEIL_STEW = ITEMS.register("amberveil_stew",
            () -> dish(EAFoods.AMBERVEIL_STEW, true));
    public static final RegistryObject<Item> AMBERVEILED_CURRY = ITEMS.register("amberveiled_curry",
            () -> dish(EAFoods.AMBERVEILED_CURRY, true));
    public static final RegistryObject<Item> CHICKEN_CURRY = ITEMS.register("chicken_curry",
            () -> dish(EAFoods.CHICKEN_CURRY, true));
    public static final RegistryObject<Item> STEAK_FRIES = ITEMS.register("steak_fries",
            () -> dish(EAFoods.STEAK_FRIES, false));
    public static final RegistryObject<Item> VEIL_OF_FLAMES_RISOTTO = ITEMS.register("veil_of_flames_risotto",
            () -> dish(EAFoods.RISOTTO, true));

    private static Item dish(FoodProperties food, boolean hasFoodEffectTooltip) {
        return new ShulkerBowlDishItem(ModItems.basicItem().food(food).stacksTo(16), hasFoodEffectTooltip);
    }

    private EAItems() {
    }
}
