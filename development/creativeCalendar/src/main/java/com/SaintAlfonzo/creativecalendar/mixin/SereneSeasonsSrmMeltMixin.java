// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecalendar.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Serene Seasons' snow and ice melt kept while Snow! Real Magic is loaded (feature_writeup/snow_real_magic.md ruling
 * R6). SRM cancels {@code RandomUpdateHandler.onWorldTick} at HEAD (it melts by its own random tick, which the pack
 * turns off with {@code snowNeverMelt = true}). This mixin applies after SRM's ({@code priority = 1100}) and
 * {@code CalendarMixinPlugin.postApply} takes SRM's callback out of the handler, so the handler runs as without SRM,
 * {@link SereneSeasonsMeltThrottleMixin}'s throttle included. The two redirects let it melt SRM's snowy plant like a
 * snow layer: the plant stays where the snow was. Needs Serene Seasons and SRM.
 */
@Pseudo
@Mixin(targets = "sereneseasons.handler.season.RandomUpdateHandler", remap = false, priority = 1100)
public abstract class SereneSeasonsSrmMeltMixin {

	// meltInChunk: "if (state.getBlock() == Blocks.SNOW)" on the top block
	@Redirect(method = "meltInChunk", remap = false, require = 0, at = @At(value = "INVOKE", ordinal = 0, remap = false,
			target = "Lnet/minecraft/world/level/block/state/BlockState;m_60734_()Lnet/minecraft/world/level/block/Block;"))
	private static Block creativecalendar$srmSnowIsSnow(BlockState state) {
		Block block = state.getBlock();
		return srmSnow(state) ? Blocks.SNOW : block;
	}

	// meltInChunk: "level.setBlockAndUpdate(topPos, AIR)" for that snow
	@Redirect(method = "meltInChunk", remap = false, require = 0, at = @At(value = "INVOKE", ordinal = 0, remap = false,
			target = "Lnet/minecraft/server/level/ServerLevel;m_46597_(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private static boolean creativecalendar$srmSnowKeepsPlant(ServerLevel level, BlockPos pos, BlockState to) {
		BlockState here = level.getBlockState(pos);
		if (srmSnow(here)) {
			BlockState held = ((snownee.snow.block.SnowVariant) here.getBlock()).getRaw(here, level, pos);
			if (held != null && !held.isAir()) to = held;
		}
		return level.setBlockAndUpdate(pos, to);
	}

	private static boolean srmSnow(BlockState state) {
		return state.getBlock() instanceof SnowLayerBlock && state.getBlock() instanceof snownee.snow.block.SnowVariant
				&& state.hasBlockEntity();
	}
}
