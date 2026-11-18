package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.LeafThorns;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mobs do not path through deep leaf piles (plan_post_scratch_1115.md G7.13, G7.14; {@link LeafThorns#blocksPath}):
 * HEAD of the static {@code getBlockPathTypeRaw} ({@code m_77643_}) gives a leaf pile of {@code pathBlockLayers} or
 * more {@code DAMAGE_OTHER}, as a sweet berry bush (the node above it too). MCA villagers: {@code McaLeafPilePathMixin}.
 * Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = WalkNodeEvaluator.class, remap = false)
public abstract class LeafPilePathMixin {

	@Inject(method = "m_77643_", at = @At("HEAD"), cancellable = true)
	private static void creativecompat$deepLeafPile(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir) {
		try {
			if (LeafThorns.blocksPath(level.getBlockState(pos))) cir.setReturnValue(BlockPathTypes.DAMAGE_OTHER);
		} catch (Throwable ignored) {
			// vanilla type
		}
	}
}
