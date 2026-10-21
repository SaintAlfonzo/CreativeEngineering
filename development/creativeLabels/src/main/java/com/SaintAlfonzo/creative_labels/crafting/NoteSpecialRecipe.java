package com.SaintAlfonzo.creative_labels.crafting;

import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * {@code creative_labels:note_special} (sticky_notes.md 3.6): any number of plain notes (no colour, no special; text
 * allowed and kept) + exactly one special ingredient (ink sac, glow berries, gunpowder, gold nugget) -> that many
 * notes with the matching look.
 */
public class NoteSpecialRecipe extends CustomRecipe {

    public NoteSpecialRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return matches(NoteGrid.of(grid));
    }

    static boolean matches(NoteGrid g) {
        if (g.notes.isEmpty() || g.specials != 1 || g.dyes != 0 || g.erasers != 0 || g.others != 0) return false;
        for (ItemStack n : g.notes) {
            if (NoteData.readColor(n) != NoteData.NO_COLOR || NoteData.readSpecial(n) != null) return false;
        }
        return g.notesIdentical();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid) {
        NoteGrid g = NoteGrid.of(grid);
        if (!matches(g) || g.special == null) return ItemStack.EMPTY;
        ItemStack out = NoteGrid.copies(g.notes.get(0), g.notes.size());
        NoteData.writeSpecial(out, g.special.name());
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CLRegistry.NOTE_SPECIAL.get();
    }
}
