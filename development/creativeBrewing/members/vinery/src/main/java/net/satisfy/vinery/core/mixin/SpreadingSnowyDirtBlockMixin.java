package net.satisfy.vinery.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.block.SpreadableGrassSlabBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Grass spreads onto dirt slabs too. Port: the spread position from the loop's block lookup, no local capture. */
@Mixin(SpreadingSnowyDirtBlock.class)
public class SpreadingSnowyDirtBlockMixin {
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
    private BlockState vinery$spreadToSlabs(ServerLevel level, BlockPos spreadPos, Operation<BlockState> original) {
        SpreadableGrassSlabBlock.trySpread(level, spreadPos);
        return original.call(level, spreadPos);
    }
}
