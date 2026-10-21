package com.SaintAlfonzo.creative_labels.crafting;

import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.block.FacePaperInteractions;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * {@code creative_labels:note_erase} (sticky_notes.md 3.6): one note + the eraser -> the note with its text wiped,
 * or, when it was blank, with its colour and special look removed (back to plain yellow). The eraser stays in the
 * grid as its own crafting remainder. A plain blank note has nothing to erase and doesn't match.
 */
public class NoteEraseRecipe extends CustomRecipe {

    public NoteEraseRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return matches(NoteGrid.of(grid));
    }

    static boolean matches(NoteGrid g) {
        if (g.notes.size() != 1 || g.erasers != 1 || g.dyes != 0 || g.specials != 0 || g.others != 0) return false;
        NoteData n = NoteData.fromItem(g.notes.get(0), 0, "");
        return n.isWritten() || !n.isPlain();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid) {
        NoteGrid g = NoteGrid.of(grid);
        if (!matches(g)) return ItemStack.EMPTY;
        ItemStack out = NoteGrid.copies(g.notes.get(0), 1);
        NoteData n = NoteData.fromItem(out, 0, "");
        FacePaperInteractions.erase(n);
        n.writeTo(out);
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CLRegistry.NOTE_ERASE.get();
    }
}
