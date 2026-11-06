package com.SaintAlfonzo.creativeworldgen.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla villagers (MCA overwrites them, so only ones it has not converted) path with WalkNodeEvaluator, where an open
 * gate is OPEN and walkable. {@code getBlockPathType(BlockGetter, x, y, z)} (m_8086_) is the per-block lookup
 * getBlockPathTypes runs over the mob's box; a gate there is FENCE for a villager, open or closed. Other mobs keep
 * vanilla. Dramatic Doors already handles its doors here. MCA villagers: {@code McaVillagerPathGatesMixin}.
 */
@Mixin(value = WalkNodeEvaluator.class, remap = false)
public abstract class WalkNodeEvaluatorVillagerGateMixin extends NodeEvaluator {

    @Inject(method = "m_8086_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$gatesAreFences(BlockGetter level, int x, int y, int z,
                                                    CallbackInfoReturnable<BlockPathTypes> cir) {
        if (!(this.mob instanceof Villager)) return;
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        if (state.is(BlockTags.FENCE_GATES) || state.getBlock() instanceof FenceGateBlock) {
            cir.setReturnValue(BlockPathTypes.FENCE);
        }
    }
}
