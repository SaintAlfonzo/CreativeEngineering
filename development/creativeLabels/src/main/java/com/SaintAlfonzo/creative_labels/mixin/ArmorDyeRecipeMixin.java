package com.SaintAlfonzo.creative_labels.mixin;

import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ArmorDyeRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Special notes are not dyeable (sticky_notes.md 3.6). {@code NoteDyeRecipe} refuses them, but vanilla's
 * armour-dye recipe matches any {@link net.minecraft.world.item.DyeableLeatherItem} plus dyes, and the sticky note
 * is one so plain notes take {@code display.color}. This keeps that recipe off a grid holding a special note; the
 * grid then falls through to no result instead of storing a hidden colour on the special.
 */
@Mixin(ArmorDyeRecipe.class)
public abstract class ArmorDyeRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"), cancellable = true)
    private void creative_labels$refuseSpecialNotes(CraftingContainer grid, Level level, CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.getItem() instanceof StickyNoteItem && NoteData.readSpecial(s) != null) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
