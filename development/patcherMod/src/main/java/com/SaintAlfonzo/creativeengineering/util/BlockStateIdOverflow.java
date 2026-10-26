package com.SaintAlfonzo.creativeengineering.util;

/**
 * Arithmetic for the vanilla 1.19.2 multi-block update packet bug (see
 * {@code mixin/SectionUpdateEncodeFixMixin}).
 *
 * <p>{@code ClientboundSectionBlocksUpdatePacket#write} packs each entry as
 * {@code Block.getId(state) << 12 | position} in INT arithmetic and only then widens it to a long.
 * For a global state id of 2^19 (524,288) or more the shift reaches the sign bit, the int goes
 * negative, and widening sign-extends it. The reader takes {@code (int) (value >>> 12)}, which
 * comes out as {@code id - 2^20}: negative, so the state lookup returns null and the client throws
 * while applying the packet. Everything after the first such entry in that packet is lost.
 *
 * <p>The server repair does the shift in long ({@link #packed}), which the unchanged vanilla reader
 * decodes correctly for any int id, so there is no state-count ceiling with a patched server. The
 * client repair ({@link #repairId}) only covers an unpatched server, and only for ids below 2^20: above
 * that the int shift has already dropped bits.
 */
public final class BlockStateIdOverflow {

    /** Ids at or above this overflow the packet's int shift. */
    public static final int FIRST_OVERFLOWING_ID = 1 << 19;

    /** Ids at or above this lose bits in an unpatched server's int and cannot be recovered on the client. */
    public static final int UNRECOVERABLE_ID = 1 << 20;

    private BlockStateIdOverflow() {}

    /** Server side: one packet entry, shifted in long so no id overflows. */
    public static long packed(int stateId, short position) {
        return (long) stateId << 12 | position;
    }

    /** Client side: undo the overflow on an id read from a packet written by an unfixed server. */
    public static int repairId(int decodedId) {
        return decodedId < 0 ? decodedId + UNRECOVERABLE_ID : decodedId;
    }
}
