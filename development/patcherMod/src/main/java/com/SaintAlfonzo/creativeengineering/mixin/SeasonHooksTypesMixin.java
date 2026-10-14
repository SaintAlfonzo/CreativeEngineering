package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.seasons.SeasonTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.season.SeasonHooks;

/**
 * The season types in Serene Seasons' weather and temperature code (both sides). See {@link SeasonTypes}.
 *
 * - getBiomeTemperature(Level, ...): every seasonal temperature read goes through it (client rain/snow per
 *   column, snow layers, ice, melting, snow golems), so a river or beach is swapped for its land here, and
 *   the result passes through {@link SeasonTypes#applySnowRules} (snow line, per-biome snow seasons).
 * - isRainingAtHook (Level.isRainingAt: fire, cauldrons, endermen, riptide, lightning): same swap.
 * - shouldRainInBiomeInSeason: arid never; dry (savannas) in Mid Wet only, whatever the biome declares
 *   (Serene Seasons' own result for a savanna, now also for dry biomes that declare rain). Tropical and
 *   untagged biomes keep the mod's rule.
 * - getBiomeTemperatureInSeason: arid, frozen and the permanent seasons skip the seasonal offset like
 *   tropical biomes; temperate and cold take the offset even above 0.8, where the mod skips it
 *   (temperate_highlands, 0.9); then {@link SeasonTypes#seasonTemperature}: frozen and permanent winter held
 *   at 0.1 or colder (snow all year, nothing melts), cold snows Late Autumn to Early Spring only, permanent
 *   spring/autumn snow in the world's winter only, permanent summer never.
 * - shouldFreezeWarmEnoughToRainHook (Serene Seasons' patch inside Biome.shouldFreeze, used by ServerLevel.tickChunk
 *   to turn water into ice): an ocean that is not frozen-tagged answers with vanilla's temperature, so cold and
 *   plain oceans get winter snow but never ice over. Frozen oceans keep the seasonal (capped) value.
 */
@Mixin(value = SeasonHooks.class, remap = false)
public class SeasonHooksTypesMixin {

	@Inject(method = "getBiomeTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)F",
			at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$adaptiveTemperature(Level level, Holder<Biome> biome, BlockPos pos,
																CallbackInfoReturnable<Float> cir) {
		Holder<Biome> land = SeasonTypes.resolve(level, biome, pos);
		if (land != biome) cir.setReturnValue(SeasonHooks.getBiomeTemperature(level, land, pos));
	}

	@Inject(method = "getBiomeTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)F",
			at = @At("RETURN"), cancellable = true, remap = false)
	private static void creativeengineering$snowRules(Level level, Holder<Biome> biome, BlockPos pos,
													  CallbackInfoReturnable<Float> cir) {
		float t = cir.getReturnValueF();
		float ruled = SeasonTypes.applySnowRules(level, biome, pos, t);
		if (ruled != t) cir.setReturnValue(ruled);
	}

	@Redirect(method = "isRainingAtHook", remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/Level;m_204166_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private static Holder<Biome> creativeengineering$adaptiveRain(Level level, BlockPos pos) {
		return SeasonTypes.resolve(level, level.getBiome(pos), pos);
	}

	@Inject(method = "shouldFreezeWarmEnoughToRainHook", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$oceanIce(Biome biome, BlockPos pos, LevelReader levelReader,
													 CallbackInfoReturnable<Boolean> cir) {
		if (SeasonTypes.vanillaIce(levelReader.getBiome(pos))) cir.setReturnValue(biome.warmEnoughToRain(pos));
	}

	@Inject(method = "shouldRainInBiomeInSeason", at = @At("HEAD"), cancellable = true, remap = false)
	private static void creativeengineering$typedRain(Level level, Holder<Biome> biome, CallbackInfoReturnable<Boolean> cir) {
		SeasonTypes.Type type = SeasonTypes.tagged(biome);
		if (type == SeasonTypes.Type.ARID) {
			cir.setReturnValue(false);
		} else if (type == SeasonTypes.Type.DRY) {
			cir.setReturnValue(SeasonHelper.getSeasonState(level).getTropicalSeason() == Season.TropicalSeason.MID_WET);
		}
	}

	@Redirect(method = "getBiomeTemperatureInSeason", remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lsereneseasons/config/BiomeConfig;usesTropicalSeasons(Lnet/minecraft/core/Holder;)Z"))
	private static boolean creativeengineering$noSeasonalOffset(Holder<Biome> biome) {
		return SeasonTypes.noSeasonalOffset(biome) || BiomeConfig.usesTropicalSeasons(biome);
	}

	@Redirect(method = "getBiomeTemperatureInSeason", remap = false,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/biome/Biome;m_47554_()F"))
	private static float creativeengineering$temperateOffset(Biome biome, Season.SubSeason subSeason, Holder<Biome> holder, BlockPos pos) {
		float base = biome.getBaseTemperature();
		return SeasonTypes.temperateLike(SeasonTypes.tagged(holder)) ? Math.min(base, 0.8F) : base;
	}

	@Inject(method = "getBiomeTemperatureInSeason", at = @At("RETURN"), cancellable = true, remap = false)
	private static void creativeengineering$typeTemperature(Season.SubSeason subSeason, Holder<Biome> biome, BlockPos pos,
															CallbackInfoReturnable<Float> cir) {
		float t = cir.getReturnValueF();
		float typed = SeasonTypes.seasonTemperature(subSeason, biome, t);
		if (typed != t) cir.setReturnValue(typed);
	}
}
