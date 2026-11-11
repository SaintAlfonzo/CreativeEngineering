package com.SaintAlfonzo.creativelabels.item;

import com.SaintAlfonzo.creativelabels.CLConfig;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Labels through the anvil (sticky_notes.md 3.8): the item on the left, a written note on the right -> the item with
 * the note's text as a new line (the whole left stack, like a vanilla rename), at {@code anvilCost} levels (vanilla refuses to hand out an output of cost 0) and
 * one note used. A full item (three lines) gets no output. A name typed into the anvil's field is applied too, as
 * vanilla would.
 */
public final class AnvilLabels {

    private AnvilLabels() {}

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack note = event.getRight();
        if (!(note.getItem() instanceof StickyNoteItem) || !ItemLabels.canLabel(note, left)) return;
        if (ItemLabels.isFull(left)) {
            event.setOutput(ItemStack.EMPTY);
            event.setCost(0);
            event.setMaterialCost(0);
            return;
        }
        ItemStack out = left.copy(); // full count: AnvilMenu.onTake clears the whole left slot, as vanilla's rename does
        ItemLabels.add(out, NoteData.readText(note));
        String name = event.getName();
        if (name != null && !name.isBlank() && !name.equals(left.getHoverName().getString())) {
            out.setHoverName(Component.literal(name));
        }
        event.setOutput(out);
        event.setCost(Math.max(0, CLConfig.integer(CLConfig.ANVIL_COST)));
        event.setMaterialCost(1);
    }
}
