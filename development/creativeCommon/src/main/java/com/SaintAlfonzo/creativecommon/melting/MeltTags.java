package com.SaintAlfonzo.creativecommon.melting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * The melting block tags ({@link Melting}, feature_writeup/melting.md). Filled by KubeJS: Creative Common's
 * {@code kubejs/data/creativecommon/kubejs/tags/melting_tags.js}, keys {@code b.t_melt_*} / {@code b.t_meltable*} in
 * block_dict.js. Any of them may be empty; the system then has nothing to do for that kind.
 */
public final class MeltTags {

	private MeltTags() {}

	/** Salt sources (The Salt's blocks, Vintage Delight's salt family, the Hearth and Harvest salt lick stages). */
	public static final TagKey<Block> SALT = tag("salt");
	/** Fire sources: fire, campfires, magma, torches, lanterns, glowstone. A block with a lit property counts while lit. */
	public static final TagKey<Block> FIRE = tag("fire");
	/** Lava sources besides any block whose fluid state is lava (lava cauldron). */
	public static final TagKey<Block> LAVA = tag("lava");
	/** Melt fully: ice and frosted ice to water (air when ultra-warm), snow to air. */
	public static final TagKey<Block> MELTABLE = tag("meltable");
	/** Step down one tier ([melting] slowSteps) at the low chance: packed and blue ice. */
	public static final TagKey<Block> MELTABLE_SLOW = tag("meltable_slow");
	/** Air the moment it is placed in an ultra-warm dimension: snow layers, snow blocks, powder snow. */
	public static final TagKey<Block> ULTRAWARM_INSTANT = tag("ultrawarm_instant");

	private static TagKey<Block> tag(String path) {
		return BlockTags.create(new ResourceLocation("creativecommon", "melting/" + path));
	}
}
