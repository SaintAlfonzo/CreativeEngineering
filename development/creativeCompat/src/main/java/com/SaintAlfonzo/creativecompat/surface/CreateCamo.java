package com.SaintAlfonzo.creativecompat.surface;

import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Create side of {@link CamoSpeed} (its own copycat panel and step). Only touched when Create is loaded (PR-CP-08). */
final class CreateCamo {

	private CreateCamo() {}

	/** The copycat's material, or null for another block entity or an empty copycat. */
	static BlockState camo(BlockEntity be) {
		if (be instanceof CopycatBlockEntity copycat) {
			return copycat.hasCustomMaterial() ? copycat.getMaterial() : null;
		}
		return null;
	}
}
