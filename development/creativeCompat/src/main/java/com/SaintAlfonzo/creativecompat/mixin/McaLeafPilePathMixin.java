package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.LeafThorns;

import forge.net.mca.entity.ai.pathfinder.ExtendedPathNodeType;
import forge.net.mca.entity.ai.pathfinder.VillagerLandPathNodeMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCA villagers do not path through deep leaf piles either (plan_post_scratch_1115.md G7.14; agent note
 * mca-villager-pathing): HEAD of MCA's static {@code getCommonNodeType}, the twin of {@code LeafPilePathMixin}.
 * {@code remap = false}; applied only with MCA loaded.
 */
@Mixin(value = VillagerLandPathNodeMaker.class, remap = false)
public abstract class McaLeafPilePathMixin {

	@Inject(method = "getCommonNodeType", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativecompat$deepLeafPile(BlockGetter world, BlockPos pos,
												   CallbackInfoReturnable<ExtendedPathNodeType> cir) {
		try {
			if (LeafThorns.blocksPath(world.getBlockState(pos))) cir.setReturnValue(ExtendedPathNodeType.DAMAGE_OTHER);
		} catch (Throwable ignored) {
			// MCA's type
		}
	}
}
