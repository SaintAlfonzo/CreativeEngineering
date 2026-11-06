package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.BlockStateLookupHook;

import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Single-block twin of {@link SectionUpdateNullStateMixin}. The packet reads its state with
 * {@code FriendlyByteBuf#readById(IdMap)} ({@code m_236816_}), which is just {@code map.byId(readVarInt())}
 * ({@code m_130242_} = readVarInt), so the redirect does the same two steps itself to get hold of the id.
 */
@Mixin(value = ClientboundBlockUpdatePacket.class, remap = false)
public abstract class BlockUpdateNullStateMixin {

    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;m_236816_(Lnet/minecraft/core/IdMap;)Ljava/lang/Object;"))
    private Object creativecommon$trackLookup(FriendlyByteBuf buf, IdMap<?> map) {
        return BlockStateLookupHook.lookup(map, buf.readVarInt());
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
    private void creativecommon$reportNulls(FriendlyByteBuf buf, CallbackInfo ci) {
        BlockStateLookupHook.finishSingle((ClientboundBlockUpdatePacket) (Object) this);
    }
}
