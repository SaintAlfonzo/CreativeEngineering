package com.SaintAlfonzo.creativedelight.golden.registry;

import com.SaintAlfonzo.creativedelight.golden.GoldenFoodMod;
import com.SaintAlfonzo.creativedelight.golden.content.GoldenChorusFruitItem;
import com.SaintAlfonzo.creativedelight.golden.content.ShinyItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.EnchantedGoldenAppleItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

/**
 * The Golden Food items, upstream ids (feature_writeup/golden_food.md 2). Every golden food eats like a golden apple
 * (4 hunger, 1.2 saturation, Regeneration II 5 s, Absorption I 2 min, always edible; rare), every enchanted one like
 * upstream's enchanted golden apple (8 hunger, 8.0 saturation, Regeneration II 20 s, Resistance and Fire Resistance
 * 5 min, Absorption IV 2 min, always edible; epic, glint, stack of 1). The item class only changes how the food is
 * used: chorus fruit teleports, stews leave a bowl, milk clears effects (and, as upstream, never applies its food
 * values), honey clears poison and leaves a bottle.
 * <p>
 * As upstream, three vanilla ids are replaced: {@code minecraft:golden_apple}, {@code minecraft:enchanted_golden_apple}
 * and {@code minecraft:golden_carrot} (the carrot gains the golden apple's effects, the enchanted apple the higher
 * values above and a stack of 1), all three moved into the Golden Food tab.
 */
public final class GoldenItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GoldenFoodMod.MODID);
    /** Upstream's replacements of vanilla items (same ids, so the vanilla entries are overridden). */
    public static final DeferredRegister<Item> VANILLA_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");

    public static final RegistryObject<Item> GOLDEN_APPLE = VANILLA_ITEMS.register("golden_apple", () -> new Item(golden()));
    public static final RegistryObject<Item> ENCHANTED_GOLDEN_APPLE = VANILLA_ITEMS.register("enchanted_golden_apple",
            () -> new EnchantedGoldenAppleItem(enchanted()));
    public static final RegistryObject<Item> GOLDEN_CARROT = VANILLA_ITEMS.register("golden_carrot", () -> new Item(golden()));

    public static final RegistryObject<Item> GOLDEN_CAKE = ITEMS.register("golden_cake",
            () -> new BlockItem(GoldenBlocks.GOLDEN_CAKE.get(), new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ENCHANTED_GOLDEN_CAKE = ITEMS.register("enchanted_golden_cake",
            () -> new ShinyItems.Block(GoldenBlocks.ENCHANTED_GOLDEN_CAKE.get(), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    static {
        // Upstream's registration order (the creative tab lists items in it).
        plain("melon_slice");
        plain("sweet_berries");
        plain("glow_berries");
        pair("chorus_fruit", p -> new GoldenChorusFruitItem(p, 32, 10, false), p -> new GoldenChorusFruitItem(p, 64, 5, true), 64, 1);
        // the golden carrot is the replaced vanilla item above; only its enchanted form is a goldenfood id
        ITEMS.register("enchanted_golden_carrot", () -> new EnchantedGoldenAppleItem(enchanted()));
        for (String food : new String[]{"baked_potato", "beetroot", "dried_kelp", "cooked_beef", "cooked_porkchop", "cooked_mutton",
                "cooked_chicken", "cooked_rabbit", "cooked_cod", "cooked_salmon", "bread", "cookie", "pumpkin_pie"}) {
            plain(food);
        }
        for (String stew : new String[]{"mushroom_stew", "beetroot_soup", "rabbit_stew"}) {
            pair(stew, p -> new BowlFoodItem(p.craftRemainder(Items.BOWL)), p -> new ShinyItems.Bowl(p.craftRemainder(Items.BOWL)), 1, 1);
        }
        pair("milk_bucket", p -> new ShinyItems.GoldenMilk(p.craftRemainder(Items.BUCKET)), p -> new ShinyItems.Milk(p.craftRemainder(Items.BUCKET)), 1, 1);
        pair("honey_bottle", p -> new HoneyBottleItem(p.craftRemainder(Items.GLASS_BOTTLE)),
                p -> new ShinyItems.Honey(p.craftRemainder(Items.GLASS_BOTTLE)), 8, 4);
    }

    private GoldenItems() {
    }

    /** A golden food and its enchanted form, plain items (enchanted: vanilla's glinting golden apple class). */
    private static void plain(String food) {
        pair(food, Item::new, EnchantedGoldenAppleItem::new, 64, 1);
    }

    private static void pair(String food, Function<Item.Properties, Item> golden, Function<Item.Properties, Item> enchanted,
                             int goldenStack, int enchantedStack) {
        ITEMS.register("golden_" + food, () -> golden.apply(golden().stacksTo(goldenStack)));
        ITEMS.register("enchanted_golden_" + food, () -> enchanted.apply(enchanted().stacksTo(enchantedStack)));
    }

    static Item.Properties golden() {
        return new Item.Properties().rarity(Rarity.RARE).tab(GoldenTab.TAB).food(new FoodProperties.Builder()
                .nutrition(4).saturationMod(1.2F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F)
                .alwaysEat().build());
    }

    static Item.Properties enchanted() {
        return new Item.Properties().rarity(Rarity.EPIC).tab(GoldenTab.TAB).stacksTo(1).food(new FoodProperties.Builder()
                .nutrition(8).saturationMod(8.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3), 1.0F)
                .alwaysEat().build());
    }
}
