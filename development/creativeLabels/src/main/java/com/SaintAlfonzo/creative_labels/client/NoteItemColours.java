package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.CreativeLabels;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.item.NoteColours;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

/**
 * Note item icons (sticky_notes.md 3.6, BRIEF 4): the {@code creative_labels:look} item property picks the model
 * override (0 plain donor icon, 1 the greyscale icon tinted by the colour handler, 2..5 the recoloured special
 * icons); the {@link net.minecraft.client.color.item.ItemColor} tints layer 0 with {@code display.color} only when
 * the note is dyed and not special (plain and special icons stay untinted).
 */
public final class NoteItemColours {

    public static final ResourceLocation LOOK = new ResourceLocation(CreativeLabels.ID, "look");

    private NoteItemColours() {}

    public static void onItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tint(stack, tintIndex), CLRegistry.STICKY_NOTE.get());
    }

    /** White unless layer 0 of a dyed, non-special note. */
    public static int tint(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return ClientSetup.WHITE;
        int color = NoteData.readColor(stack);
        if (color == NoteData.NO_COLOR || NoteData.readSpecial(stack) != null) return ClientSetup.WHITE;
        return color;
    }

    /** {@code ItemProperties.register} is not thread-safe: called from client setup's enqueueWork. */
    @SuppressWarnings("deprecation")
    public static void registerProperties() {
        ItemProperties.register(CLRegistry.STICKY_NOTE.get(), LOOK,
                (stack, level, entity, seed) -> NoteColours.look(NoteData.readColor(stack), NoteData.readSpecial(stack)));
    }
}
