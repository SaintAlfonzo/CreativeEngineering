package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.OceanGeodes;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GeodeFeature#place (m_142674_): a geode under an ocean places lower (worldgen/OceanGeodes). Vanilla's amethyst
 * geode and every Geode Plus geode (all built on Feature.GEODE) come through here; the prismarine geode is skipped
 * by its inner layer ([worldgen.ocean_geodes] untouched).
 */
@Mixin(value = GeodeFeature.class, remap = false)
public abstract class GeodeFeatureOceanDepthMixin {

	@Inject(method = "m_142674_(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At("HEAD"), cancellable = true,
			require = 1, remap = false)
	private void creativeengineering$lowerUnderOcean(FeaturePlaceContext<GeodeConfiguration> ctx, CallbackInfoReturnable<Boolean> cir) {
		Boolean placed = OceanGeodes.placeLowered(ctx, ((GeodeFeature) (Object) this)::place);
		if (placed != null) cir.setReturnValue(placed);
	}
}
