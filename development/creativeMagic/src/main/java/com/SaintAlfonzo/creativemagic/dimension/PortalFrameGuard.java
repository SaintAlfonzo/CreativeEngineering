// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativemagic.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

/** Whether an obsidian block is part of a Nether portal frame (IWLightningPortalFrameMixin, plan_scratch_findings_1121 R3). */
public final class PortalFrameGuard {

	private PortalFrameGuard() {}

	/**
	 * True when {@code state} is obsidian and a neighbor is a portal block or the inside of a whole, empty frame (air or
	 * fire, the shape vanilla's PortalSpawnEvent checks; findEmptyPortalShape tries both axes).
	 */
	public static boolean isFrameBlock(ServerLevel level, BlockPos pos, BlockState state) {
		if (!state.is(Blocks.OBSIDIAN)) return false;
		for (Direction d : Direction.values()) {
			BlockPos n = pos.relative(d);
			BlockState ns = level.getBlockState(n);
			if (ns.is(BlockTags.PORTALS)) return true;
			if ((ns.isAir() || ns.is(BlockTags.FIRE))
					&& PortalShape.findEmptyPortalShape(level, n, Direction.Axis.X).isPresent()) {
				return true;
			}
		}
		return false;
	}
}
