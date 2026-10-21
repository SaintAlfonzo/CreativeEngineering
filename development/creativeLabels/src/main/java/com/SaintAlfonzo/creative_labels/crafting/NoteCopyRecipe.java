package com.SaintAlfonzo.creative_labels.crafting;

import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * {@code creative_labels:note_copy} (sticky_notes.md 3.6): one written note + exactly eight blank notes (any colour
 * or special) -> nine copies of the written one (its text, colour and special look).
 */
public class NoteCopyRecipe extends CustomRecipe {

    public static final int BLANKS = 8;

    public NoteCopyRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return written(NoteGrid.of(grid)) != null;
    }

    /** The single written note of a matching grid, or null when the grid doesn't match. */
    @Nullable
    static ItemStack written(NoteGrid g) {
        if (g.notes.size() != BLANKS + 1 || g.dyes != 0 || g.specials != 0 || g.erasers != 0 || g.others != 0) return null;
        ItemStack written = null;
        for (ItemStack n : g.notes) {
            if (!NoteData.isWritten(n)) continue;
            if (written != null) return null;
            written = n;
        }
        return written;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid) {
        ItemStack written = written(NoteGrid.of(grid));
        return written == null ? ItemStack.EMPTY : NoteGrid.copies(written, BLANKS + 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= BLANKS + 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CLRegistry.NOTE_COPY.get();
    }
}
