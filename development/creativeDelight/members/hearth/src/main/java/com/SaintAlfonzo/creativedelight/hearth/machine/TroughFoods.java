package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Whether an item is breeding food for any animal, by {@link Animal#isFood} rather than upstream's vanilla food tags
 * (3.1 Trough), so modded animals count. One throwaway instance per animal type is built once (never added to the
 * world); answers are cached per item.
 */
final class TroughFoods {
    private static List<Animal> samples;
    private static final Map<Item, Boolean> RESULTS = new HashMap<>();

    private TroughFoods() {
    }

    static boolean isFood(Level level, ItemStack stack) {
        return RESULTS.computeIfAbsent(stack.getItem(), item -> test(level, stack));
    }

    private static boolean test(Level level, ItemStack stack) {
        for (Animal animal : samples(level)) {
            try {
                if (animal.isFood(stack)) return true;
            } catch (RuntimeException ignored) {
                // a modded isFood that needs world state: treat as not food
            }
        }
        return false;
    }

    private static List<Animal> samples(Level level) {
        if (samples != null) return samples;
        List<Animal> built = new ArrayList<>();
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
            MobCategory category = type.getCategory();
            if (category == MobCategory.MISC || category == MobCategory.MONSTER) continue;
            try {
                if (type.create(level) instanceof Animal animal) built.add(animal);
            } catch (RuntimeException ignored) {
                // a mod's constructor that refuses a bare level: skip that type
            }
        }
        samples = built;
        return built;
    }

    static void clear() {
        samples = null;
        RESULTS.clear();
    }
}
