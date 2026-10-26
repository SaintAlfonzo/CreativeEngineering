package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.util.BlockStateIdOverflow;

import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes ghost blocks on a dedicated server: vanilla 1.19.2 writes each entry of a multi-block update
 * as {@code Block.getId(state) << 12 | pos} in int arithmetic, which overflows to a negative number
 * for any state id of 2^19 or more. This pack has 761,052 states, so the last ~237,000 (4,700 blocks:
 * everycomp, quark, upgrade_aquatic, neapolitan, supplementaries, ...) were affected. The client read
 * them back as negative ids, found no state, and the NullPointerException dropped the rest of the
 * packet. Details: {@link BlockStateIdOverflow}.
 *
 * <p>Singleplayer never showed it because the integrated server's local connection hands packet
 * objects over without encoding them.
 *
 * <p>{@code write} ({@code m_5779_}) is replaced with the same layout (section pos, light flag, count,
 * one VarLong per entry) but each entry shifted in long, so any id round-trips through the vanilla
 * reader. Ids below 2^19 give the same bytes as vanilla. (The first version masked vanilla's value to
 * 32 bits, which stopped working at 2^20 states.) The client half is in
 * {@link SectionUpdateNullStateMixin}.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap). Applies on both
 * sides (the integrated server is a server), harmless where nothing is written. Fields are only read,
 * so a packet written to several connections at once is safe.
 */
@Mixin(value = ClientboundSectionBlocksUpdatePacket.class, remap = false)
public abstract class SectionUpdateEncodeFixMixin {

    @Shadow @Final private SectionPos f_132980_;          // sectionPos
    @Shadow @Final private short[] f_132981_;             // positions
    @Shadow @Final private BlockState[] f_132982_;        // states
    @Shadow @Final private boolean f_132983_;             // suppressLightUpdates

    @Inject(method = "m_5779_", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$writeLongShift(FriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeLong(f_132980_.asLong());
        buf.writeBoolean(f_132983_);
        buf.writeVarInt(f_132981_.length);
        for (int i = 0; i < f_132981_.length; i++) {
            buf.writeVarLong(BlockStateIdOverflow.packed(Block.getId(f_132982_[i]), f_132981_[i]));
        }
        ci.cancel();
    }
}
