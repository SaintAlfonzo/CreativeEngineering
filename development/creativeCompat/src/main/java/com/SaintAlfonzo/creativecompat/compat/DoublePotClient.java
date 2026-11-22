// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.compat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Draws the plants of a {@link DoublePotBlock}. The block's model (two pots and their dirt) is wrapped at bake time;
 * for each pot the wrapper adds the quads of its plant's {@code potted_*} block model minus those textured like the
 * empty vanilla pot (as {@link BotanistPotClient} does), moved from the vanilla pot's center onto that pot: pot B
 * also {@link DoublePot#TALL_RISE_PX} px up. Tint indices move up by 1000 (pot A) or 2000 (pot B) and go to the
 * potted block's own color handler.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DoublePotClient {

	static final int TINT_A = 1000;
	static final int TINT_B = 2000;
	/** Vanilla pot center in block pixels. */
	private static final float VANILLA_CENTER_PX = 8f;

	private DoublePotClient() {}

	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		Block block = DoublePot.BLOCK.get();
		Map<ResourceLocation, BakedModel> models = event.getModels();
		BakedModel emptyPot = models.get(BlockModelShaper.stateToModelLocation(Blocks.FLOWER_POT.defaultBlockState()));
		Set<ResourceLocation> potSprites = new HashSet<>();
		if (emptyPot != null) {
			RandomSource random = RandomSource.create();
			for (Direction side : sidesAndNull()) {
				for (BakedQuad quad : emptyPot.getQuads(Blocks.FLOWER_POT.defaultBlockState(), side, random, ModelData.EMPTY, null)) {
					potSprites.add(quad.getSprite().getName());
				}
			}
		}
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			ResourceLocation location = BlockModelShaper.stateToModelLocation(state);
			BakedModel base = models.get(location);
			if (base == null || base instanceof Wrapped) continue;
			models.put(location, new Wrapped(base, potSprites));
		}
	}

	@SubscribeEvent
	public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
		event.register((state, level, pos, tint) -> {
			if (tint < TINT_A || level == null || pos == null) return -1;
			if (!(level.getBlockEntity(pos) instanceof DoublePotBlockEntity pot)) return -1;
			boolean b = tint >= TINT_B;
			Block potted = BotanistPot.pottedFor(pot.plant(b));
			if (potted == null) return -1;
			return Minecraft.getInstance().getBlockColors().getColor(potted.defaultBlockState(), level, pos,
					tint - (b ? TINT_B : TINT_A));
		}, DoublePot.BLOCK.get());
	}

	private static Transformation offset(float centerPx, float risePx) {
		float d = (centerPx - VANILLA_CENTER_PX) / 16f;
		return new Transformation(new Vector3f(d, risePx / 16f, d), null, null, null);
	}

	private static Direction[] sidesAndNull() {
		Direction[] sides = new Direction[7];
		System.arraycopy(Direction.values(), 0, sides, 0, 6);
		return sides;
	}

	/** One pot's plant: where it goes and its quad cache. */
	private static final class Slot {
		final ModelProperty<Block> property;
		final IQuadTransformer transformer;
		final int tintOffset;
		final Map<Block, Map<RenderType, List<BakedQuad>>> byType = new ConcurrentHashMap<>();
		final Map<Block, List<BakedQuad>> allTypes = new ConcurrentHashMap<>();

		Slot(ModelProperty<Block> property, Transformation placement, int tintOffset) {
			this.property = property;
			this.transformer = QuadTransformers.applying(placement);
			this.tintOffset = tintOffset;
		}
	}

	private static final class Wrapped extends BakedModelWrapper<BakedModel> {

		private final Set<ResourceLocation> potSprites;
		private final Slot[] slots;

		Wrapped(BakedModel base, Set<ResourceLocation> potSprites) {
			super(base);
			this.potSprites = potSprites;
			this.slots = new Slot[]{
					new Slot(DoublePotBlockEntity.POTTED_A, offset(DoublePot.POT_A_CENTER_PX, 0), TINT_A),
					new Slot(DoublePotBlockEntity.POTTED_B, offset(DoublePot.POT_B_CENTER_PX, DoublePot.TALL_RISE_PX), TINT_B)};
		}

		@Override
		public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
			ChunkRenderTypeSet out = super.getRenderTypes(state, rand, data);
			for (Slot slot : slots) {
				Block potted = data.get(slot.property);
				if (potted != null) {
					out = ChunkRenderTypeSet.union(out, plantModel(potted).getRenderTypes(potted.defaultBlockState(), rand, ModelData.EMPTY));
				}
			}
			return out;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
				ModelData data, @Nullable RenderType renderType) {
			List<BakedQuad> own = super.getQuads(state, side, rand, data, renderType);
			if (side != null) return own;
			List<BakedQuad> out = null;
			for (Slot slot : slots) {
				Block potted = data.get(slot.property);
				if (potted == null) continue;
				List<BakedQuad> plant = plantQuads(slot, potted, renderType);
				if (plant.isEmpty()) continue;
				if (out == null) out = new ArrayList<>(own);
				out.addAll(plant);
			}
			return out == null ? own : out;
		}

		private static BakedModel plantModel(Block potted) {
			if (potted == Blocks.GLOW_LICHEN) return Minecraft.getInstance().getModelManager().getModel(PlantBoxModels.GLOW_LICHEN_POT);
			return Minecraft.getInstance().getBlockRenderer().getBlockModel(potted.defaultBlockState());
		}

		private List<BakedQuad> plantQuads(Slot slot, Block potted, @Nullable RenderType renderType) {
			// ConcurrentHashMap takes no null keys: getQuads(..., null) (every render type) has its own cache.
			if (renderType == null) return slot.allTypes.computeIfAbsent(potted, k -> buildQuads(slot, k, null));
			return slot.byType.computeIfAbsent(potted, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(renderType, type -> buildQuads(slot, potted, type));
		}

		private List<BakedQuad> buildQuads(Slot slot, Block potted, @Nullable RenderType renderType) {
			BlockState pottedState = potted.defaultBlockState();
			BakedModel model = plantModel(potted);
			RandomSource random = RandomSource.create(42L);
			if (renderType != null && !model.getRenderTypes(pottedState, random, ModelData.EMPTY).contains(renderType)) {
				return List.of();
			}
			List<BakedQuad> out = new ArrayList<>();
			for (Direction face : sidesAndNull()) {
				random.setSeed(42L);
				for (BakedQuad quad : model.getQuads(pottedState, face, random, ModelData.EMPTY, renderType)) {
					if (potSprites.contains(quad.getSprite().getName())) continue;
					BakedQuad moved = slot.transformer.process(quad);
					int tint = moved.getTintIndex() >= 0 ? moved.getTintIndex() + slot.tintOffset : -1;
					out.add(new BakedQuad(moved.getVertices(), tint, quad.getDirection(), moved.getSprite(), moved.isShade()));
				}
			}
			return List.copyOf(out);
		}
	}
}
