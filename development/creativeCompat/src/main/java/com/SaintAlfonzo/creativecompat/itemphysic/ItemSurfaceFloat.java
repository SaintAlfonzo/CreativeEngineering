// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.itemphysic;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.surface.PassThrough;
import com.SaintAlfonzo.creativecompat.surface.PassTier;
import com.SaintAlfonzo.creativecompat.surface.SurfaceLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.itemphysic.ItemPhysic;
import team.creative.itemphysic.server.ItemPhysicServer;

/**
 * Surface floating for dropped items under ItemPhysic (feature_writeup/item_physics.md 10; rulings 9.6 R4, R16, R17,
 * R23). ItemPhysic's floaters dive about 2.8 blocks from a 3-block drop and bob for good, and nothing floats in lava.
 * This replaces ItemPhysic's vertical force with a damped surface spring for three kinds of item:
 * <ul>
 *   <li><b>skim</b> ({@code physics/skims}: foliage, fibre, light powder) rests on the water surface;</li>
 *   <li><b>float</b> (every other floater, with {@code damping} on) dips once, then rests just below it;</li>
 *   <li><b>lava</b> ({@link ItemPhysicSplit#lavaSkims}: lavaproof or fire resistant, not a heavy block) rests on the
 *       lava surface. Heavy blocks keep ItemPhysic's slow sink.</li>
 * </ul>
 * In the fluid: {@code vy = vy0 x drag + clamp((rest - y) x spring, +-maxPush)}, where {@code vy0} is the speed before
 * ItemPhysic's own change this tick (taken by the drag twice while still sinking at or below the rest height) and
 * {@code rest} the surface less the motion's depth. Simulated (2026-11-25): from a 3-block drop a skimmer dips
 * about 0.14 below the surface, a damped floater 0.52 (ItemPhysic's float: 2.8, then a 0.35 bob for good); a
 * lavaproof item from 2 blocks onto lava 0.2. None bob once settled. In the air a skimmer about to
 * pass its rest height this tick is stopped there (entry look-ahead), so a fast drop does not overshoot.
 *
 * <p><b>Sinkable layers (R23):</b> a skimmer (water or lava) also rests on top of snow layers, the pass-through layer
 * tier and the surface layers, whose collision lets items sink into them; the outline top counts as the ground.
 *
 * <p>Called from {@code mixin/ItemPhysicServerMixin} at the HEAD (records the speed) and TAIL of
 * {@code ItemPhysicServer.updatePre}, on both sides: item motion is simulated on the client too and the tags are synced.
 */
public final class ItemSurfaceFloat {

	private ItemSurfaceFloat() {}

	/** Highest fluid block walked up to find the surface. */
	private static final int MAX_DEPTH = 8;
	/** Ground friction for a skimmer held on a layer top (vanilla block friction 0.6; the item is not on ground). */
	private static final double LAYER_FRICTION = 0.6;

	private static final ThreadLocal<double[]> VY_BEFORE = ThreadLocal.withInitial(() -> new double[1]);

	/** HEAD of {@code updatePre}: the vertical speed before ItemPhysic's change. */
	public static void before(ItemEntity item) {
		VY_BEFORE.get()[0] = item.getDeltaMovement().y;
	}

	/** TAIL of {@code updatePre}. */
	public static void after(ItemEntity item) {
		if (!CompatConfig.SPEC.isLoaded() || item.isNoGravity()) return;   // NoGravity: held in a web, or a mod's
		ItemStack stack = item.getItem();
		if (stack.isEmpty()) return;
		CompatConfig.ItemPhysics cfg = CompatConfig.ITEM_PHYSICS;
		Fluid fluid = ItemPhysicServer.fluid.get();
		if (fluid != null) {
			inFluid(item, stack, fluid, cfg);
		} else {
			inAir(item, stack, cfg);
		}
	}

	private static void inFluid(ItemEntity item, ItemStack stack, Fluid fluid, CompatConfig.ItemPhysics cfg) {
		CompatConfig.ItemPhysics.Motion m = motion(stack, fluid.is(FluidTags.LAVA), cfg);
		if (m == null) return;
		TagKey<Fluid> tag = fluid.is(FluidTags.LAVA) ? FluidTags.LAVA : FluidTags.WATER;
		double surface = surface(item.level, item.blockPosition(), tag);
		if (Double.isNaN(surface)) return;
		double max = cfg.maxPush.get();
		double rest = surface - m.depth.get();
		double drag = m.drag.get();
		double vy0 = VY_BEFORE.get()[0];
		// still sinking at or below the rest height (the speed carried in from the fall): the surface takes it twice
		if (vy0 < 0.0 && item.getY() <= rest + 1.0E-6) vy0 *= drag;
		double push = Mth.clamp((rest - item.getY()) * m.spring.get(), -max, max);
		Vec3 v = item.getDeltaMovement();
		item.setDeltaMovement(v.x, vy0 * drag + push, v.z);
	}

