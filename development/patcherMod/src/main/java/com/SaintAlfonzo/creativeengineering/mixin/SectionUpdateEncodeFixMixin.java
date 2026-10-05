package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.util.BlockStateIdOverflow;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes ghost blocks on a dedicated server: vanilla 1.19.2 writes each entry of a multi-block update
 * as {@code Block.getId(state) << 12 | pos} in int arithmetic, which overflows to a negative number
 * for any state id of 2^19 or more. This pack has 761,052 states, so the last ~237,000 (4,700 blocks:
 * everycomp, quark, upgrade_aquatic, neapolitan, supplementaries, ...) were affected. The client read
 * them back as negative ids, found no state, and the NullPointerException dropped the rest of the
 * packet. Details and the limit of the repair: {@link BlockStateIdOverflow}.
 *
 * <p>Singleplayer never showed it because the integrated server's local connection hands packet
 * objects over without encoding them.
 *
 * <p>The only {@code writeVarLong} ({@code m_130103_}) in {@code write} ({@code m_5779_}) is the packed
 * entry, so redirecting it and masking the value to its low 32 bits writes the intended number. Ids
 * below 2^19 are unchanged. The client half is in {@link SectionUpdateNullStateMixin}.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap). Applies on both
 * sides (the integrated server is a server), harmless where nothing is written.
 */
@Mixin(value = ClientboundSectionBlocksUpdatePacket.class, remap = false)
public abstract class SectionUpdateEncodeFixMixin {

    @Redirect(method = "m_5779_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;m_130103_(J)Lnet/minecraft/network/FriendlyByteBuf;"))
    private FriendlyByteBuf creativeengineering$writePackedEntry(FriendlyByteBuf buf, long packed) {
        return buf.writeVarLong(BlockStateIdOverflow.unsignedPacked(packed));
    }
}
