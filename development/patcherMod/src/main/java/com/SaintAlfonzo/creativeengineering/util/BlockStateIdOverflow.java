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
 * <p>Nothing is lost on the wire as long as the id is below 2^20 (1,048,576): the low 32 bits of the
 * long are exactly {@code id << 12 | position}. Both repairs below rely on that, so a pack with 2^20
 * or more block states cannot be repaired this way (it would need the shift done in long).
 */
public final class BlockStateIdOverflow {

    /** Ids at or above this overflow the packet's int shift. */
    public static final int FIRST_OVERFLOWING_ID = 1 << 19;

    /** Ids at or above this lose bits in the int itself and cannot be recovered. */
    public static final int UNRECOVERABLE_ID = 1 << 20;

    private BlockStateIdOverflow() {}

    /** Server side: undo the sign extension before the packed value is written. */
    public static long unsignedPacked(long packed) {
        return packed & 0xFFFFFFFFL;
    }

    /** Client side: undo the overflow on an id read from a packet written by an unfixed server. */
    public static int repairId(int decodedId) {
        return decodedId < 0 ? decodedId + UNRECOVERABLE_ID : decodedId;
    }
}
