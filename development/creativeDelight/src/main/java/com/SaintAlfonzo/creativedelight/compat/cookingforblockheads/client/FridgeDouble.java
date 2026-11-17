package com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.CfbCompartment;
import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen.FridgePairs;
import com.mojang.logging.LogUtils;
import net.blay09.mods.cookingforblockheads.block.BlockKitchen;
import net.blay09.mods.cookingforblockheads.block.FridgeBlock;
import net.blay09.mods.cookingforblockheads.block.FridgeBlock.FridgeModelType;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.blay09.mods.cookingforblockheads.client.ModModels;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

/**
 * Double fridges drawn joined (TODO cooking/kitchen #36; pairing rules in {@code kitchen/FridgePairs}). On the joined
 * side of each fridge:
 * <ul>
 * <li>the body reaches the block edge (a 1 px side strip) and the gray door seal on that side is replaced by a white lip,
 * so no gap or seal shows between the two fridges;</li>
 * <li>the door reaches the block edge (a 1 px strip), which swings with the door.</li>
 * </ul>
 * The pieces are our own models {@code creativedelight:block/fridge_join_<small|lower|upper>_<body|door>} (plain boxes on
 * CfB's {@code fridge_side}, tintindex 0 so dyed fridges match), built for a joint at model -x and mirrored here for a
 * flipped door; CfB's models are not copied or edited. The body pieces are added by a wrapper on every fridge state model,
 * which reads the pairing into its model data per block; the door pieces by {@code CfbFridgeRendererMixin}, which draws
 * them in the door's own pose.
 */
public final class FridgeDouble {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ModelProperty<Direction> JOIN = new ModelProperty<>();
	private static final float EPS = 1e-4f;
	private static final float PX = 1f / 16f;
	private static final int STRIDE = 8;

	private enum Part {
		SMALL, LOWER, UPPER;

		ResourceLocation body() {
			return id("body");
		}

		ResourceLocation door() {
			return id("door");
		}

		private ResourceLocation id(String what) {
			return new ResourceLocation(CfbCompartment.NAMESPACE,
					"block/fridge_join_" + name().toLowerCase(java.util.Locale.ROOT) + "_" + what);
		}

		static Part of(FridgeModelType type) {
			return switch (type) {
				case LARGE_LOWER -> LOWER;
				case LARGE_UPPER -> UPPER;
				default -> SMALL;
			};
		}
	}

	private static final Map<Part, BakedModel> BODY = new EnumMap<>(Part.class);
	private static final Map<Part, BakedModel> DOOR = new EnumMap<>(Part.class);
	/** Body pieces placed for a facing and door side, keyed {@code part, facing, flipped}. */
	private static final Map<String, List<BakedQuad>> PLACED = new ConcurrentHashMap<>();
	/** Door models with their joint strip, keyed by CfB's door model. */
	private static final Map<BakedModel, BakedModel> DOORS = new ConcurrentHashMap<>();

	private FridgeDouble() {
	}

	public static void register(IEventBus modBus) {
		modBus.addListener((ModelEvent.RegisterAdditional e) -> {
			for (Part p : Part.values()) {
				e.register(p.body());
				e.register(p.door());
			}
		});
	}

	/** Called by FridgeTopFill after the top box is on, so the wrapper sees the filled model. */
	static void wrap(ModelEvent.BakingCompleted event) {
		Map<ResourceLocation, BakedModel> models = event.getModels();
		BakedModel missing = event.getModelManager().getMissingModel();
		BODY.clear();
		DOOR.clear();
		PLACED.clear();
		DOORS.clear();
		for (Part p : Part.values()) {
			BakedModel body = models.get(p.body());
			BakedModel door = models.get(p.door());
			if (body == null || body == missing || door == null || door == missing) {
				LOGGER.warn("[{}] {} or {} did not load: double fridges are drawn apart", CfbCompartment.MODID,
						p.body(), p.door());
				BODY.clear();
				DOOR.clear();
				return;
			}
			BODY.put(p, body);
			DOOR.put(p, door);
		}
		int wrapped = 0;
		for (BlockState state : ModBlocks.fridge.getStateDefinition().getPossibleStates()) {
			ResourceLocation key = BlockModelShaper.stateToModelLocation(state);
			BakedModel model = models.get(key);
			if (model == null || model == missing || model instanceof Joined) continue;
			models.put(key, new Joined(model));
			wrapped++;
		}
		LOGGER.debug("[{}] double fridge joints on {} states", CfbCompartment.MODID, wrapped);
	}

