package com.SaintAlfonzo.creativedelight.roll.registry;

import com.SaintAlfonzo.creativedelight.roll.RollDelightMod;
import com.SaintAlfonzo.creativedelight.roll.content.ChopsticksItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * Every Roll Delight 1.3.1 id. Food values follow the pack's value rule (roll_delight.md 3.1.1, D6), not upstream's:
 * a cut roll is 2/3 of its input hunger, each slice 1/3; standalone crafts of cooked food give back what went in.
 * The Salmon Maki ids keep upstream's {@code amazing_salmon_roll} names (D4) so the art resolves.
 */
public final class RollItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RollDelightMod.MODID);

    public static final RegistryObject<Item> RICE_ROLL = food("rice_roll", 14, 0.6F);
    public static final RegistryObject<Item> RICE_ROLL_SLICE = food("rice_roll_slice", 7, 0.5F);
    public static final RegistryObject<Item> SALMON_MAKI_ROLL = food("amazing_salmon_roll", 13, 0.6F);
    public static final RegistryObject<Item> SALMON_MAKI = food("amazing_salmon_roll_slice", 6, 0.5F);
    public static final RegistryObject<Item> MUSHROOM_ROLL = food("mushroom_roll", 11, 0.6F);
    public static final RegistryObject<Item> MUSHROOM_ROLL_SLICE = food("mushroom_roll_slice", 6, 0.5F);
    public static final RegistryObject<Item> ANCIENT_ROLL = food("ancient_roll", 13, 0.6F);
    public static final RegistryObject<Item> ANCIENT_ROLL_SLICE = food("ancient_roll_slice", 6, 0.5F);
    /**
     * Golden tier (roll_delight.md 2.3): always edible, stack 16 as the pack's golden foods. The whole roll's values and
     * effects are copied from the golden tier by CD roll_delight/food_values.js (4 / 1.2 here as a matching base); the
     * slice is half the roll and carries no effects, so a cut roll does not hand them out three times.
     */
    public static final RegistryObject<Item> GOLDEN_ROLL = ITEMS.register("golden_roll",
            () -> new Item(props().stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationMod(1.2F).alwaysEat().build())));
    public static final RegistryObject<Item> GOLDEN_ROLL_SLICE = ITEMS.register("golden_roll_slice",
            () -> new Item(props().stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationMod(1.2F).alwaysEat().build())));
    public static final RegistryObject<Item> NETHER_ROLL = food("nether_roll", 6, 0.7F);
    public static final RegistryObject<Item> TOBIKO_ROLL = food("tobiko_roll", 7, 0.6F);
    /** As Farmer's Delight's Cooked Rice (D14): a bowl food that hands the bowl back, stack 64. */
    public static final RegistryObject<Item> COOKED_NETHER_WART = ITEMS.register("cooked_nether_wart", () -> new ConsumableItem(props()
            .food(new FoodProperties.Builder().nutrition(6).saturationMod(0.3F).build()).craftRemainder(Items.BOWL).stacksTo(64), true));
    public static final RegistryObject<Item> CHOPSTICKS = ITEMS.register("chopsticks", () -> new ChopsticksItem(props().durability(64)));

    public static final RegistryObject<Item> LEGENDARY_ROLL_MEDLEY = ITEMS.register("legendary_roll_medley",
            () -> new BlockItem(RollBlocks.LEGENDARY_ROLL_MEDLEY.get(), props().stacksTo(1)));
    public static final RegistryObject<Item> MYTHICAL_ROLL_MEDLEY = ITEMS.register("mythical_roll_medley",
            () -> new BlockItem(RollBlocks.MYTHICAL_ROLL_MEDLEY.get(), props().stacksTo(1)));
    public static final RegistryObject<Item> WRAPPED_CHOPSTICKS = ITEMS.register("wrapped_chopsticks",
            () -> new BlockItem(RollBlocks.WRAPPED_CHOPSTICKS.get(), props().stacksTo(16)));

    private RollItems() {
    }

    private static Item.Properties props() {
        return new Item.Properties().tab(RollTab.TAB);
    }

    private static RegistryObject<Item> food(String id, int hunger, float saturation) {
        return ITEMS.register(id, () -> new Item(props().food(new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation).build())));
    }
}
