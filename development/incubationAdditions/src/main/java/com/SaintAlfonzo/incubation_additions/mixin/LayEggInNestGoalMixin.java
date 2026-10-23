package com.SaintAlfonzo.incubation_additions.mixin;

import com.SaintAlfonzo.incubation_additions.goldenegg.GoldenEggLayer;
import com.teamabnormals.incubation.common.block.EmptyNestBlock;
import com.teamabnormals.incubation.common.entity.ai.goal.LayEggInNestGoal;
import com.teamabnormals.incubation.core.api.EggLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Incubation's LayEggInNestGoal accepts ANY empty nest as a target, then lays with
 * {@code emptyNest.getNest(egg).defaultBlockState()} - getNest is null when that nest has no
 * variant for the bird's egg, so the lay NPEs (server crash). Stock Incubation never hits it
 * (hay/twig nests map every egg it knows), but the emu nest (nest.EmuNests) maps only emu eggs
 * and hay/twig nests have no emu variant. Reject those empty nests up front in isValidTarget
 * (m_6465_; MoveToBlockGoal re-checks it every tick through canContinueToUse, so tick() never
 * reaches a foreign nest either).
 *
 * Also keeps a goose that won the golden egg roll (goldenegg.GoldenEggLayer) off nests: canEggBeLaid
 * (Incubation's own private method, unobfuscated) gates canUse, canContinueToUse and the lay in tick.
 */
@Mixin(value = LayEggInNestGoal.class, remap = false)
public abstract class LayEggInNestGoalMixin {

    @Shadow
    @Final
    private EggLayer eggLayer;

    @Inject(method = "m_6465_", at = @At("HEAD"), cancellable = true)
    private void incubation_additions$skipForeignEmptyNests(LevelReader level, BlockPos pos,
                                                          CallbackInfoReturnable<Boolean> cir) {
        Block block = level.getBlockState(pos.above()).getBlock();
        if (block instanceof EmptyNestBlock nest && nest.getNest(this.eggLayer.getEggItem()) == null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canEggBeLaid", at = @At("HEAD"), cancellable = true)
    private void incubation_additions$noNestForGoldenEgg(CallbackInfoReturnable<Boolean> cir) {
        if (this.eggLayer instanceof GoldenEggLayer bird && bird.incubation_additions$isGoldenEggDue()) {
            cir.setReturnValue(false);
        }
    }
}