	/**
	 * The door model CfB's renderer is about to draw at {@code pos} with the fridge's lower block state (the upper door
	 * half one block up), with the joint strip when the fridge is joined; CfB's door model otherwise.
	 */
	public static BakedModel door(BakedModel model, BlockAndTintGetter level, BlockState state, BlockPos pos) {
		if (model == null || DOOR.isEmpty() || !state.is(ModBlocks.fridge)) return model;
		boolean upper = model == ModModels.fridgeDoorLargeUpper.get() || model == ModModels.fridgeDoorLargeUpperFlipped.get();
		if (FridgePairs.joinSide(level, upper ? pos.below() : pos, state) == null) return model;
		BakedModel cached = DOORS.get(model);
		if (cached != null) return cached;
		Part part;
		boolean flipped;
		if (model == ModModels.fridgeDoor.get()) {
			part = Part.SMALL;
			flipped = false;
		} else if (model == ModModels.fridgeDoorFlipped.get()) {
			part = Part.SMALL;
			flipped = true;
		} else if (model == ModModels.fridgeDoorLargeLower.get()) {
			part = Part.LOWER;
			flipped = false;
		} else if (model == ModModels.fridgeDoorLargeLowerFlipped.get()) {
			part = Part.LOWER;
			flipped = true;
		} else if (model == ModModels.fridgeDoorLargeUpper.get()) {
			part = Part.UPPER;
			flipped = false;
		} else if (model == ModModels.fridgeDoorLargeUpperFlipped.get()) {
			part = Part.UPPER;
			flipped = true;
		} else {
			return model;
		}
		List<BakedQuad> strip = place(quads(DOOR.get(part)), flipped, null);
		return DOORS.computeIfAbsent(model, m -> new WithDoorStrip(m, strip));
	}

	private static List<BakedQuad> quads(BakedModel model) {
		return model.getQuads(null, null, RandomSource.create(42L), ModelData.EMPTY, null);
	}

	/** The joint pieces of a fridge block (model -x joint), mirrored for a flipped door, turned to the facing. */
	private static List<BakedQuad> bodyPieces(FridgeModelType type, Direction facing, boolean flipped) {
		Part part = Part.of(type);
		return PLACED.computeIfAbsent(part + "," + facing + "," + flipped,
				k -> place(quads(BODY.get(part)), flipped, facing));
	}

	/**
	 * Quads moved from the model frame (front at north) into place: mirrored in x when {@code mirror}, then turned to
	 * {@code facing} (null: left in the model frame, for the door, which the renderer turns).
	 */
	private static List<BakedQuad> place(List<BakedQuad> quads, boolean mirror, @Nullable Direction facing) {
		List<BakedQuad> out = new ArrayList<>(quads.size());
		Direction ax = facing == null ? Direction.EAST : facing.getClockWise();
		Direction az = facing == null ? Direction.SOUTH : facing.getOpposite();
		for (BakedQuad q : quads) {
			int[] v = q.getVertices();
			int[] n = new int[v.length];
			for (int i = 0; i < 4; i++) {
				int src = (mirror ? 3 - i : i) * STRIDE;
				int dst = i * STRIDE;
				System.arraycopy(v, src, n, dst, STRIDE);
				float x = Float.intBitsToFloat(v[src]);
				float z = Float.intBitsToFloat(v[src + 2]);
				if (mirror) x = 1f - x;
				n[dst] = Float.floatToRawIntBits(0.5f + ax.getStepX() * (x - 0.5f) + az.getStepX() * (z - 0.5f));
				n[dst + 2] = Float.floatToRawIntBits(0.5f + ax.getStepZ() * (x - 0.5f) + az.getStepZ() * (z - 0.5f));
				int packed = v[src + 7];
				int nx = (byte) (packed & 0xFF);
				int nz = (byte) ((packed >> 16) & 0xFF);
				if (mirror) nx = -nx;
				int wx = ax.getStepX() * nx + az.getStepX() * nz;
				int wz = ax.getStepZ() * nx + az.getStepZ() * nz;
				n[dst + 7] = (packed & 0xFF00FF00) | (wx & 0xFF) | ((wz & 0xFF) << 16);
			}
			Direction d = q.getDirection();
			if (mirror && d.getAxis() == Direction.Axis.X) d = d.getOpposite();
			d = switch (d) {
				case EAST -> ax;
				case WEST -> ax.getOpposite();
				case SOUTH -> az;
				case NORTH -> az.getOpposite();
				default -> d;
			};
			out.add(new BakedQuad(n, q.getTintIndex(), d, q.getSprite(), q.isShade()));
		}
		return List.copyOf(out);
	}

