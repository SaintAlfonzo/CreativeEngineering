// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** The two block state properties, on the target ids only (OxidationTargets), and the tag that lets them age. */
public final class OxidationProperties {
	public static final EnumProperty<OxidationStage> STAGE = EnumProperty.create("creativeoxidation_stage", OxidationStage.class);
	public static final BooleanProperty WAXED = BooleanProperty.create("creativeoxidation_waxed");
	/** Blocks that age, wax and scrape (data: data/creativeoxidation/tags/blocks/oxidizable_blocks.json). */
	public static final TagKey<Block> OXIDIZABLE_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY,
			new ResourceLocation(CreativeOxidation.MOD_ID, "oxidizable_blocks"));

	private OxidationProperties() {
	}

	public static boolean hasProperties(BlockState state) {
		return state.hasProperty(STAGE) && state.hasProperty(WAXED);
	}

	public static boolean isOxidizable(BlockState state) {
		return hasProperties(state) && state.is(OXIDIZABLE_BLOCKS);
	}

	/** Target, in the tag, not waxed, not yet oxidized. */
	public static boolean isAging(BlockState state) {
		return isOxidizable(state) && OxidationRules.isAging(true, state.getValue(WAXED), state.getValue(STAGE));
	}
}
