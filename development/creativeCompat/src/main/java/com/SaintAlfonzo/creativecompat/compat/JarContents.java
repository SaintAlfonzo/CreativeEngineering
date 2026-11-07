package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * How {@link WthitHolderContents} reads a Supplementaries jar's items. Mixed jars (several items in one jar) are Creative
 * Compat's (compat.JarMixing, which sets {@link #set} at construction). Unset: a plain per-slot listing (each non-empty
 * slot is its own entry) and a plain count sum.
 */
public final class JarContents {

    private static volatile Function<List<ItemStack>, List<ItemStack>> kinds = JarContents::perSlot;
    private static volatile ToIntFunction<Container> total = JarContents::sum;

    private JarContents() {}

    public static void set(Function<List<ItemStack>, List<ItemStack>> kindsFn, ToIntFunction<Container> totalFn) {
        kinds = kindsFn;
        total = totalFn;
    }

    /** The jar's slots merged into one stack per kind of item, in slot order. */
    public static List<ItemStack> kinds(List<ItemStack> slots) {
        return kinds.apply(slots);
    }

    /** Items in the jar. */
    public static int total(Container jar) {
        return total.applyAsInt(jar);
    }

    private static List<ItemStack> perSlot(List<ItemStack> slots) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : slots) {
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private static int sum(Container jar) {
        int n = 0;
        for (int i = 0; i < jar.getContainerSize(); i++) {
            n += jar.getItem(i).getCount();
        }
        return n;
    }
}
