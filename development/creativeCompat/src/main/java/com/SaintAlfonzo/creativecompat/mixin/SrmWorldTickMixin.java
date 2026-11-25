// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.melting.Melting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Snow! Real Magic's snowfall ({@code snownee.snow.WorldTickHandler.tick}, which replaces vanilla's per-chunk snow and
 * ice roll and never asks {@code Biome.shouldSnow}): snow_real_magic.md 4 step 3 and ruling R6.
 * <ul>
 *   <li>Magma within [melting] fireRange stops snow forming ({@link Melting#magmaBlocksFreezing}, the check
 *       {@link BiomeMeltMixin} makes for vanilla): both {@code Hooks.convert} calls (the top block and the plants down
 *       to 5 below) are skipped there. Ice forming still goes through {@code Biome.shouldFreeze}, already covered.</li>
 *   <li>SRM's own ice melt in seasonal biomes is off: ice melts by Serene Seasons and Immersive Weathering only, as
 *       without SRM (R6).</li>
 * </ul>
 * Gated on {@code snowrealmagic} in {@code CompatMixinPlugin}; {@code require = 0} so a changed SRM only loses the
 * rule.
 */
@Pseudo
@Mixin(targets = "snownee.snow.WorldTickHandler", remap = false)
public abstract class SrmWorldTickMixin {

	@Redirect(method = "tick", remap = false, require = 0, at = @At(value = "INVOKE", remap = false,
			target = "Lsnownee/snow/Hooks;convert(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;IIZ)Z"))
	private static boolean creativecompat$noSnowNearMagma(LevelAccessor level, BlockPos pos, BlockState state, int layers,
			int flags, boolean canConvert) {
		if (Melting.magmaBlocksFreezing(level, pos)) return false;
		return snownee.snow.Hooks.convert(level, pos, state, layers, flags, canConvert);
	}

	@Redirect(method = "tick", remap = false, require = 0, at = @At(value = "INVOKE", remap = false,
			target = "Lsnownee/snow/ModUtil;snowAndIceMeltInWarmBiomes(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/Holder;)Z"))
	private static boolean creativecompat$noSrmIceMelt(ResourceKey<Level> dimension, Holder<Biome> biome) {
		return false;
	}
}
