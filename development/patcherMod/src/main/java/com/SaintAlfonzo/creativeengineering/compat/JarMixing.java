package com.SaintAlfonzo.creativeengineering.compat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Mixed Supplementaries jars. A jar normally holds one stack of one cookie kind in its single slot; with
 * {@code SupplementariesJarTileMixin} it has one slot per capacity point (12) holding one item each, filled bottom
 * to top, so different items can share a jar and each layer keeps its own item.
 *
 * <p>What may share a jar: two items mix when they are the same item, or when one
 * {@code #creativeengineering:jar_family/<name>} tag holds both (cookies, candied berries ...). A new item has to
 * share one family with every item already inside. Items in no family still go in a jar on their own, as before.
 * What goes in a jar at all is still {@code #supplementaries:cookies}. Tags: server_scripts/tags/jartags.js,
 * feature_writeup/mixed_jars.md.
 *
 * <p>Everything here works on the jar as a plain {@link Container} so it runs on the tile, the client copy and the
 * dummy tile Supplementaries builds from a jar item in an inventory.
 */
public final class JarMixing {

    private static final String FAMILY_NAMESPACE = "creativeengineering";
    private static final String FAMILY_PREFIX = "jar_family/";

    private JarMixing() {}

    /** The jar_family tags an item is in. */
    public static Set<TagKey<Item>> families(Item item) {
        return item.builtInRegistryHolder().tags()
                .filter(t -> t.location().getNamespace().equals(FAMILY_NAMESPACE)
                        && t.location().getPath().startsWith(FAMILY_PREFIX))
                .collect(Collectors.toSet());
    }

    /** True when {@code stack} may join what is in the jar: same item as all of it, or one family covering all. */
    public static boolean canMix(Container jar, ItemStack stack) {
        Item item = stack.getItem();
        List<Item> others = new ArrayList<>();
        for (int i = 0; i < jar.getContainerSize(); i++) {
            ItemStack s = jar.getItem(i);
            if (!s.isEmpty() && s.getItem() != item && !others.contains(s.getItem())) {
                others.add(s.getItem());
            }
        }
        if (others.isEmpty()) {
            return true;
        }
        for (TagKey<Item> family : families(item)) {
            if (others.stream().allMatch(o -> o.builtInRegistryHolder().is(family))) {
                return true;
            }
        }
        return false;
    }

    public static int total(Container jar) {
        int n = 0;
        for (int i = 0; i < jar.getContainerSize(); i++) {
            n += jar.getItem(i).getCount();
        }
        return n;
    }

    /** The lowest empty slot, or -1 when every slot is filled. */
    public static int firstEmpty(Container jar) {
        for (int i = 0; i < jar.getContainerSize(); i++) {
            if (jar.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /** The highest filled slot (the top layer), or -1 when empty. */
    public static int top(Container jar) {
        for (int i = jar.getContainerSize() - 1; i >= 0; i--) {
            if (!jar.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Puts the contents back into one item per slot from the bottom, keeping their order: fills the gap an
     * extraction from the middle left (Supplementaries' inventory tweak takes slot 0, item handlers any slot), and
     * splits the one big stack of a jar saved before mixing ("Cookie x5" in slot 0). Anything past the last slot is
     * dropped, which only happens if the jar capacity config was lowered.
     */
    public static void normalize(NonNullList<ItemStack> slots) {
        List<ItemStack> layers = new ArrayList<>();
        for (ItemStack s : slots) {
            for (int n = 0; n < s.getCount(); n++) {
                ItemStack one = s.copy();
                one.setCount(1);
                layers.add(one);
            }
        }
        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, i < layers.size() ? layers.get(i) : ItemStack.EMPTY);
        }
    }

    /** One stack per kind with its total, in order of the lowest layer of each kind (tooltip, WTHIT line). */
    public static List<ItemStack> kinds(Iterable<ItemStack> slots) {
        Map<Item, ItemStack> byItem = new LinkedHashMap<>();
        for (ItemStack s : slots) {
            if (s.isEmpty()) {
                continue;
            }
            ItemStack kind = byItem.get(s.getItem());
            if (kind == null) {
                byItem.put(s.getItem(), s.copy());
            } else {
                kind.grow(s.getCount());
            }
        }
        return new ArrayList<>(byItem.values());
    }
}
