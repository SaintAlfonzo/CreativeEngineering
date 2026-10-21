package com.SaintAlfonzo.creative_labels.net;

import com.SaintAlfonzo.creative_labels.CLConfig;
import com.SaintAlfonzo.creative_labels.CLRegistry;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlock;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creative_labels.block.NoteData;
import com.SaintAlfonzo.creative_labels.block.Spots;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * The note screen's Done, client to server (sticky_notes.md 3.7, C6). Two modes:
 * <ul>
 *   <li>{@link Mode#HAND}: the text goes on <b>one</b> note out of the stack in {@code hand} (the stack splits when
 *       it holds more); empty text clears that note</li>
 *   <li>{@link Mode#PLACED}: the note on {@code spot} of face {@code face} of the paper cell at {@code pos} is
 *       rewritten (last write wins)</li>
 * </ul>
 * The server trusts nothing: the text is cut to its first line, section-sign codes and disallowed characters are
 * stripped, it is capped at {@code noteLength}; the held item must be a note; a placed note must still be there
 * and within reach (8 blocks).
 */
public record NoteTextPacket(Mode mode, InteractionHand hand, BlockPos pos, Direction face, int spot, String text) {

    public enum Mode { HAND, PLACED }

    /** Vanilla's block interaction reach check (squared): the cell centre must be within 8 blocks. */
    public static final double REACH_SQ = 64.0;
    /** Longest raw text accepted off the wire before sanitising (the config cap is far lower). */
    public static final int WIRE_LIMIT = 1024;

    public static NoteTextPacket forHand(InteractionHand hand, String text) {
        return new NoteTextPacket(Mode.HAND, hand, BlockPos.ZERO, Direction.NORTH, 0, text);
    }

    public static NoteTextPacket forPlaced(BlockPos pos, Direction face, int spot, String text) {
        return new NoteTextPacket(Mode.PLACED, InteractionHand.MAIN_HAND, pos, face, spot, text);
    }

    // ------------------------------------------------------------------------------------------- wire

    public static void encode(NoteTextPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.mode);
        buf.writeEnum(msg.hand);
        buf.writeBlockPos(msg.pos);
        buf.writeEnum(msg.face);
        buf.writeVarInt(msg.spot);
        buf.writeUtf(msg.text, WIRE_LIMIT);
    }

    public static NoteTextPacket decode(FriendlyByteBuf buf) {
        Mode mode = buf.readEnum(Mode.class);
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        BlockPos pos = buf.readBlockPos();
        Direction face = buf.readEnum(Direction.class);
        int spot = buf.readVarInt();
        String text = buf.readUtf(WIRE_LIMIT);
        return new NoteTextPacket(mode, hand, pos, face, spot, text);
    }

    public static void handle(NoteTextPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) ctx.get().enqueueWork(() -> msg.apply(player));
        ctx.get().setPacketHandled(true);
    }

    // ------------------------------------------------------------------------------------------- server

    /** Applies a validated write for this player (main thread). Returns true when something changed. */
    public boolean apply(ServerPlayer player) {
        String clean = sanitize(text, CLConfig.integer(CLConfig.NOTE_LENGTH));
        return switch (mode) {
            case HAND -> applyHand(player, clean);
            case PLACED -> applyPlaced(player, clean);
        };
    }

    private boolean applyHand(ServerPlayer player, String clean) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof StickyNoteItem)) return false;
        if (NoteData.readText(stack).equals(clean)) return false;
        if (stack.getCount() <= 1) {
            NoteData.writeText(stack, clean);
        } else {
            ItemStack one = stack.copy();
            one.setCount(1);
            NoteData.writeText(one, clean);
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(one);
        }
        player.containerMenu.broadcastChanges();
        return true;
    }

    private boolean applyPlaced(ServerPlayer player, String clean) {
        if (spot < 0 || spot >= Spots.COUNT) return false;
        if (player.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQ) return false;
        if (!player.level.isLoaded(pos) || !player.level.getBlockState(pos).is(CLRegistry.FACE_PAPER.get())) return false;
        FacePaperBlockEntity be = FacePaperBlock.entity(player.level, pos);
        if (be == null) return false;
        NoteData note = be.face(face).note(spot);
        if (note == null) return false;
        if (note.text.equals(clean)) return false;
        note.text = clean;
        be.sync();
        return true;
    }

    // ------------------------------------------------------------------------------------------- pure

    /**
     * C6: the first line only, {@code §} plus its code character removed, every other character vanilla would not
     * allow in chat dropped, leading and trailing blanks trimmed, then capped at {@code max} characters.
     */
    public static String sanitize(String raw, int max) {
        if (raw == null) return "";
        int end = raw.length();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\n' || c == '\r') {
                end = i;
                break;
            }
        }
        StringBuilder out = new StringBuilder(Math.min(end, max + 8));
        for (int i = 0; i < end; i++) {
            char c = raw.charAt(i);
            if (c == '§') {
                i++; // the code character goes with it
                continue;
            }
            if (SharedConstants.isAllowedChatCharacter(c)) out.append(c);
        }
        String s = out.toString().strip();
        if (max >= 0 && s.length() > max) s = s.substring(0, max).strip();
        return s;
    }
}
