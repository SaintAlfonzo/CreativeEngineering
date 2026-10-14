package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.BeeNests;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Per-biome bee nest chances on generated trees (see worldgen/BeeNests).
 *
 * TreeFeature#place (m_142674_) reads config.decorators (f_68187_) twice once the tree is placed: isEmpty(), then
 * forEach(place). Both reads get the tuned list, so a tree without decorators that gains a beehive still gets its
 * decorator context. HEAD records the biome at the origin, RETURN counts the tree.
 */
@Mixin(value = TreeFeature.class, remap = false)
public class TreeFeatureBeeNestsMixin {

	private static final String PLACE = "m_142674_(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z";

	@Inject(method = PLACE, at = @At("HEAD"), require = 1, remap = false)
	private void creativeengineering$beginTree(FeaturePlaceContext<TreeConfiguration> ctx, CallbackInfoReturnable<Boolean> cir) {
		BeeNests.beginTree(ctx.level(), ctx.origin());
	}

	@Redirect(method = PLACE, require = 2, remap = false, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;f_68187_:Ljava/util/List;"))
	private List<TreeDecorator> creativeengineering$decorators(TreeConfiguration config) {
		return BeeNests.decorators(config.decorators);
	}

	@Inject(method = PLACE, at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$endTree(FeaturePlaceContext<TreeConfiguration> ctx, CallbackInfoReturnable<Boolean> cir) {
		BeeNests.endTree(cir.getReturnValueZ());
	}
}
