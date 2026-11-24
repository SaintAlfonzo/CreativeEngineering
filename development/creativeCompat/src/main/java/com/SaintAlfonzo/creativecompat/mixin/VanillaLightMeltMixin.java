package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativecommon.melting.Melting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SnowLayerBlock;

/**
 * Vanilla light melting off ([melting] vanillaLightMelting = false, feature_writeup/melting.md): IceBlock and
 * SnowLayerBlock randomTick ({@code m_213898_}) read block light ({@code ServerLevel.m_45517_}) and melt above 11.
 * The read returns 0 while the switch is off, so only the melting sources melt them. Other injections into these
 * methods (Immersive Weathering's rain melt, Creative Delight's salt layer tick) keep running.
 */
@Mixin(value = {IceBlock.class, SnowLayerBlock.class}, remap = false)
public abstract class VanillaLightMeltMixin {

	@Redirect(method = "m_213898_", remap = false, require = 1, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/server/level/ServerLevel;m_45517_(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I"))
	private int creativecommon$lightMelt(ServerLevel level, LightLayer layer, BlockPos pos) {
		return Melting.vanillaLight(level, layer, pos);
	}
}
