package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.BlockStateLookupHook;
import com.SaintAlfonzo.creativecommon.util.BlockStateIdOverflow;

import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client half of the fix for vanilla's overflowing state ids in multi-block update packets (the server
 * half is {@link SectionUpdateEncodeFixMixin}; the arithmetic is in {@link BlockStateIdOverflow}). An
 * unfixed server sends ids of 2^19 and up as negative numbers; the redirect on {@code IdMapper#byId}
 * ({@code m_7942_}) adds 2^20 back first, so the client also works against a server without the patch.
 *
 * <p>It also feeds the debug tool through {@link BlockStateLookupHook} (Creative Devtools, {@code [debug.block_state_sync]}):
 * remember the raw id of every state the client still cannot resolve, then report them with their
 * block positions once the packet is built. Nothing is logged while the switch is off.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} (no refmap). Client-only
 * (listed under "client" in the mixin config); the packet class exists on the server but decoding
 * these packets only matters on the receiving client.
 */
@Mixin(value = ClientboundSectionBlocksUpdatePacket.class, remap = false)
public abstract class SectionUpdateNullStateMixin {

    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;m_7942_(I)Ljava/lang/Object;"))
    private Object creativecommon$trackLookup(IdMapper<?> map, int id) {
        return BlockStateLookupHook.lookup(map, BlockStateIdOverflow.repairId(id));
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
    private void creativecommon$reportNulls(FriendlyByteBuf buf, CallbackInfo ci) {
        BlockStateLookupHook.finishSection((ClientboundSectionBlocksUpdatePacket) (Object) this);
    }
}
