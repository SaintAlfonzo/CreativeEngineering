package com.SaintAlfonzo.creativedelight.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * R4 (plan_small_fixes_1116.md, largecontent/mod-additions #63): the block-breaking crack overlay draws every quad of a
 * block's model, so the straw rug's fringe planes (8 px outside the block) showed a crack texture hanging in the air. For
 * blocks in {@code #creativedelight:clip_break_overlay} the overlay pass (only that pass, see
 * {@code mixin.minecraft.BreakOverlayClipMixin}) draws the model without any quad that reaches outside the 0-1 block box.
 * The normal render keeps the fringe.
 */
public final class BreakOverlayClip {
	public static final TagKey<Block> TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("creativedelight", "clip_break_overlay"));
	private static final float EPS = 1.0E-4F;

	private BreakOverlayClip() {
	}

	public static BakedModel wrap(BlockState state, BakedModel model) {
		return state.is(TAG) ? new Clipped(model) : model;
	}

	static boolean inside(BakedQuad quad) {
		int[] v = quad.getVertices();
		for (int vertex = 0; vertex < 4; vertex++) {
			for (int axis = 0; axis < 3; axis++) {
				float f = Float.intBitsToFloat(v[vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + axis]);
				if (f < -EPS || f > 1.0F + EPS) {
					return false;
				}
			}
		}
		return true;
	}

	private static final class Clipped extends BakedModelWrapper<BakedModel> {
		Clipped(BakedModel base) {
			super(base);
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data,
				@Nullable RenderType renderType) {
			List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
			List<BakedQuad> out = new ArrayList<>(quads.size());
			for (BakedQuad quad : quads) {
				if (inside(quad)) {
					out.add(quad);
				}
			}
			return out;
		}
	}
}
