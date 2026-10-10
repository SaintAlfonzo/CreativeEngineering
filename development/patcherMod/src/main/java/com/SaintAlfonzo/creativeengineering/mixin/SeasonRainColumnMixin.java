package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.seasons.SeasonTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Records which column the rain renderer is on, for {@link SeasonHooksClientMixin}: Serene Seasons already
 * redirects the biome lookups in these two methods, so the position is taken from the call just before
 * (renderSnowAndRain sets its MutableBlockPos to the column, tickRain asks the heightmap for the splash spot).
 * SRG names with remap = false: this project ships no refmap.
 */
@Mixin(LevelRenderer.class)
public class SeasonRainColumnMixin {

	// renderSnowAndRain -> MutableBlockPos.set(double, double, double)
	@Redirect(method = "m_109703_", remap = false,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;m_122169_(DDD)Lnet/minecraft/core/BlockPos$MutableBlockPos;"))
	private BlockPos.MutableBlockPos creativeengineering$rainColumn(BlockPos.MutableBlockPos pos, double x, double y, double z) {
		SeasonTypes.setRenderColumn((int) Math.floor(x), (int) Math.floor(z));
		return pos.set(x, y, z);
	}

	// tickRain -> LevelReader.getHeightmapPos
	@Redirect(method = "m_109693_", remap = false,
			at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/level/LevelReader;m_5452_(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
	private BlockPos creativeengineering$splashColumn(LevelReader level, Heightmap.Types type, BlockPos pos) {
		BlockPos top = level.getHeightmapPos(type, pos);
		SeasonTypes.setRenderColumn(top.getX(), top.getZ());
		return top;
	}
}
