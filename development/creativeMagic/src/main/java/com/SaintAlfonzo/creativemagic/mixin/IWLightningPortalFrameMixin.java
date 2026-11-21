// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativemagic.mixin;

import java.util.function.Supplier;

import com.SaintAlfonzo.creativemagic.dimension.PortalFrameGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lightning on a Nether portal frame lights the frame instead of weathering it (owner ruling R3,
 * plan_scratch_findings_1121 F10). Immersive Weathering's {@code crying_obsidian_from_lightning} growth turns the struck
 * obsidian into crying obsidian before the bolt's fire lands, so the frame is no longer whole and the portal attempt
 * (PortalSpawnEvent, PortalFailureHandler) never happens. Every growth on an obsidian block that is part of a frame
 * around an empty portal space is skipped; obsidian elsewhere still weathers.
 */
@Mixin(targets = "com.ordana.immersive_weathering.data.block_growths.growths.ConfigurableBlockGrowth", remap = false)
public abstract class IWLightningPortalFrameMixin {

	@Inject(method = "tryGrowing", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativemagic$keepPortalFrame(BlockPos pos, BlockState state, ServerLevel level,
											   Supplier<Holder<Biome>> biome, CallbackInfo ci) {
		if (PortalFrameGuard.isFrameBlock(level, pos, state)) ci.cancel();
	}
}
