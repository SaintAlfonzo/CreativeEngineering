package com.SaintAlfonzo.creativecommon.compat;

import java.util.AbstractList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * Cache behind {@code ArmorTrimsLargeItemListsMixin} (perf.md 10.2 #1, TODO perf #10). Armor Trims 1.3.3's
 * {@code LargeItemLists#getAllItemsOfType} copies the whole item registry on every call, and
 * {@code getAllTrimmable} calls it once per trimmable class. {@code TrimmableItem#applyTrim} asks for that list on
 * every trim, so the 288 JEI trim recipes took 58 s of every world join; the trimmed-item decorator (every rendered
 * stack), the smithing screen and the untrimming recipe ask for it too.
 *
 * <p>Holds nothing until Forge locks the item registry, so mod loading sees Armor Trims' own lists. After that:
 * {@code getAllItemsOfType} per class for the session (the item set can't change); {@code getAllTrimmable} until the
 * next tag load (it adds tagged items), or until Armor Trims' trimmable class or tag lists change size.
 * The lists handed out are read-only, as Armor Trims' own {@code getAllItemsOfType} lists already are; its callers
 * only read and call {@code contains}, which the trimmable list answers from a set.
 *
 * <p>Allowlist ({@code gear/tuning #14}, gear_tuning.md Armor Trims): when the item tag
 * {@code creativeengineering:trimmable_armor} (KubeJS equipmenttags.js) has members, the cached trimmable list keeps
 * only them, so the smithing table ({@code TrimmableItem.applyTrim} checks the list), the JEI trim and untrimming pages
 * and the trimmed-item decorator all see the standard armor sets only. An empty or missing tag keeps Armor Trims' list.
 */
public final class ArmorTrimsItemCache {

    private static final TagKey<Item> TRIMMABLE_ARMOR = TagKey.create(net.minecraft.core.Registry.ITEM_REGISTRY,
            new ResourceLocation("creativeengineering", "trimmable_armor"));

    private static final Map<Class<?>, List<Item>> BY_TYPE = new ConcurrentHashMap<>();
    /** Bumped on every tag (re)load; the trimmable list is valid for one generation. */
    private static volatile int generation = 1;
    private static volatile Trimmable trimmable;

    private ArmorTrimsItemCache() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
            generation++;
            trimmable = null;
        });
    }

    public static boolean ready() {
        return ForgeRegistries.ITEMS instanceof ForgeRegistry<Item> items && items.isLocked();
    }

    public static List<Item> ofType(Class<?> type) {
        return BY_TYPE.get(type);
    }

    public static void putOfType(Class<?> type, List<Item> items) {
        if (ready()) {
            BY_TYPE.putIfAbsent(type, List.copyOf(items));
        }
    }

    /** The cached trimmable list, or null when there is none for these list sizes and this tag generation. */
    public static List<Item> trimmable(int classes, int tags) {
        Trimmable t = trimmable;
        return t != null && t.generation == generation && t.classes == classes && t.tags == tags ? t : null;
    }

    public static List<Item> putTrimmable(int classes, int tags, List<Item> items) {
        if (!ready()) {
            return items;
        }
        Trimmable t = new Trimmable(generation, classes, tags, allowed(items));
        trimmable = t;
        return t;
    }

    /** Armor Trims' list cut to the allowlist tag when the tag has members; else unchanged. */
    private static Item[] allowed(List<Item> items) {
        boolean anyTagged = false;
        for (Item item : items) {
            if (item.builtInRegistryHolder().is(TRIMMABLE_ARMOR)) {
                anyTagged = true;
                break;
            }
        }
        if (!anyTagged) {
            return items.toArray(new Item[0]);
        }
        return items.stream().filter(item -> item.builtInRegistryHolder().is(TRIMMABLE_ARMOR)).toArray(Item[]::new);
    }

    /** Read-only list in Armor Trims' order (duplicates kept), with a set for {@code contains}. */
    private static final class Trimmable extends AbstractList<Item> {
        final int generation;
        final int classes;
        final int tags;
        private final Item[] items;
        private final Set<Item> set;

        Trimmable(int generation, int classes, int tags, Item[] items) {
            this.generation = generation;
            this.classes = classes;
            this.tags = tags;
            this.items = items;
            this.set = new HashSet<>(items.length * 2);
            for (Item item : items) {
                set.add(item);
            }
        }

        @Override
        public Item get(int index) {
            return items[index];
        }

        @Override
        public int size() {
            return items.length;
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }
    }
}
