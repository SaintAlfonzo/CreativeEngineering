package net.satisfy.foundation.armor;

import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ArmorSets {
    private static final List<ArmorSet> SETS = new CopyOnWriteArrayList<>();

    private ArmorSets() {
    }

    static void register(ArmorSet set) {
        SETS.add(set);
    }

    public static List<ArmorSet> all() {
        return List.copyOf(SETS);
    }

    public static Optional<ArmorSet> find(Item item) {
        return SETS.stream().filter(set -> set.contains(item)).findFirst();
    }
}
