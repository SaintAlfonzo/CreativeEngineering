package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonAnswers;
import com.SaintAlfonzo.creativecalendar.seasons.SeasonAnswers.Question;
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
 *
 * All three answers (and "less colour change") are remembered per biome in {@link SeasonAnswers} until a tag or
 * config reload: the HEAD handler returns a remembered answer, the RETURN handler stores Serene Seasons' own one
 * (perf.md 19).
 */
@Mixin(value = BiomeConfig.class, remap = false)
public class SeasonBiomeConfigMixin {

	@Inject(method = "usesTropicalSeasons", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$taggedType(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		Boolean known = SeasonAnswers.get(Question.TROPICAL, biome);
		if (known != null) {
			cir.setReturnValue(known);
			return;
		}
		SeasonTypes.Type type = SeasonTypes.tagged(biome);
		if (type != null) {
			boolean answer = SeasonTypes.wetDryCycle(type);
			SeasonAnswers.put(Question.TROPICAL, biome, answer);
			cir.setReturnValue(answer);
		}
	}

	@Inject(method = "usesTropicalSeasons", at = @At("RETURN"), remap = false)
	private static void creativeengineering$rememberTropical(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		SeasonAnswers.put(Question.TROPICAL, biome, cir.getReturnValueZ());
	}

	@Inject(method = "enablesSeasonalEffects", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$taggedHasSeasons(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		Boolean known = SeasonAnswers.get(Question.SEASONAL, biome);
		if (known != null) {
			cir.setReturnValue(known);
			return;
		}
		if (SeasonTypes.listed(biome)) {
			SeasonAnswers.put(Question.SEASONAL, biome, true);
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "enablesSeasonalEffects", at = @At("RETURN"), remap = false)
	private static void creativeengineering$rememberHasSeasons(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		SeasonAnswers.put(Question.SEASONAL, biome, cir.getReturnValueZ());
	}

	@Inject(method = "lessColorChange", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$knownLessColour(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		Boolean known = SeasonAnswers.get(Question.LESS_COLOUR, biome);
		if (known != null) cir.setReturnValue(known);
	}

	@Inject(method = "lessColorChange", at = @At("RETURN"), remap = false)
	private static void creativeengineering$rememberLessColour(Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		SeasonAnswers.put(Question.LESS_COLOUR, biome, cir.getReturnValueZ());
	}
}
