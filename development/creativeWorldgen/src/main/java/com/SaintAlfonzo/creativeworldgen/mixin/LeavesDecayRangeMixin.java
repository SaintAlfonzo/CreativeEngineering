package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.SaintAlfonzo.creativeworldgen.block.LeafDecayRange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code LeavesBlock.randomTick} (m_213898_) is where a decaying leaf (m_221385_: not persistent, distance 7) is
 * dropped; the pack's fruit leaves (Fruits Delight, Vinery) call super. {@code LeavesBlock.tick} (m_213897_) is the
 * scheduled tick a neighbour change (or {@code block.FastLeafDecayPort}) queues; it stores the recomputed distance,
 * and {@link LeafDecayRange#afterTick} decides at its end. With CWGConfig [leaf_decay] onlyWhenDisturbed the
 * random-tick decay is cancelled; without it the random-tick decay only goes ahead past maxDistance
 * ({@link LeafDecayRange#keep}).
 */
@Mixin(value = LeavesBlock.class, remap = false)
public abstract class LeavesDecayRangeMixin {

    @Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$gateRandomDecay(BlockState state, ServerLevel level, BlockPos pos,
                                                     RandomSource random, CallbackInfo ci) {
        // Vanilla decaying() inlined: @Shadow of MC members has no refmap in this build.
        if (LeafDecayRange.decaying(state) && LeafDecayRange.cancelRandomDecay(level, pos, state)) {
            ci.cancel();
        }
    }

    @Inject(method = "m_213897_", at = @At("TAIL"), remap = false)
    private void creativeengineering$decayOnUpdate(BlockState state, ServerLevel level, BlockPos pos,
                                                   RandomSource random, CallbackInfo ci) {
        LeafDecayRange.afterTick(level, pos, state, random);
    }
}
