package com.SaintAlfonzo.creativecompat.melting;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * The melting block tags ({@link Melting}, feature_writeup/melting.md). Filled by KubeJS: Creative Compat's
 * {@code kubejs/data/creativecompat/kubejs/tags/melting_tags.js}, keys {@code b.t_melt_*} / {@code b.t_meltable*} in
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
	/** Melt fully: ice and frosted ice to water (air when ultra-warm), snow to air; frozen decor by [melting] meltProducts. */
	public static final TagKey<Block> MELTABLE = tag("meltable");
	/**
	 * Step down one tier ([melting] slowSteps) at the low chance: packed and blue ice, IW cryosol and permafrost,
	 * Architect's Palette packed ice. Only lava and magma (close range) and salt melt them (G11).
	 */
	public static final TagKey<Block> MELTABLE_SLOW = tag("meltable_slow");
	/** Slow targets that only lava melts, and only by touching it: blue ice, IW permafrost (G11). Never saltable. */
	public static final TagKey<Block> LAVA_ONLY = tag("lava_only");
	/** Targets that melt at [melting] sluggishMeltFactor of their chance: IW snow bricks (E1). */
	public static final TagKey<Block> SLUGGISH = tag("sluggish");
	/** The only targets salt melts (salt blocks, salt dispenser, Salt Melt); product by [melting] saltProducts. */
	public static final TagKey<Block> SALTABLE = tag("saltable");
	/** Snow resting on these ignores heat and light, not salt: blue ice, IW permafrost. */
	public static final TagKey<Block> KEEPS_SNOW = tag("keeps_snow");
	/** Fire sources that also melt snow at once within [melting] magmaInstantRange and stop snow and ice forming. */
	public static final TagKey<Block> MAGMA = tag("magma");
	/** Air the moment it is placed in an ultra-warm dimension: snow layers, snow blocks, powder snow. */
	public static final TagKey<Block> ULTRAWARM_INSTANT = tag("ultrawarm_instant");

	/**
	 * Hot biomes (G11): ice and snow there take melt ticks every [melting] hotMeltTickInterval with no source needed.
	 * Filled from Creative Calendar's arid and tropical season types. Ultra-warm dimensions (the Nether) count without it.
	 */
	public static final TagKey<Biome> HOT_BIOMES = TagKey.create(Registry.BIOME_REGISTRY,
			new ResourceLocation("creativecompat", "melting/hot"));

	/** Items that melt ice and snow (salt dust, placed as a salt layer; Salt Melt): the "Usable on ice and snow" tooltip. */
	public static final TagKey<Item> SALT_ITEMS = ItemTags.create(new ResourceLocation("creativecompat", "melting/salt_items"));

	private static TagKey<Block> tag(String path) {
		return BlockTags.create(new ResourceLocation("creativecompat", "melting/" + path));
	}
}
