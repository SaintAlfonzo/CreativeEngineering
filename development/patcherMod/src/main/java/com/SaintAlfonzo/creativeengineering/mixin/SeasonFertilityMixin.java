package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.seasons.SeasonTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sereneseasons.config.BiomeConfig;
import sereneseasons.init.ModFertility;

/**
 * Serene Seasons' crop fertility under the season types ({@link SeasonTypes}):
 * - a crop by a river or on a beach counts as growing in the land around it;
 * - arid biomes use the tropical rule (summer crops and unlisted crops, all year), since they have no seasons;
 * - frozen biomes use the "too cold" rule (winter crops only) even where the biome itself is above 0.15.
 */
@Mixin(value = ModFertility.class, remap = false)
public class SeasonFertilityMixin {

	@Redirect(method = "isCropFertile", remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/Level;m_204166_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private static Holder<Biome> creativeengineering$adaptiveBiome(Level level, BlockPos pos) {
		return SeasonTypes.resolve(level, level.getBiome(pos), pos);
	}

	@Redirect(method = "isCropFertile", remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lsereneseasons/config/BiomeConfig;usesTropicalSeasons(Lnet/minecraft/core/Holder;)Z"))
	private static boolean creativeengineering$aridAllYear(Holder<Biome> biome) {
		return SeasonTypes.tagged(biome) == SeasonTypes.Type.ARID || BiomeConfig.usesTropicalSeasons(biome);
	}

	@Redirect(method = "isCropFertile", remap = false,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/biome/Biome;m_198906_(Lnet/minecraft/core/BlockPos;)Z"))
	private static boolean creativeengineering$frozenAlwaysCold(Biome biome, BlockPos pos, String cropName, Level level, BlockPos cropPos) {
		Holder<Biome> holder = SeasonTypes.resolve(level, level.getBiome(cropPos), cropPos);
		if (SeasonTypes.tagged(holder) == SeasonTypes.Type.FROZEN) return false;
		return biome.warmEnoughToRain(pos);
	}
}
