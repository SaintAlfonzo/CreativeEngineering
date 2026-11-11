package com.SaintAlfonzo.creativelabels.block;

import com.SaintAlfonzo.creativelabels.CLRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * One placed sticky note (sticky_notes.md 3.4, BRIEF 3). Also the home of the item-NBT helpers for notes, so a
 * placed note and a note item share one NBT shape:
 * <pre>
 *   display: { color: int }                 vanilla DyeableLeatherItem colour (absent = plain yellow)
 *   CreativeLabels: { Text: str, Special: str }   written only when non-empty (C5)
 * </pre>
 * The block entity stores the same compound plus {@code Day} and {@code Placer}, so {@link #toItem()} and
 * {@link #fromItem} are trivial. Keys are only written when they carry something; {@link #clean} removes empty
 * compounds and an empty tag so untouched stacks stack again.
 */
public final class NoteData {

    /** Item compound shared with {@code ItemLabels} (labelled items keep their lines under the same compound). */
    public static final String TAG = "CreativeLabels";
    public static final String KEY_TEXT = "Text";
    public static final String KEY_SPECIAL = "Special";
    public static final String KEY_DAY = "Day";
    public static final String KEY_PLACER = "Placer";
    public static final String TAG_DISPLAY = "display";
    public static final String KEY_COLOR = "color";

    /** {@link #color} value of a plain (undyed) note. */
    public static final int NO_COLOR = -1;

    /** RGB, or {@link #NO_COLOR} for plain yellow. */
    public int color = NO_COLOR;
    /** One of {@code NoteColours.SPECIALS} names, or null. */
    @Nullable
    public String special;
    /** May be empty, never null. */
    public String text = "";
    /** In-game day the note was placed. */
    public long day;
    /** Name of the player who placed it; may be empty. */
    public String placer = "";

    public NoteData() {}

    public NoteData(int color, @Nullable String special, String text, long day, String placer) {
        this.color = color;
        this.special = emptyToNull(special);
        this.text = text == null ? "" : text;
        this.day = day;
        this.placer = placer == null ? "" : placer;
    }

    public boolean isPlain() {
        return color == NO_COLOR && special == null;
    }

    public boolean isSpecial() {
        return special != null;
    }

    public boolean isDyed() {
        return color != NO_COLOR;
    }

    public boolean isWritten() {
        return !text.isEmpty();
    }

    public NoteData copy() {
        return new NoteData(color, special, text, day, placer);
    }

    // ----- block entity NBT -----

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        writeItemNbt(tag);
        if (day != 0) tag.putLong(KEY_DAY, day);
        if (!placer.isEmpty()) tag.putString(KEY_PLACER, placer);
        return tag;
    }

    public static NoteData load(CompoundTag tag) {
        NoteData n = new NoteData();
        n.color = readColor(tag);
        n.text = readText(tag);
        n.special = readSpecial(tag);
        n.day = tag.getLong(KEY_DAY);
        n.placer = tag.getString(KEY_PLACER);
        return n;
    }

    // ----- items -----

    /** A sticky note item carrying this note's colour, special and text (not the day or placer). */
    public ItemStack toItem() {
        ItemStack stack = new ItemStack(CLRegistry.STICKY_NOTE.get());
        writeTo(stack);
        return stack;
    }

    /** Writes colour, special and text onto any stack (the stack's other NBT is kept). */
    public void writeTo(ItemStack stack) {
        writeColor(stack, color);
        writeSpecial(stack, special);
        writeText(stack, text);
    }

    public static NoteData fromItem(ItemStack stack, long day, String placer) {
        return new NoteData(readColor(stack), readSpecial(stack), readText(stack), day, placer);
    }

    // ----- item NBT helpers (work on any stack; the block entity uses the same compound shape) -----

    public static int readColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? NO_COLOR : readColor(tag);
    }

    public static int readColor(CompoundTag tag) {
        CompoundTag display = tag.getCompound(TAG_DISPLAY);
        return display.contains(KEY_COLOR, Tag.TAG_ANY_NUMERIC) ? display.getInt(KEY_COLOR) : NO_COLOR;
    }

    /** {@link #NO_COLOR} removes the colour (and an emptied display compound). */
    public static void writeColor(ItemStack stack, int color) {
        if (color == NO_COLOR) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(TAG_DISPLAY, Tag.TAG_COMPOUND)) {
                tag.getCompound(TAG_DISPLAY).remove(KEY_COLOR);
            }
        } else {
            stack.getOrCreateTagElement(TAG_DISPLAY).putInt(KEY_COLOR, color);
        }
        clean(stack);
    }

    public static String readText(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : readText(tag);
    }

    public static String readText(CompoundTag tag) {
        return tag.getCompound(TAG).getString(KEY_TEXT);
    }

    /** Empty or null text removes the key. */
    public static void writeText(ItemStack stack, @Nullable String text) {
        if (text == null || text.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(TAG, Tag.TAG_COMPOUND)) tag.getCompound(TAG).remove(KEY_TEXT);
        } else {
            stack.getOrCreateTagElement(TAG).putString(KEY_TEXT, text);
        }
        clean(stack);
    }

    @Nullable
    public static String readSpecial(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : readSpecial(tag);
    }

    @Nullable
    public static String readSpecial(CompoundTag tag) {
        return emptyToNull(tag.getCompound(TAG).getString(KEY_SPECIAL));
    }

    /** Null or empty removes the key. */
    public static void writeSpecial(ItemStack stack, @Nullable String special) {
        if (special == null || special.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(TAG, Tag.TAG_COMPOUND)) tag.getCompound(TAG).remove(KEY_SPECIAL);
        } else {
            stack.getOrCreateTagElement(TAG).putString(KEY_SPECIAL, special);
        }
        clean(stack);
    }

    public static boolean isWritten(ItemStack stack) {
        return !readText(stack).isEmpty();
    }

    /** Removes empty {@code CreativeLabels} / {@code display} compounds, then an empty tag, so clean stacks stack. */
    public static void clean(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        if (tag.contains(TAG, Tag.TAG_COMPOUND) && tag.getCompound(TAG).isEmpty()) tag.remove(TAG);
        if (tag.contains(TAG_DISPLAY, Tag.TAG_COMPOUND) && tag.getCompound(TAG_DISPLAY).isEmpty()) tag.remove(TAG_DISPLAY);
        if (tag.isEmpty()) stack.setTag(null);
    }

    private void writeItemNbt(CompoundTag tag) {
        if (color != NO_COLOR) {
            CompoundTag display = new CompoundTag();
            display.putInt(KEY_COLOR, color);
            tag.put(TAG_DISPLAY, display);
        }
        CompoundTag cl = new CompoundTag();
        if (!text.isEmpty()) cl.putString(KEY_TEXT, text);
        if (special != null) cl.putString(KEY_SPECIAL, special);
        if (!cl.isEmpty()) tag.put(TAG, cl);
    }

    @Nullable
    private static String emptyToNull(@Nullable String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteData n)) return false;
        return color == n.color && day == n.day && Objects.equals(special, n.special)
                && text.equals(n.text) && placer.equals(n.placer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, special, text, day, placer);
    }

    @Override
    public String toString() {
        return "NoteData{color=" + color + ", special=" + special + ", text='" + text + "', day=" + day
                + ", placer='" + placer + "'}";
    }
}
