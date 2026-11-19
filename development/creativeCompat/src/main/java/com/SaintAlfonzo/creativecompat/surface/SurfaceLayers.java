// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.surface;

import java.util.Map;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;
import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Surface layers: the layer blocks that are a walking surface at every height, tag
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
 *   <li><b>Fall damage</b> ({@link #fallDamage}): full. A layer only cushions like its full block when it
 *       has one, and that block lies under it or the pile is over half height; none of the full blocks cushion today.
 *       Before, Immersive Weathering sand and ash and Supplementaries ash took 0.3x above 2 layers.</li>
 *   <li><b>Full block</b> ({@link #settle}): sand, red sand and salt hold 8 layers; a 9th turns the pile into the
 *       block (9 layers = 1 block, the recipes match). Salt does it in Vintage Delight's own code.</li>
 *   <li><b>Replacing</b> ({@link #replaceable}): never by another block; only snow, leaf piles and bamboo leaves
 *       ({@code #creativecompat:replaceable_layers}) are replaced, up to {@code replaceMaxLayers} layers.</li>
 * </ul>
 */
public final class SurfaceLayers {

	private SurfaceLayers() {}

	public static final TagKey<Block> TAG = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "surface_layers"));

	/** Layers a placed block may replace, up to {@code replaceMaxLayers} (snow, leaf piles, bamboo leaves). */
	public static final TagKey<Block> REPLACEABLE = BlockTags.create(
			new ResourceLocation(CreativeCompat.NS, "replaceable_layers"));

	/** Layer block -> the full block a 9th layer turns it into (salt's is in Vintage Delight's SaltLayerBlock). */
	private static final Map<ResourceLocation, ResourceLocation> FULL = Map.of(
			new ResourceLocation("immersive_weathering", "sand_layer_block"), new ResourceLocation("minecraft", "sand"),
			new ResourceLocation("immersive_weathering", "red_sand_layer_block"),
			new ResourceLocation("minecraft", "red_sand"));

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

	/** The full block a 9th layer turns this layer into, or null (no full block). */
	@Nullable
	public static BlockState fullBlock(BlockState layer) {
		ResourceLocation to = FULL.get(ForgeRegistries.BLOCKS.getKey(layer.getBlock()));
		if (to == null) return null;
		Block b = ForgeRegistries.BLOCKS.getValue(to);
		return b == null || b == Blocks.AIR ? null : b.defaultBlockState();
	}

	/**
	 * After a layer block with a full block is placed or changed at {@code pos} (server): a layer resting on a full pile
	 * of the same block makes the pile the full block and gives up one layer, so a 9th layer by hand, falling or
	 * dispenser always ends as one full block. Also runs when the pile below fills up with a layer already above.
	 */
	public static void settle(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide || fullBlock(state) == null) return;
		IntegerProperty p = FallingLayers.layersProperty(state);
		if (p == null) return;
		int max = FallingLayers.maxLayers(state);
		BlockState under = level.getBlockState(pos.below());
		if (under.getBlock() == state.getBlock() && under.getValue(p) == max) {
			level.setBlock(pos.below(), fullBlock(state), Block.UPDATE_ALL);
			int n = state.getValue(p) - 1;
			level.setBlock(pos, n > 0 ? state.setValue(p, n) : state.getFluidState().createLegacyBlock(),
					Block.UPDATE_ALL);
			return;
		}
		if (state.getValue(p) == max) {
			BlockState above = level.getBlockState(pos.above());
			if (above.getBlock() == state.getBlock()) settle(level, pos.above(), above);
		}
	}

	/**
	 * {@code canBeReplaced} of a layer block for another block placed into it; null keeps the block's own rule (its own
	 * item adding a layer, or not a layer ruled here). Leaf piles, bamboo leaves and snow: up to
	 * {@code replaceMaxLayers}. Surface layers: never.
	 */
	@Nullable
	public static Boolean replaceable(BlockState state, BlockPlaceContext ctx) {
		if (ctx.getItemInHand().is(state.getBlock().asItem())) return null;
		if (state.is(REPLACEABLE)) {
			int most = CompatConfig.SPEC.isLoaded() ? CompatConfig.SURFACE.passthrough.replaceMaxLayers.get() : 3;
			return FallingLayers.layers(state) <= most;
		}
		return state.is(TAG) ? Boolean.FALSE : null;
	}

	/**
	 * Fall damage onto a surface layer (replaces the mods' own {@code causeFallDamage} call): full damage, unless the
	 * layer has a full block and that block lies under it or the pile is over half height; then the full block's own
	 * {@code fallOn} decides (a hay-like block would cushion, sand and salt don't).
	 */
	public static boolean fallDamage(Level level, BlockState state, BlockPos pos, Entity entity, float distance,
			DamageSource source) {
		BlockState full = fullBlock(state);
		if (full != null && (level.getBlockState(pos.below()).is(full.getBlock())
				|| FallingLayers.layers(state) * 2 > FallingLayers.maxLayers(state))) {
			full.getBlock().fallOn(level, full, pos, entity, distance);
			return true;
		}
		return entity.causeFallDamage(distance, 1.0F, source);
	}
}
