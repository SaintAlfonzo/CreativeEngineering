// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.melting.Melting;
import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Snow! Real Magic's snowfall ({@code snownee.snow.WorldTickHandler.tick}, which replaces vanilla's per-chunk snow and
 * ice roll and never asks {@code Biome.shouldSnow}): snow_real_magic.md 4 step 3 and ruling R6.
 * <ul>
 *   <li>Magma within [melting] fireRange stops snow forming ({@link Melting#magmaBlocksFreezing}, the check
 *       {@link BiomeMeltMixin} makes for vanilla): both {@code Hooks.convert} calls (the top block and the plants down
 *       to 5 below) are skipped there. Ice forming still goes through {@code Biome.shouldFreeze}, already covered.</li>
 *   <li>SRM's own ice melt in seasonal biomes is off: ice melts by Serene Seasons and Immersive Weathering only, as
 *       without SRM (R6).</li>
 *   <li>Blizzards (R13, part B): the {@code blizzardStrength} gamerule reads 0 unless the level is thundering, so rain
 *       keeps SRM's plain snowfall and thunder brings the falling snow; a blizzard never drops onto a column whose top
 *       snow already has 8 layers (no towers, R5).</li>
 *   <li>Snowfall on a block whose snowy look is off turns the look back on (R19), at the column's top block and the
 *       block above it.</li>
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

	@Redirect(method = "tick", remap = false, require = 0, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/world/level/GameRules;m_46215_(Lnet/minecraft/world/level/GameRules$Key;)I"))
	private static int creativecompat$blizzardInThunder(GameRules rules, GameRules.Key<GameRules.IntegerValue> key,
														ServerLevel level, LevelChunk chunk, RandomSource random) {
		int value = rules.getInt(key);
		return level.isThundering() ? value : 0;
	}

	@Inject(method = "doBlizzard", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void creativecompat$blizzardCap(ServerLevel level, BlockPos pos, int strength, CallbackInfo ci) {
		try {
			if (SnowRealMagic.fullSnowAbove(level, pos)) ci.cancel();
		} catch (Throwable t) {
			HookFaults.once("SrmWorldTickMixin#doBlizzard", t);
		}
	}

	@Redirect(method = "tick", remap = false, require = 0, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/world/level/block/Block;m_141997_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/biome/Biome$Precipitation;)V"))
	private static void creativecompat$snowfallLookOn(Block block, BlockState state, Level level, BlockPos pos,
													  Biome.Precipitation precipitation) {
		block.handlePrecipitation(state, level, pos, precipitation);
		try {
			if (precipitation == Biome.Precipitation.SNOW) {
				SnowRealMagic.lookOn(level, pos);
				SnowRealMagic.lookOn(level, pos.above());
			}
		} catch (Throwable t) {
			HookFaults.once("SrmWorldTickMixin#snowfall", t);
		}
	}
}
