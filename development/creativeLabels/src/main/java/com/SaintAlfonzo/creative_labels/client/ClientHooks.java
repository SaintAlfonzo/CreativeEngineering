package com.SaintAlfonzo.creative_labels.client;

import com.SaintAlfonzo.creative_labels.block.FacePaperBlock;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creative_labels.block.FacePaperInteractions;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side entry points the common code reaches through {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)}
 * (C4: this class is never loaded on a dedicated server, so it may reference Minecraft and the screens).
 */
public final class ClientHooks {

    private ClientHooks() {}

    /** Empty-hand right-click on a placed note (3.4): the note screen for the note on {@code spot} of {@code face}. */
    public static void openNoteScreen(BlockPos pos, Direction face, int spot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        FacePaperBlockEntity be = FacePaperBlock.entity(mc.level, pos);
        if (be == null) return;
        NoteData note = be.face(face).note(spot);
        if (note == null) return;
        mc.setScreen(NoteScreen.forPlaced(pos, face, spot, note));
    }

    /** Right-click in the air with a note (3.7): the note screen for one note out of the stack in {@code hand}. */
    public static void openHandNoteScreen(InteractionHand hand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ItemStack note = mc.player.getItemInHand(hand);
        if (!(note.getItem() instanceof StickyNoteItem)) return;
        mc.setScreen(NoteScreen.forHand(hand, note, FacePaperInteractions.day(mc.level), mc.player.getGameProfile().getName()));
    }
}
