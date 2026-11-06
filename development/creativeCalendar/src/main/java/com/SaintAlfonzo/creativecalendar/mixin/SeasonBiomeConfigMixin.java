package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.config.BiomeConfig;

/**
 * Serene Seasons' "is this biome tropical" answers from the pack's season tags first: tropical- or
 * dry-tagged yes, arid-, frozen- or temperate-tagged no (so deserts leave the wet/dry cycle). Untagged biomes
 * keep the mod's rule (config list, or warmer than 0.8). Every Serene Seasons consumer goes through here:
 * weather, temperature, grass colours, crop fertility, SeasonHUD.
 *
 * "Does this biome have seasons" answers yes for every tagged biome, so the season tags win over the
 * blacklist (Serene Seasons' default list names the oceans and minecraft:river). See {@link SeasonTypes}.
 */
@Mixin(value = BiomeConfig.class, remap = false)
public class SeasonBiomeConfigMixin {

	@Inject(method = "usesTropicalSeasons", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$taggedType(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		SeasonTypes.Type type = SeasonTypes.tagged(biome);
		if (type != null) cir.setReturnValue(SeasonTypes.wetDryCycle(type));
	}

	@Inject(method = "enablesSeasonalEffects", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$taggedHasSeasons(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		if (SeasonTypes.listed(biome)) cir.setReturnValue(true);
	}
}
