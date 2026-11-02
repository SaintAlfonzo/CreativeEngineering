package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Cool Rain 1.0.2 (coolrain) cancels LevelRenderer.tickRain at HEAD with its own copy, so Serene Seasons'
 * hooks and {@link SeasonRainColumnMixin} never run, and its copy plays the per-block rain sounds whenever the
 * biome's declared precipitation is RAIN: rain sounds in deserts and in winter snow.
 *
 * Cool Rain's copy is a mixin handler merged into LevelRenderer under a generated name
 * ({@code handler$<id>$tickRain}), so this targets it by regex. Priority 1500 applies it after Cool Rain (1000),
 * once the handler exists. The handler looks the biome up once ({@code getBiome(pos)}) and then reads its
 * precipitation: the first redirect keeps that position, the second answers from
 * {@link SeasonTypes#precipitationAt} at it, the same rule as {@link PrettyRainSeasonMixin}. Render thread only.
 * Applied only with Cool Rain loaded (CalendarMixinPlugin).
 */
@Mixin(value = LevelRenderer.class, priority = 1500)
public class CoolRainSeasonMixin {

	@Unique
	private static BlockPos creativeengineering$coolRainPos;

	@Redirect(method = "/^handler\\$\\w+\\$tickRain$/", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/LevelReader;m_204166_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
	private Holder<Biome> creativeengineering$coolRainBiome(LevelReader level, BlockPos pos) {
		creativeengineering$coolRainPos = pos;
		return level.getBiome(pos);
	}

	@Redirect(method = "/^handler\\$\\w+\\$tickRain$/", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/world/level/biome/Biome;m_47530_()Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private Biome.Precipitation creativeengineering$coolRainPrecipitation(Biome biome) {
		ClientLevel level = Minecraft.getInstance().level;
		BlockPos pos = creativeengineering$coolRainPos;
		if (level == null || pos == null) return biome.getPrecipitation();
		return SeasonTypes.precipitationAt(level, level.getBiome(pos), pos);
	}
}
