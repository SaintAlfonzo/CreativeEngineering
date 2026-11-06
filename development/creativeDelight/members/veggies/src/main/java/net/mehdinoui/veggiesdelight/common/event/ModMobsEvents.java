package net.mehdinoui.veggiesdelight.common.event;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.mehdinoui.veggiesdelight.common.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

// Upstream's mob food, copied as is (feature_writeup/veggies_delight.md 4.10): chickens eat the seeds, parrots tame
// with them, pigs eat the vegetables, villagers pick up and share the produce. Where upstream named its own bell pepper
// and seeds, Rustic Delight's peppers and seeds take their place (when that mod is loaded).
@EventBusSubscriber(modid = "veggiesdelight", bus = Bus.MOD)
public class ModMobsEvents {
    private static final List<String> RUSTIC_PEPPERS = List.of(
            "rusticdelight:bell_pepper_red", "rusticdelight:bell_pepper_green", "rusticdelight:bell_pepper_yellow");
    private static final String RUSTIC_PEPPER_SEEDS = "rusticdelight:bell_pepper_seeds";

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            List<Item> peppers = existing(RUSTIC_PEPPERS);
            List<Item> pepperSeeds = existing(List.of(RUSTIC_PEPPER_SEEDS));

            Ingredient chickenFood = Ingredient.of(ModItems.BROCCOLI_SEEDS.get(), ModItems.CAULIFLOWER_SEEDS.get(),
                    ModItems.TURNIP_SEEDS.get(), ModItems.ZUCCHINI_SEEDS.get());
            Chicken.FOOD_ITEMS = new CompoundIngredient(Arrays.asList(Chicken.FOOD_ITEMS, chickenFood)) {};

            List<Item> pigFood = new ArrayList<>(peppers);
            pigFood.addAll(List.of(ModItems.CAULIFLOWER.get(), ModItems.CAULIFLOWER_FLORET.get(), ModItems.SWEET_POTATO.get(),
                    ModItems.TURNIP.get(), ModItems.ZUCCHINI.get(), ModItems.ZUCCHINI_SLICE.get()));
            Pig.FOOD_ITEMS = new CompoundIngredient(Arrays.asList(Pig.FOOD_ITEMS, Ingredient.of(pigFood.toArray(Item[]::new)))) {};

            Parrot.TAME_FOOD.addAll(List.of(ModItems.BROCCOLI_SEEDS.get(), ModItems.CAULIFLOWER_SEEDS.get(),
                    ModItems.TURNIP_SEEDS.get(), ModItems.ZUCCHINI_SEEDS.get()));

            Set<Item> wanted = new HashSet<>(Villager.WANTED_ITEMS);
            wanted.add(ModItems.CAULIFLOWER_BREAD.get());
            wanted.addAll(peppers);
            wanted.addAll(pepperSeeds);
            wanted.addAll(List.of(ModItems.BROCCOLI.get(), ModItems.BROCCOLI_SEEDS.get(), ModItems.CAULIFLOWER.get(),
                    ModItems.CAULIFLOWER_SEEDS.get(), ModItems.GARLIC.get(), ModItems.GARLIC_CLOVE.get(), ModItems.SWEET_POTATO.get(),
                    ModItems.TURNIP.get(), ModItems.TURNIP_SEEDS.get(), ModItems.ZUCCHINI.get(), ModItems.ZUCCHINI_SLICE.get()));
            Villager.WANTED_ITEMS = ImmutableSet.copyOf(wanted);

            Map<Item, Integer> foodPoints = new HashMap<>();
            foodPoints.put(ModItems.CAULIFLOWER_BREAD.get(), 4);
            peppers.forEach(item -> foodPoints.put(item, 1));
            for (Item item : List.of(ModItems.BROCCOLI.get(), ModItems.CAULIFLOWER.get(), ModItems.GARLIC.get(), ModItems.GARLIC_CLOVE.get(),
                    ModItems.SWEET_POTATO.get(), ModItems.TURNIP.get(), ModItems.ZUCCHINI.get(), ModItems.ZUCCHINI_SLICE.get())) {
                foodPoints.put(item, 1);
            }
            foodPoints.putAll(Villager.FOOD_POINTS);
            Villager.FOOD_POINTS = ImmutableMap.copyOf(foodPoints);
        });
    }

    private static List<Item> existing(List<String> ids) {
        List<Item> items = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation key = new ResourceLocation(id);
            if (ForgeRegistries.ITEMS.containsKey(key)) {
                items.add(ForgeRegistries.ITEMS.getValue(key));
            }
        }
        return items;
    }
}
