package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.seasons.SeasonTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.api.season.ISeasonColorProvider;
import sereneseasons.util.SeasonColorUtil;

/**
 * Arid and frozen biomes have no seasons, so Serene Seasons' grass and foliage tint leaves them alone
 * (before, deserts and badlands took the tropical tint and frozen biomes the winter one). See {@link SeasonTypes}.
 */
@Mixin(value = SeasonColorUtil.class, remap = false)
public class SeasonColorUtilMixin {

	@Inject(method = {"applySeasonalGrassColouring", "applySeasonalFoliageColouring"}, at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$yearRoundColour(ISeasonColorProvider provider, Holder<Biome> biome, int originalColour,
															CallbackInfoReturnable<Integer> cir) {
		if (SeasonTypes.yearRound(biome)) cir.setReturnValue(originalColour);
	}
}
