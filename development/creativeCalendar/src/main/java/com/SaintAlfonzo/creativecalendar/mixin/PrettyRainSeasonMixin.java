package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Pretty Rain 1.1.1 (particlerain) with renderVanillaWeather / tickVanillaWeather off cancels
 * LevelRenderer.renderSnowAndRain and tickRain at HEAD, so Serene Seasons' renderer redirects and
 * {@link SeasonHooksClientMixin} / {@link SeasonRainColumnMixin} never run. It then picks rain, snow or
 * dust from the biome's declared precipitation alone: a warm river in a desert (declares rain) rained
 * though the HUD read Arid, deserts rained in no season either way, and temperate biomes rained in winter.
 *
 * Both of its precipitation reads (particles, ambient sound) answer from
 * {@link SeasonTypes#precipitationAt} instead: river/beach resolved to its land, the season's rule, and
 * rain or snow by the seasonal temperature, the same as the vanilla renderer with Serene Seasons.
 * Pseudo: Pretty Rain is a client-only extra and may be removed.
 */
@Pseudo
@Mixin(targets = "com.leclowndu93150.particlerain.WeatherParticleSpawner", remap = false)
public class PrettyRainSeasonMixin {

	@Redirect(method = "spawnParticle", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/biome/Biome;m_47530_()Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private static Biome.Precipitation creativeengineering$particlePrecipitation(Biome biome, ClientLevel level, Holder<Biome> holder,
																				 double x, double y, double z) {
		return SeasonTypes.precipitationAt(level, holder, new BlockPos(x, y, z));
	}

	@Redirect(method = "getBiomeSound", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/biome/Biome;m_47530_()Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private static Biome.Precipitation creativeengineering$soundPrecipitation(Biome biome, BlockPos pos, boolean above) {
		ClientLevel level = Minecraft.getInstance().level;
		return level != null ? SeasonTypes.precipitationAt(level, level.getBiome(pos), pos) : biome.getPrecipitation();
	}
}
