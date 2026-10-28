package com.SaintAlfonzo.creativeengineering.mixin;

import com.sk89q.worldedit.forge.internal.ForgeTransmogrifier;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.world.block.BlockState;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lazy WorldEdit block state ids, taken from vanilla's {@code Block.BLOCK_STATE_REGISTRY}.
 *
 * <p>With the startup build skipped ({@link WorldEditSkipStateIdInitMixin}), a WorldEdit state has no id until
 * asked for one. {@code getBlockStateId} then maps it to the vanilla state, takes {@code Block.getId} (the
 * same id the startup build would have stored) and records the pair. {@code getBlockStateById} reads that
 * record. WorldEdit's own {@code TO_STATE} is a plain fastutil map and edits run off the server thread, so
 * the record is a {@link ConcurrentHashMap}; the per-state id field is already volatile.
 *
 * <p>An id-to-state miss returns null, which every caller already handles: {@code ForgeAdapter.adapt} falls
 * back to {@code ForgeTransmogrifier}, and {@code Int2BaseBlockMap} only looks up ids it got from
 * {@code getBlockStateId}, so those are always recorded.
 */
@Mixin(value = BlockStateIdAccess.class, remap = false)
public abstract class WorldEditBlockStateIdAccessMixin {

    @Shadow private static BlockStateIdAccess.BlockStateInternalId blockStateInternalId;

    @Unique
    private static final ConcurrentHashMap<Integer, BlockState> creativeengineering$lazyStates = new ConcurrentHashMap<>();

    @Inject(method = "getBlockStateId", at = @At("RETURN"), cancellable = true, remap = false)
    private static void creativeengineering$lazyId(BlockState holder, CallbackInfoReturnable<Integer> cir) {
        if (BlockStateIdAccess.isValidInternalId(cir.getReturnValueI())) return;
        // ForgeTransmogrifier, not ForgeAdapter.adapt: adapt asks getBlockStateId first and would loop.
        int id = Block.getId(ForgeTransmogrifier.transmogToMinecraft(holder));
        if (!BlockStateIdAccess.isValidInternalId(id)) return;
        creativeengineering$lazyStates.putIfAbsent(id, holder);
        blockStateInternalId.setInternalId(holder, id);
        cir.setReturnValue(id);
    }

    @Inject(method = "getBlockStateById", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$lazyState(int id, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = creativeengineering$lazyStates.get(id);
        if (state != null) cir.setReturnValue(state);
    }

    @Inject(method = "clear", at = @At("HEAD"), remap = false)
    private static void creativeengineering$clearLazy(CallbackInfo ci) {
        for (BlockState state : creativeengineering$lazyStates.values()) {
            blockStateInternalId.setInternalId(state, BlockStateIdAccess.invalidId());
        }
        creativeengineering$lazyStates.clear();
    }
}
