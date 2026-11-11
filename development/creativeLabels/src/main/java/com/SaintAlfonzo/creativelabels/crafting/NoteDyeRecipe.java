package com.SaintAlfonzo.creativelabels.crafting;

import com.SaintAlfonzo.creativelabels.CLRegistry;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import com.SaintAlfonzo.creativelabels.item.NoteColours;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * {@code creativelabels:note_dye} (sticky_notes.md 3.6): any number of identical notes (same colour, special-free,
 * same text) + exactly one dye -> that many notes with the dye mixed in like leather, text kept. One note + several
 * dyes is vanilla's own armour-dye recipe (notes are {@code DyeableLeatherItem}s). Special notes don't take dye.
 */
public class NoteDyeRecipe extends CustomRecipe {

    public NoteDyeRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return matches(NoteGrid.of(grid));
    }

    static boolean matches(NoteGrid g) {
        if (g.notes.isEmpty() || g.dyes != 1 || g.specials != 0 || g.erasers != 0 || g.others != 0) return false;
        if (NoteData.readSpecial(g.notes.get(0)) != null) return false;
        return g.notesIdentical();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid) {
        NoteGrid g = NoteGrid.of(grid);
        if (!matches(g) || g.dye == null) return ItemStack.EMPTY;
        ItemStack out = NoteGrid.copies(g.notes.get(0), g.notes.size());
        NoteData.writeColor(out, NoteColours.mix(NoteData.readColor(out), g.dye.getDyeColor()));
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CLRegistry.NOTE_DYE.get();
    }
}
