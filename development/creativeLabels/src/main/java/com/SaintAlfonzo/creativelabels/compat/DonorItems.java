package com.SaintAlfonzo.creativelabels.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * The donor mods' own items, out of play while their jars stay as art donors (sticky_notes.md 3.13): Sticky It's
 * note and eraser and Labels' label are hidden from every creative tab and the creative search through
 * {@code ItemHiddenMixin} ({@code Item.allowedIn} answers false), and the pack hides them from JEI and removes
 * their recipes (kubejs removed_items.js, server_scripts/other/creative_labels.js). Leftover stacks stay inert
 * and vanish with the jars in phase 5.
 * <p>
 * Only resource locations are held here: the registry is read when a check is made, never at class
 * initialisation, so this class is safe to touch from a mixin before the donors have registered anything.
 */
public final class DonorItems {

    public static final ResourceLocation STICKY_IT_NOTE = new ResourceLocation("sticky_it", "sticky_note");
    public static final ResourceLocation STICKY_IT_ERASER = new ResourceLocation("sticky_it", "eraser");
    public static final ResourceLocation LABELS_LABEL = new ResourceLocation("labels", "label");

    /** Every donor item hidden from the creative tabs. */
    public static final Set<ResourceLocation> HIDDEN = Set.of(STICKY_IT_NOTE, STICKY_IT_ERASER, LABELS_LABEL);

    private DonorItems() {}

    /** True when {@code item} is one of the donor items in {@link #HIDDEN} (registry lookup at call time). */
    public static boolean isHidden(Item item) {
        if (item == null) return false;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null && HIDDEN.contains(key);
    }
}
