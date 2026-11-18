package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.LeafThorns;
import com.SaintAlfonzo.creativecompat.surface.SnowLayers;

import forge.net.mca.entity.ai.pathfinder.ExtendedPathNodeType;
import forge.net.mca.entity.ai.pathfinder.VillagerLandPathNodeMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCA villagers do not path through deep leaf piles either (plan_post_scratch_1115.md G7.14; agent note
 * mca-villager-pathing): HEAD of MCA's static {@code getCommonNodeType}, the twin of {@code LeafPilePathMixin} (deep leaf
 * piles, and snow layers for a sunk villager, G11).
 * {@code remap = false}; applied only with MCA loaded.
 */
@Mixin(value = VillagerLandPathNodeMaker.class, remap = false)
public abstract class McaLeafPilePathMixin {

	@Inject(method = "getCommonNodeType", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativecompat$deepLeafPile(BlockGetter world, BlockPos pos,
												   CallbackInfoReturnable<ExtendedPathNodeType> cir) {
		try {
			BlockState state = world.getBlockState(pos);
			if (LeafThorns.blocksPath(state)) cir.setReturnValue(ExtendedPathNodeType.DAMAGE_OTHER);
			else if (SnowLayers.openForPath(state)) cir.setReturnValue(ExtendedPathNodeType.OPEN);
		} catch (Throwable ignored) {
			// MCA's type
		}
	}
}
