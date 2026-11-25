// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Client half of the Snow! Real Magic fold-in (snow_real_magic.md 4c, rulings R11 and R19), called from
 * {@code mixin.SrmRenderMixin} inside SRM's render hook (its vanilla and Rubidium chunk paths both end there).
 * <ul>
 *   <li>R11, overlay always on: SRM draws its snow-drip sides ({@code snowrealmagic:block/overlay}, the author's
 *       texture) on the block under a snowy plant only after an empty-hand toggle click. Here every snowy plant draws
 *       them, and plain snow layers are routed through the same hook to draw them too. Plain snow is drawn in the
 *       solid pass as before; the drip sides go to the cutout-mipped pass, so plain snow is also put in that pass
 *       ({@link #setup}). No drip sides over another snow layer or over a block with SRM's {@code snowy} property
 *       (snowy grass already shows snow on its sides), the same blocks SRM's own toggle skips.</li>
 *   <li>R19, snowy look off: the held block drawn with its plain model, the snow layer kept, no snowy variant, drip
 *       sides or snow cap.</li>
 * </ul>
 * Every SRM type stays in this class, loaded only from SRM's own client classes.
 */
public final class SnowRealMagicClient {

	private SnowRealMagicClient() {}

	/** Plain snow layers also render in the cutout-mipped pass, where the drip sides are drawn. Client setup. */
	@SuppressWarnings("deprecation")
	public static void setup() {
		RenderType solid = RenderType.solid(), cutout = RenderType.cutoutMipped();
		ItemBlockRenderTypes.setRenderLayer(Blocks.SNOW, type -> type == solid || type == cutout);
	}

	/** Plain snow layers go through SRM's render hook (R11). */
	public static boolean drawsPlainSnow(BlockState state) {
		return state.getBlock() == Blocks.SNOW;
	}

	/** True: drawn, false: nothing drawn, null: SRM draws as it would. */
	@Nullable
	public static Boolean render(BlockAndTintGetter world, BlockPos pos, BlockState state, BlockState camo,
								 snownee.snow.block.entity.SnowBlockEntity.Options options, @Nullable RenderType layer,
								 Supplier<RandomSource> random, boolean cullSides, snownee.snow.client.RenderAPI api) {
		if (state.getBlock() == Blocks.SNOW) return plainSnow(world, pos, state, layer, random, cullSides, api);
		if (options instanceof SrmLookFlag flag && flag.creativecompat$lookOff()) {
			return lookOff(world, pos, state, camo, options, layer, random, cullSides, api);
		}
		if (snownee.snow.CoreModule.TILE_BLOCK.is(state)) {
			boolean drip = dripUnder(world, pos);
			if (options.renderOverlay != drip) {
				// the same render with the drip sides as R11 wants (a copy: the block entity keeps SRM's own flag)
				snownee.snow.block.entity.SnowBlockEntity.Options copy = new snownee.snow.block.entity.SnowBlockEntity.Options();
				copy.update(drip, options.renderBottom);
				return snownee.snow.client.SnowClient.renderHook(world, pos, state, camo, copy, layer, random, cullSides, api);
			}
		}
		return null;
	}

	/**
	 * The drip sides belong on the block under a snow layer at {@code pos}, and one of its four sides can be seen (SRM
	 * draws the sides unculled, so flat snowfields would otherwise add hidden faces under every layer).
	 */
	static boolean dripUnder(BlockAndTintGetter world, BlockPos pos) {
		BlockPos under = pos.below();
		BlockState below = world.getBlockState(under);
		if (below.isAir() || below.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock
				|| below.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.SNOWY)) {
			return false;
		}
		for (net.minecraft.core.Direction side : net.minecraft.core.Direction.Plane.HORIZONTAL) {
			BlockPos n = under.relative(side);
			if (!world.getBlockState(n).isSolidRender(world, n)) return true;
		}
		return false;
	}

	private static boolean plainSnow(BlockAndTintGetter world, BlockPos pos, BlockState state, @Nullable RenderType layer,
									 Supplier<RandomSource> random, boolean cullSides, snownee.snow.client.RenderAPI api) {
		boolean drawn = false;
		if (layer == null || layer == RenderType.solid()) {
			drawn = api.translateYAndRender(world, state, pos, layer, random, cullSides, model(state), 0.0);
		}
		if ((layer == null || layer == RenderType.cutoutMipped()) && dripUnder(world, pos)) {
			try {
				drawn |= api.translateYAndRender(world, snownee.snow.CoreModule.TILE_BLOCK.defaultBlockState(), pos.below(),
						layer, random, cullSides, overlayModel(), -1.0);
			} catch (Throwable t) {
				HookFaults.once("SnowRealMagicClient#drip", t);
			}
		}
		return drawn;
	}

	private static boolean lookOff(BlockAndTintGetter world, BlockPos pos, BlockState state, BlockState camo,
								   snownee.snow.block.entity.SnowBlockEntity.Options options, @Nullable RenderType layer,
								   Supplier<RandomSource> random, boolean cullSides, snownee.snow.client.RenderAPI api) {
		boolean solidPass = layer == null || layer == RenderType.solid();
		if (solidPass && state.getBlock() instanceof snownee.snow.block.WatcherSnowVariant watcher) {
			watcher.updateOptions(state, world, pos, options);
		}
		boolean drawn = false;
		if (!camo.isAir() && camo.getRenderShape() == RenderShape.MODEL) {
			double y = camo.is(snownee.snow.CoreModule.OFFSET_Y) ? 0.101 : 0.0;
			drawn = api.translateYAndRender(world, camo, pos, layer, random, cullSides, model(camo), y);
		}
		if (options.renderBottom && solidPass && state.getBlock() instanceof snownee.snow.block.SnowVariant variant) {
			BlockState snow = variant.getSnowState(state, world, pos);
			if (!snow.isAir()) {
				double y = snownee.snow.CoreModule.SLAB.is(state) ? 0.5 : 0.0;
				drawn |= api.translateYAndRender(world, snow, pos, layer, random, cullSides, model(snow), y);
			}
		}
		return drawn;
	}

	private static BakedModel model(BlockState state) {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	private static BakedModel overlayModel() {
		BakedModel cached = snownee.snow.client.SnowClient.cachedOverlayModel;
		if (cached == null) {
			cached = Minecraft.getInstance().getModelManager().getModel(snownee.snow.client.SnowClient.OVERLAY_MODEL);
			snownee.snow.client.SnowClient.cachedOverlayModel = cached;
		}
		return cached;
	}
}
