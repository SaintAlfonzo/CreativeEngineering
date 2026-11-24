package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.SaintAlfonzo.creativecommon.melting.Melting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;

/**
 * {@link VanillaLightMeltMixin} for Rare Ice's ice block, which copies vanilla's light melt in its own randomTick
 * ({@code m_213898_}). Skipped when Rare Ice is absent.
 */
@Pseudo
@Mixin(targets = "me.shedaniel.rareice.forge.blocks.RareIceBlock", remap = false)
public abstract class RareIceLightMeltMixin {

	@Redirect(method = "m_213898_", remap = false, require = 0, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/server/level/ServerLevel;m_45517_(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I"))
	private int creativecommon$lightMelt(ServerLevel level, LightLayer layer, BlockPos pos) {
		return Melting.vanillaLight(level, layer, pos);
	}
}
