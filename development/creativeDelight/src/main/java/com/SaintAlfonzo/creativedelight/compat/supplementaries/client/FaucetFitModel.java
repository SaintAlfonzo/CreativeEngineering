package com.SaintAlfonzo.creativedelight.compat.supplementaries.client;

import com.SaintAlfonzo.creativedelight.compat.supplementaries.FaucetFit;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

/**
 * Supplementaries faucet model moved toward the block it drains ({@link FaucetFit#shift}, TODO cooking/kitchen #26):
 * a wrapper on every faucet block-state model that works out the shift from the neighbours when the chunk is built
 * ({@code getModelData}) and moves the quads by it. Supplementaries' own models are not touched. Client only,
 * registered on {@link ModelEvent.BakingCompleted} at the lowest priority when Supplementaries is loaded.
 */
public final class FaucetFitModel {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation FAUCET = new ResourceLocation("supplementaries", "faucet");
	private static final ModelProperty<double[]> SHIFT = new ModelProperty<>();
	/** Ints per vertex in the block format; x, y, z are the first three (floats). */
	private static final int STRIDE = 8;

	private FaucetFitModel() {}

	public static void wrap(ModelEvent.BakingCompleted event) {
		Block faucet = ForgeRegistries.BLOCKS.getValue(FAUCET);
		if (faucet == null || !FAUCET.equals(ForgeRegistries.BLOCKS.getKey(faucet))) return;
		Map<ResourceLocation, BakedModel> models = event.getModels();
		int wrapped = 0;
		for (BlockState state : faucet.getStateDefinition().getPossibleStates()) {
			ResourceLocation key = BlockModelShaper.stateToModelLocation(state);
			BakedModel model = models.get(key);
			if (model == null || model instanceof Shifted) continue;
			models.put(key, new Shifted(model));
			wrapped++;
		}
		LOGGER.debug("[creativedelight/supplementaries] faucet fit on {} states", wrapped);
	}

	private static final class Shifted extends BakedModelWrapper<BakedModel> {

		Shifted(BakedModel original) {
			super(original);
		}

		@Override
		@Nonnull
		public ModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData data) {
			ModelData base = super.getModelData(level, pos, state, data);
			double[] shift = FaucetFit.shift(level, pos, state);
			if (shift[0] == 0 && shift[1] == 0) return base;
			return base.derive().with(SHIFT, shift).build();
		}

		@Override
		@Nonnull
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
				@Nonnull ModelData data, @Nullable RenderType renderType) {
			double[] shift = data.get(SHIFT);
			if (shift == null) return originalModel.getQuads(state, side, rand, data, renderType);
			// moved quads are no longer flush with the block face: they draw with the unculled ones
			if (side != null) return List.of();
			List<BakedQuad> out = new ArrayList<>();
			for (BakedQuad quad : originalModel.getQuads(state, null, rand, data, renderType)) out.add(moved(quad, shift));
			for (Direction d : Direction.values()) {
				for (BakedQuad quad : originalModel.getQuads(state, d, rand, data, renderType)) out.add(moved(quad, shift));
			}
			return out;
		}

		private static BakedQuad moved(BakedQuad quad, double[] shift) {
			int[] v = quad.getVertices().clone();
			for (int i = 0; i + 2 < v.length; i += STRIDE) {
				v[i] = Float.floatToRawIntBits(Float.intBitsToFloat(v[i]) + (float) shift[0]);
				v[i + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(v[i + 2]) + (float) shift[1]);
			}
			return new BakedQuad(v, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
		}
	}
}
