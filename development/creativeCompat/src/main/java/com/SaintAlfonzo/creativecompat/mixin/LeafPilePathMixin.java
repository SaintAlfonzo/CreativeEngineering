package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.LeafThorns;
import com.SaintAlfonzo.creativecompat.surface.SnowLayers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mobs do not path through deep leaf piles (plan_post_scratch_1115.md G7.13, G7.14; {@link LeafThorns#blocksPath}):
 * HEAD of the static {@code getBlockPathTypeRaw} ({@code m_77643_}) gives a leaf pile of {@code pathBlockLayers} or
 * more {@code DAMAGE_OTHER}, as a sweet berry bush (the node above it too; {@code DAMAGE_CACTUS} for foxes and bees,
 * {@link LeafThorns#pathType}); a walk-through snow layer is {@code OPEN} while
 * the searching mob is sunk in snow (G11, {@link SnowLayers#openForPath}). MCA villagers: {@code McaLeafPilePathMixin}.
 * Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = WalkNodeEvaluator.class, remap = false)
public abstract class LeafPilePathMixin {

	@Inject(method = "m_77643_", at = @At("HEAD"), cancellable = true)
	private static void creativecompat$deepLeafPile(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir) {
		try {
			BlockState state = level.getBlockState(pos);
			if (LeafThorns.blocksPath(state)) cir.setReturnValue(LeafThorns.pathType());
			else if (SnowLayers.openForPath(state)) cir.setReturnValue(BlockPathTypes.OPEN);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("LeafPilePathMixin", t);
			// vanilla type
		}
	}
}
