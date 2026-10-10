package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.seasons.SeasonTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sereneseasons.season.SeasonHooks;

/**
 * Client half of {@link SeasonHooksTypesMixin}: whether the renderer draws anything in a column
 * ({@code getLevelRendererPrecipitation}, called by Serene Seasons' LevelRenderer redirects with the biome
 * only). A river or beach is swapped for its land using the column {@link SeasonRainColumnMixin} recorded.
 * Separate class because the method is client-only (stripped on a dedicated server).
 */
@Mixin(value = SeasonHooks.class, remap = false)
public class SeasonHooksClientMixin {

	@ModifyVariable(method = "getLevelRendererPrecipitation", at = @At("HEAD"), argsOnly = true, remap = false)
	private static Holder<Biome> creativeengineering$adaptiveColumn(Holder<Biome> biome) {
		Level level = Minecraft.getInstance().level;
		BlockPos column = SeasonTypes.renderColumn();
		return level != null && column != null ? SeasonTypes.resolve(level, biome, column) : biome;
	}
}
