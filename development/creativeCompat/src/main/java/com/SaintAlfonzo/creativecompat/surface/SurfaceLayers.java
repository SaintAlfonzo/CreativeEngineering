// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Surface layers (owner notes 2026-11-18, "layers"): the layer blocks that are a walking surface at every height, tag
 * {@code #creativecompat:surface_layers}: Immersive Weathering's sand, red sand and ash layers, Supplementaries ash and
 * fodder, Vintage Delight's salt and Immersive Engineering sawdust. Leaf piles, bamboo leaves and snow layers are not
 * in it: they are walk-through ({@code #creativecompat:passthrough_layers}).
 *
 * <p>One rule for all of them:
 * <ul>
 *   <li><b>Height:</b> the collision is the layer's visible shape (2 px per layer, sawdust its own 9 steps), for every
 *       entity ({@code mixin/IwLayerCollisionMixin}, {@code SuppAshCollisionMixin}, {@code IeSawdustCollisionMixin},
 *       Vintage Delight's {@code SaltLayerBlock}). Before, sand and fodder stood on the full layer height, salt and
 *       Supplementaries ash one layer lower, sawdust 2 px lower, Immersive Weathering ash half the height and nothing
 *       on ash over ash.</li>
 *   <li><b>Sinking:</b> visual, like a full sinking block ({@link SinkingRender}): the layer's tier decides
 *       (dust, soft), {@code [surface.sinking] depthPixels} deep at every layer count.</li>
 *   <li><b>Speed:</b> the layer's own BlockRunner tier at every layer count. BlockRunner reads the block 0.2 below the
 *       feet, which is the block under a layer of 1 (or 1-2 for sawdust); {@link #afterBlockRunner} puts the layer's
 *       speed back when the feet stand on it. {@link RunSpeed} (WTHIT) follows the same rule.</li>
 * </ul>
 */
public final class SurfaceLayers {

	private SurfaceLayers() {}

	public static final TagKey<Block> TAG = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "surface_layers"));

	/** The state is a surface layer below full height (a full one is an ordinary full block to everything). */
	public static boolean is(BlockState state) {
		return state.is(TAG);
	}

	/**
	 * After BlockRunner (re)applies its block speed: when the feet stand on a surface layer too thin for BlockRunner's
	 * 0.2 look-down, use the layer's speed instead of the block beneath. Same modifier BlockRunner adds.
	 */
	public static void afterBlockRunner(LivingEntity entity) {
		if (entity instanceof Player player && player.getAbilities().flying) {
			return; // BlockRunner gives no block speed while flying either
		}
		Level level = entity.level;
		BlockPos feet = entity.blockPosition();
		BlockPos on = entity.getOnPos();
		if (feet.equals(on)) {
			return; // BlockRunner already reads the layer
		}
		BlockState state = level.getBlockState(feet);
		if (!is(state)) {
			return;
		}
		AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		speed.removeModifier(BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID);
		double factor = BlockSpeedManager.INSTANCE.getSpeedFactor(state.getBlock());
		if (factor != 1.0) {
			double base = speed.getBaseValue();
			speed.addTransientModifier(new AttributeModifier(BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID,
					"Block speed boost", factor * base - base, AttributeModifier.Operation.ADDITION));
		}
	}
}