	/** True for a quad of CfB's door seal on the joined side ({@code x 1.5-2.5, z 2-3} px in the joint's frame). */
	private static boolean isJoinSeal(BakedQuad q, Direction facing, boolean flipped) {
		Direction ax = facing.getClockWise();
		Direction az = facing.getOpposite();
		float lo = (flipped ? 13.5f : 1.5f) * PX - EPS;
		float hi = (flipped ? 14.5f : 2.5f) * PX + EPS;
		int[] v = q.getVertices();
		for (int i = 0; i < 4; i++) {
			float wx = Float.intBitsToFloat(v[i * STRIDE]) - 0.5f;
			float wz = Float.intBitsToFloat(v[i * STRIDE + 2]) - 0.5f;
			float x = 0.5f + ax.getStepX() * wx + ax.getStepZ() * wz;
			float z = 0.5f + az.getStepX() * wx + az.getStepZ() * wz;
			if (x < lo || x > hi || z < 2 * PX - EPS || z > 3 * PX + EPS) return false;
		}
		return true;
	}

	/** A fridge state model that adds the joint pieces when its block is joined (read per block into model data). */
	private static final class Joined extends BakedModelWrapper<BakedModel> {
		Joined(BakedModel original) {
			super(original);
		}

		@Override
		@Nonnull
		public ModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state,
									  @Nonnull ModelData data) {
			ModelData base = originalModel.getModelData(level, pos, state, data);
			Direction join = FridgePairs.joinSide(level, pos, state);
			return join == null ? base : base.derive().with(JOIN, join).build();
		}

		@Override
		@Nonnull
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
										@Nonnull ModelData data, @Nullable RenderType renderType) {
			List<BakedQuad> quads = originalModel.getQuads(state, side, rand, data, renderType);
			Direction join = data.get(JOIN);
			if (join == null || state == null || !state.is(ModBlocks.fridge) || BODY.isEmpty()) return quads;
			if (renderType != null) {
				ChunkRenderTypeSet types = originalModel.getRenderTypes(state, rand, data);
				if (types.isEmpty() || types.asList().get(0) != renderType) return quads;
			}
			Direction facing = state.getValue(BlockKitchen.FACING);
			boolean flipped = join == facing.getClockWise();
			List<BakedQuad> out = new ArrayList<>(quads.size() + 24);
			for (BakedQuad q : quads) {
				if (!isJoinSeal(q, facing, flipped)) out.add(q);
			}
			if (side == null) out.addAll(bodyPieces(state.getValue(FridgeBlock.MODEL_TYPE), facing, flipped));
			return out;
		}
	}

	/** One of CfB's door models plus its joint strip, in the door's model frame. */
	private static final class WithDoorStrip extends BakedModelWrapper<BakedModel> {
		private final List<BakedQuad> strip;

		WithDoorStrip(BakedModel original, List<BakedQuad> strip) {
			super(original);
			this.strip = strip;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return with(originalModel.getQuads(state, side, rand), side);
		}

		@Override
		@Nonnull
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
										@Nonnull ModelData data, @Nullable RenderType renderType) {
			return with(originalModel.getQuads(state, side, rand, data, renderType), side);
		}

		private List<BakedQuad> with(List<BakedQuad> quads, @Nullable Direction side) {
			if (side != null) return quads;
			List<BakedQuad> out = new ArrayList<>(quads.size() + strip.size());
			out.addAll(quads);
			out.addAll(strip);
			return out;
		}
	}
}
