package com.SaintAlfonzo.creativecalendar.compat;

import com.SaintAlfonzo.creativecalendar.seasons.SeasonTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

/**
 * Static targets that CalendarMixinPlugin.postApply wires into Cool Rain's tickRain handler in LevelRenderer
 * (see mixin.CoolRainSeasonMixin). The handler looks the biome up once ({@code getBiome(pos)}) and then reads its
 * precipitation: the first hook keeps that position, the second answers from {@link SeasonTypes#precipitationAt} at
 * it, the same rule as PrettyRainSeasonMixin. Client render thread only.
 */
public final class CoolRainHooks {

	private static BlockPos lastPos;

	private CoolRainHooks() {}

	public static Holder<Biome> getBiome(LevelReader level, BlockPos pos) {
		lastPos = pos;
		return level.getBiome(pos);
	}

	public static Biome.Precipitation getPrecipitation(Biome biome) {
		ClientLevel level = Minecraft.getInstance().level;
		BlockPos pos = lastPos;
		if (level == null || pos == null) return biome.getPrecipitation();
		return SeasonTypes.precipitationAt(level, level.getBiome(pos), pos);
	}
}
