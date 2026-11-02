package com.SaintAlfonzo.creativedelight.barbeques.registry;

import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlockItem;
import com.SaintAlfonzo.creativedelight.barbeques.item.BBQSandwichItem;
import com.SaintAlfonzo.creativedelight.barbeques.item.BBQSeasoning;
import com.SaintAlfonzo.creativedelight.barbeques.item.BBQSkewerItem;
import com.SaintAlfonzo.creativedelight.barbeques.item.FoodItem;
import com.SaintAlfonzo.creativedelight.barbeques.item.SeasoningItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModEffects;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Items, ids and values as 1.3.0 (BBQDItems, BBQSkewers, BBQSeasoning). All in Farmer's Delight's creative tab
 * (ModItems.basicItem()), as the other Creative Delight members; 1.3.0's own tab is not registered.
 */
public final class BBQItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BarbequesDelight.MODID);

    public static final TagKey<Item> GRILLED_SKEWERS = ItemTags.create(BarbequesDelight.loc("grilled_skewers"));
    public static final TagKey<Item> RAW_SKEWERS = ItemTags.create(BarbequesDelight.loc("raw_skewers"));

    public static final RegistryObject<Item> GRILL = ITEMS.register("grill",
            () -> new GrillBlockItem(BBQBlocks.GRILL.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> BASIN = ITEMS.register("basin",
            () -> new BlockItem(BBQBlocks.BASIN.get(), ModItems.basicItem()));
    public static final RegistryObject<Item> TRAY = ITEMS.register("tray",
            () -> new BlockItem(BBQBlocks.TRAY.get(), ModItems.basicItem()));

    /** 10 raw (not edible) and 10 cooked skewers; both return the stick when crafted with or eaten. */
    public enum Skewer {
        COD(7, 1.0F),
        SALMON(7, 1.0F),
        CHICKEN(7, 0.7F),
        MUSHROOM(6, 0.4F),
        BEEF(8, 0.7F, () -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 0), 0.5F),
        LAMB(12, 0.8F, () -> new MobEffectInstance(MobEffects.REGENERATION, 1800, 0), 0.5F),
        RABBIT(10, 0.8F, () -> new MobEffectInstance(MobEffects.JUMP, 1800, 0), 1.0F),
        PORK_SAUSAGE(8, 0.7F, () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1800, 0), 0.5F),
        POTATO(6, 0.6F, () -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 1800, 0), 0.5F),
        VEGETABLE(5, 0.5F, () -> new MobEffectInstance(MobEffects.REGENERATION, 1200, 0), 0.25F);

        public final RegistryObject<Item> raw;
        public final RegistryObject<Item> grilled;

        Skewer(int nutrition, float saturation) {
            this(nutrition, saturation, null, 0);
        }

        Skewer(int nutrition, float saturation, Supplier<MobEffectInstance> effect, float chance) {
            String id = name().toLowerCase(Locale.ROOT);
            FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
            if (effect != null) {
                food.effect(effect, chance);
            }
            FoodProperties built = food.build();
            raw = ITEMS.register("raw_" + id + "_skewer",
                    () -> new BBQSkewerItem(ModItems.basicItem().craftRemainder(Items.STICK)));
            grilled = ITEMS.register("grilled_" + id + "_skewer",
                    () -> new BBQSkewerItem(ModItems.basicItem().craftRemainder(Items.STICK).food(built)));
        }
    }

    static {
        // registers the 20 skewers in 1.3.0's order (the enum's constants)
        Skewer.values();
    }

    public static final RegistryObject<Item> BURNT_SKEWER = ITEMS.register("burnt_skewer",
            () -> new FoodItem(ModItems.basicItem().craftRemainder(Items.STICK).food(new FoodProperties.Builder()
                    .alwaysEat().nutrition(4).saturationMod(0.2F)
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 1), 1.0F)
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 1), 1.0F)
                    .build())));
    public static final RegistryObject<Item> KEBAB_WRAP = ITEMS.register("kebab_wrap",
            () -> new BBQSandwichItem(ModItems.basicItem().food(new FoodProperties.Builder().nutrition(8).saturationMod(0.7F)
                    .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 1200, 0), 0.5F).build())));
    public static final RegistryObject<Item> KEBAB_SANDWICH = ITEMS.register("kebab_sandwich",
            () -> new BBQSandwichItem(ModItems.basicItem().food(new FoodProperties.Builder().nutrition(14).saturationMod(0.7F)
                    .build())));
    public static final RegistryObject<Item> BIBIMBAP = ITEMS.register("bibimbap",
            () -> new BBQSandwichItem(ModItems.basicItem().food(new FoodProperties.Builder().nutrition(16).saturationMod(0.7F)
                    .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 2400, 0), 1.0F).build())));

    /** The six seasonings: powders 64 uses, sauces 16 (durability, so they stack to 1). */
    public static final Map<BBQSeasoning, RegistryObject<Item>> SEASONINGS = new EnumMap<>(BBQSeasoning.class);

    static {
        for (BBQSeasoning s : BBQSeasoning.values()) {
            SEASONINGS.put(s, ITEMS.register(s.itemId(),
                    () -> new SeasoningItem(ModItems.basicItem().durability(s.powder ? 64 : 16), s)));
        }
    }

    public static ResourceLocation id(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    private BBQItems() {
    }
}