	/** The motion for this stack in water or lava, or null to keep ItemPhysic's. */
	private static CompatConfig.ItemPhysics.Motion motion(ItemStack stack, boolean lava, CompatConfig.ItemPhysics cfg) {
		if (lava) return cfg.surfaceFloat.get() && ItemPhysicSplit.lavaSkims(stack) ? cfg.lava : null;
		if (ItemPhysicSplit.forcedSink(stack)) return null;
		if (cfg.surfaceFloat.get() && ItemPhysicSplit.skims(stack)) return cfg.skim;
		if (cfg.damping.get() && floats(stack)) return cfg.floating;
		return null;
	}

	/** The same float test ItemPhysicServerMixin routes ItemPhysic's swimming list through. */
	private static boolean floats(ItemStack stack) {
		return ItemPhysicSplit.isFloat(stack) || ItemPhysic.CONFIG.general.swimmingItems.canPass(stack);
	}

	private static void inAir(ItemEntity item, ItemStack stack, CompatConfig.ItemPhysics cfg) {
		double vy = item.getDeltaMovement().y;
		if (vy >= 0.0) return;
		boolean water = cfg.surfaceFloat.get() && ItemPhysicSplit.skims(stack);
		boolean lava = cfg.surfaceFloat.get() && ItemPhysicSplit.lavaSkims(stack);
		boolean layers = cfg.skimOnLayers.get() && (ItemPhysicSplit.skims(stack) || ItemPhysicSplit.lavaSkims(stack));
		if (!water && !lava && !layers) return;
		Level level = item.level;
		double y = item.getY();
		int top = Mth.floor(y), bottom = Mth.floor(y + vy);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int cy = top; cy >= bottom; cy--) {
			pos.set(item.getBlockX(), cy, item.getBlockZ());
			if (water || lava) {
				FluidState fs = level.getFluidState(pos);
				TagKey<Fluid> tag = water && fs.is(FluidTags.WATER) ? FluidTags.WATER
						: lava && fs.is(FluidTags.LAVA) ? FluidTags.LAVA : null;
				if (tag != null) {
					// entry look-ahead: stop at the rest height instead of passing it this tick
					double surface = surface(level, pos, tag);
					if (Double.isNaN(surface)) return;
					double rest = surface - (tag == FluidTags.LAVA ? cfg.lava : cfg.skim).depth.get();
					if (y + vy < rest && y >= rest) land(item, rest - y, false);
					return;
				}
			}
			BlockState state = level.getBlockState(pos);
			if (state.isAir()) continue;
			if (layers && sinkable(state)) {
				VoxelShape shape = state.getShape(level, pos);
				if (shape.isEmpty()) continue;
				double layerTop = cy + shape.max(Direction.Axis.Y);
				if (y >= layerTop - 1.0E-3) {
					if (y + vy < layerTop) land(item, layerTop - y, true);
				} else if (y >= cy) {
					item.setPos(item.getX(), layerTop, item.getZ());   // sunk in (a layer grew around it): back on top
					land(item, 0.0, true);
				}
				return;
			}
			if (!state.getCollisionShape(level, pos, CollisionContext.of(item)).isEmpty()) return;   // solid: lands there
		}
	}

	/** Snow layers, the pass-through layer tier and the surface layers: blocks items sink into (R23). */
	public static boolean sinkable(BlockState state) {
		return state.getBlock() instanceof SnowLayerBlock || PassThrough.tierOf(state) == PassTier.LAYERS
				|| SurfaceLayers.is(state);
	}

	private static void land(ItemEntity item, double vy, boolean ground) {
		Vec3 v = item.getDeltaMovement();
		double f = ground ? LAYER_FRICTION : 1.0;
		item.setDeltaMovement(v.x * f, vy, v.z * f);
	}

	/**
	 * Surface height of the fluid column holding {@code start} (the top fluid block's own height), or NaN when
	 * {@code start} holds none of it.
	 */
	public static double surface(Level level, BlockPos start, TagKey<Fluid> tag) {
		BlockPos.MutableBlockPos pos = start.mutable();
		FluidState fs = level.getFluidState(pos);
		if (!fs.is(tag)) return Double.NaN;
		for (int n = 0; n < MAX_DEPTH; n++) {
			pos.move(0, 1, 0);
			FluidState up = level.getFluidState(pos);
			if (!up.is(tag)) {
				pos.move(0, -1, 0);
				break;
			}
			fs = up;
		}
		return pos.getY() + fs.getHeight(level, pos);
	}
}
