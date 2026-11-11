package com.SaintAlfonzo.creativelabels.item;

import com.SaintAlfonzo.creativelabels.block.NoteData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Text labels on items (sticky_notes.md 3.8, C5): up to {@link #MAX_LINES} lines under
 * {@code CreativeLabels.Lines} (a list of strings). Written only when a line exists; removing the last line removes
 * the key, the compound if it is then empty, and the tag if that is then empty.
 *
 * <p>Also the shared labelling action of the three ways (inventory cursor, anvil, off hand): {@link #labelWith}
 * puts a written note's text on a whole stack and uses the note up (one per line), {@link #refuse} is the "no room"
 * answer, and {@link #onTooltip} shows a labelled item's lines in grey under its name.
 */
public final class ItemLabels {

    public static final String KEY_LINES = "Lines";
    public static final int MAX_LINES = 3;
    public static final String FULL_KEY = "creativelabels.label.full";

    private ItemLabels() {}

    public static boolean has(ItemStack stack) {
        return !read(stack).isEmpty();
    }

    /** The lines, in order (empty list when none); a copy. */
    public static List<String> read(ItemStack stack) {
        List<String> out = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NoteData.TAG, Tag.TAG_COMPOUND)) return out;
        ListTag list = tag.getCompound(NoteData.TAG).getList(KEY_LINES, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String s = list.getString(i);
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    /** Replaces the lines (empty lines dropped, at most {@link #MAX_LINES}); an empty list removes the key. */
    public static void write(ItemStack stack, List<String> lines) {
        ListTag list = new ListTag();
        for (String s : lines) {
            if (s == null || s.isEmpty()) continue;
            list.add(StringTag.valueOf(s));
            if (list.size() >= MAX_LINES) break;
        }
        if (list.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(NoteData.TAG, Tag.TAG_COMPOUND)) tag.getCompound(NoteData.TAG).remove(KEY_LINES);
        } else {
            stack.getOrCreateTagElement(NoteData.TAG).put(KEY_LINES, list);
        }
        NoteData.clean(stack);
    }

    /** Adds a line; false (nothing changed) when full or the line is empty. */
    public static boolean add(ItemStack stack, String line) {
        if (line == null || line.isEmpty()) return false;
        List<String> lines = read(stack);
        if (lines.size() >= MAX_LINES) return false;
        lines.add(line);
        write(stack, lines);
        return true;
    }

    /** Removes the last line; false when there was none. */
    public static boolean removeLast(ItemStack stack) {
        List<String> lines = read(stack);
        if (lines.isEmpty()) return false;
        lines.remove(lines.size() - 1);
        write(stack, lines);
        return true;
    }

    public static boolean isFull(ItemStack stack) {
        return read(stack).size() >= MAX_LINES;
    }

    // ------------------------------------------------------------------------------------------- the action

    /** A stack a written note can label: something, and not a note (notes are written through the screen). */
    public static boolean canLabel(ItemStack note, ItemStack target) {
        return !target.isEmpty() && !(target.getItem() instanceof StickyNoteItem) && NoteData.isWritten(note);
    }

    /**
     * Puts {@code note}'s text on the whole {@code target} stack and uses one note up (creative too, 4.1). A full
     * target is refused with the bass note and the action-bar line, nothing used. Runs on whichever side calls it;
     * the sound goes through {@link Player#playSound} so each side plays it once. Returns true when a line was added.
     */
    public static boolean labelWith(Player player, ItemStack note, ItemStack target) {
        if (!canLabel(note, target)) return false;
        if (isFull(target)) {
            refuse(player);
            return false;
        }
        add(target, NoteData.readText(note));
        note.shrink(1); // 4.1 / Q3: labelling always uses up the note, creative included
        player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.8f, 1.0f);
        return true;
    }

    /** "No room for another label": bass note (both sides) and the action bar line (server). */
    public static void refuse(Player player) {
        player.playSound(SoundEvents.NOTE_BLOCK_BASS, 1.0f, 0.6f);
        if (!player.level.isClientSide) player.displayClientMessage(Component.translatable(FULL_KEY), true);
    }

    /** The eraser on a labelled stack: the last line goes. Returns true when a line was removed. */
    public static boolean eraseLast(Player player, ItemStack target) {
        if (!removeLast(target)) return false;
        player.playSound(SoundEvents.WOOL_HIT, 0.8f, 1.0f);
        return true;
    }

    // ------------------------------------------------------------------------------------------- tooltip

    /** A labelled item's lines in grey right under its name (3.8: no header, no [Shift]). Forge bus. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        List<String> lines = read(event.getItemStack());
        if (lines.isEmpty()) return;
        List<Component> tooltip = event.getToolTip();
        int at = Math.min(1, tooltip.size());
        for (String line : lines) {
            tooltip.add(at++, Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
    }
}
