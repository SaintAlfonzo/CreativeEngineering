package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.BeeNests;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bee nest tuning (see worldgen/BeeNests): exposes the decorator's private probability (f_69955_) to the rules,
 * and counts a nest each time BeehiveDecorator#place (m_214187_) sets one (Context#setBlock, m_226061_; it is
 * only called once a free spot was found).
 */
@Mixin(value = BeehiveDecorator.class, remap = false)
public abstract class BeehiveDecoratorBeeNestsMixin implements BeeNests.ProbabilityHolder {

	@Shadow(remap = false)
	@Final
	private float f_69955_;

	@Override
	public float creativeengineering$probability() {
		return f_69955_;
	}

	@Inject(method = "m_214187_(Lnet/minecraft/world/level/levelgen/feature/treedecorators/TreeDecorator$Context;)V",
			require = 1, remap = false, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/feature/treedecorators/TreeDecorator$Context;m_226061_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
	private void creativeengineering$countNest(TreeDecorator.Context context, CallbackInfo ci) {
		BeeNests.nestPlaced();
	}
}
