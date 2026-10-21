package com.SaintAlfonzo.creative_labels.crafting;

import com.SaintAlfonzo.creative_labels.item.EraserItem;
import com.SaintAlfonzo.creative_labels.item.NoteColours;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * What a crafting grid holds, sorted for the four note recipes (sticky_notes.md 3.6): the note stacks, at most one
 * dye, at most one special ingredient, at most one eraser, and whether anything else is there. Built once per
 * {@code matches} / {@code assemble}; the recipes decide on the counts. Pure apart from item identity, which goes
 * through {@link #isNote} / {@link #isEraser}: the headless test (whose registries are frozen, so no new Item can
 * be constructed) swaps those for stand-in vanilla items with {@link #standIns}.
 */
public final class NoteGrid {

    private static Predicate<ItemStack> noteTest = s -> s.getItem() instanceof StickyNoteItem;
    private static Predicate<ItemStack> eraserTest = s -> s.getItem() instanceof EraserItem;

    public static boolean isNote(ItemStack stack) {
        return noteTest.test(stack);
    }

    public static boolean isEraser(ItemStack stack) {
        return eraserTest.test(stack);
    }

    /** Test hook only: treat these items as the note and the eraser (null restores the class checks). */
    public static void standIns(@Nullable net.minecraft.world.item.Item note, @Nullable net.minecraft.world.item.Item eraser) {
        noteTest = note == null ? s -> s.getItem() instanceof StickyNoteItem : s -> s.is(note);
        eraserTest = eraser == null ? s -> s.getItem() instanceof EraserItem : s -> s.is(eraser);
    }

    public final List<ItemStack> notes = new ArrayList<>();
    @Nullable
    public DyeItem dye;
    public int dyes;
    @Nullable
    public NoteColours.Special special;
    public int specials;
    public int erasers;
    /** Stacks that are none of the above. */
    public int others;

    private NoteGrid() {}

    public static NoteGrid of(CraftingContainer grid) {
        NoteGrid g = new NoteGrid();
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.isEmpty()) continue;
            if (isNote(s)) {
                g.notes.add(s);
            } else if (s.getItem() instanceof DyeItem d) {
                g.dye = d;
                g.dyes++;
            } else if (isEraser(s)) {
                g.erasers++;
            } else {
                NoteColours.Special sp = NoteColours.specialFor(s.getItem());
                if (sp != null) {
                    g.special = sp;
                    g.specials++;
                } else {
                    g.others++;
                }
            }
        }
        return g;
    }

    /** True when every note stack has the same NBT as the first (colour, special and text alike). */
    public boolean notesIdentical() {
        for (int i = 1; i < notes.size(); i++) {
            if (!ItemStack.isSameItemSameTags(notes.get(0), notes.get(i))) return false;
        }
        return true;
    }

    /** A copy of {@code source} with the given count (the recipes' way of making N results without a registry). */
    public static ItemStack copies(ItemStack source, int count) {
        ItemStack out = source.copy();
        out.setCount(count);
        return out;
    }
}
