// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.surface;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Leaf pile models (plan_owner_notes_1119.md rulings 3 and 5), client, at model bake:
 * <ul>
 * <li>Inside faces ({@code leafInsideFaces}): every face of a leaf pile (Immersive Weathering piles, generated modded
 * ones included; Twigs bamboo leaves; the full bamboo leaves) gets a reversed copy facing into the block, inset by
 * {@link #INSET} so it never shares a plane with a neighbor's face. The copy sits in the same cull list as its source,
 * so it is hidden exactly when the outside face is. A head inside a pile sees the pile's leaves.</li>
 * <li>Tall pile texture ({@code leafTallTexture}): IW's tall piles (12 to 16 px) use a "heavy" texture with no
 * see-through pixel; their quads are moved onto the pile's own medium texture ({@code heavy_} -> {@code medium_} in
 * the sprite name) or the leaves block's texture, UVs remapped. No texture file is made. A missing medium sprite
 * falls back to the tree's, then to the heavy one.</li>
 * </ul>
 * Pile blocks are found by class and id, not by tag (tags are not bound at bake).
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LeafPileModels {

	/** What the tall IW piles are drawn with. */
	public enum TallTexture { MEDIUM, TREE, HEAVY }

	/** Inward offset of an inside face, blocks (1/512). */
	static final float INSET = 1.0F / 512.0F;
	private static final String HEAVY = "heavy_";

	private LeafPileModels() {}

	@SubscribeEvent
	public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		if (!CompatConfig.SPEC.isLoaded()) return;
		boolean inside = CompatConfig.SURFACE.passthrough.leafInsideFaces.get();
		TallTexture tall = CompatConfig.SURFACE.passthrough.leafTallTexture.get();
		Map<ResourceLocation, BakedModel> models = event.getModels();
		TextureAtlas atlas = event.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
		Map<BakedModel, BakedModel> wrapped = new IdentityHashMap<>();
		for (Block block : ForgeRegistries.BLOCKS) {
			Block tree = treeOf(block);
			boolean iw = tree != null;
			if (!iw && !FallingLayers.isBambooLeaves(block.defaultBlockState())) continue;
			TextureAtlasSprite treeSprite = null;
			if (iw && tall != TallTexture.HEAVY) {
				BakedModel treeModel = models.get(BlockModelShaper.stateToModelLocation(tree.defaultBlockState()));
				if (treeModel != null) treeSprite = treeModel.getParticleIcon(ModelData.EMPTY);
			}
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				ResourceLocation location = BlockModelShaper.stateToModelLocation(state);
				BakedModel base = models.get(location);
				if (base == null || base instanceof Pile) continue;
				TextureAtlasSprite tSprite = treeSprite;
				BakedModel w = wrapped.computeIfAbsent(base,
						b -> new Pile(b, inside, iw ? tall : TallTexture.HEAVY, atlas, tSprite));
				models.put(location, w);
			}
		}
	}

	/** The leaves block of an IW leaf pile, else null (IW classes load only with IW present). */
	@Nullable
	private static Block treeOf(Block block) {
		if (!ModList.get().isLoaded("immersive_weathering")) return null;
		return Iw.tree(block);
	}

	private static final class Iw {
		@Nullable
		static Block tree(Block block) {
			if (block instanceof com.ordana.immersive_weathering.blocks.LeafPileBlock lp && lp.getLeafType() != null
					&& lp.getLeafType().leaves != null) {
				return lp.getLeafType().leaves;
			}
			return null;
		}
	}

	// ---- quads ----

	/** The see-through sprite for a heavy one, or the sprite itself. */
	static TextureAtlasSprite tallSprite(TextureAtlasSprite sprite, TallTexture mode, TextureAtlas atlas,
										  @Nullable TextureAtlasSprite tree) {
		ResourceLocation name = sprite.getName();
		int at = name.getPath().lastIndexOf('/') + 1;
		if (mode == TallTexture.HEAVY || !name.getPath().startsWith(HEAVY, at)) return sprite;
		if (mode == TallTexture.MEDIUM) {
			ResourceLocation medium = new ResourceLocation(name.getNamespace(),
					name.getPath().substring(0, at) + "medium_" + name.getPath().substring(at + HEAVY.length()));
			TextureAtlasSprite m = atlas.getSprite(medium);
			if (!MissingTextureAtlasSprite.getLocation().equals(m.getName())) return m;
		}
		if (tree != null && !MissingTextureAtlasSprite.getLocation().equals(tree.getName())) return tree;
		return sprite;
	}

	/** The quad moved onto another sprite, UVs remapped. */
	static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite to) {
		TextureAtlasSprite from = quad.getSprite();
		if (from == to) return quad;
		int[] v = quad.getVertices().clone();
		for (int i = 0; i < 4; i++) {
			float u = getFloat(v, i, IQuadTransformer.UV0), w = getFloat(v, i, IQuadTransformer.UV0 + 1);
			setFloat(v, i, IQuadTransformer.UV0, to.getU(from.getUOffset(u)));
			setFloat(v, i, IQuadTransformer.UV0 + 1, to.getV(from.getVOffset(w)));
		}
		return new BakedQuad(v, quad.getTintIndex(), quad.getDirection(), to, quad.isShade());
	}

	/** The quad seen from behind: vertices reversed, normal flipped, moved {@link #INSET} along the new facing. */
	static BakedQuad inside(BakedQuad quad) {
		int[] src = quad.getVertices();
		int[] v = new int[src.length];
		int stride = IQuadTransformer.STRIDE;
		for (int i = 0; i < 4; i++) {
			System.arraycopy(src, (3 - i) * stride, v, i * stride, stride);
		}
		Direction face = quad.getDirection().getOpposite();
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 3; k++) {
				int axis = k == 0 ? face.getStepX() : k == 1 ? face.getStepY() : face.getStepZ();
				if (axis != 0) setFloat(v, i, IQuadTransformer.POSITION + k, getFloat(v, i, IQuadTransformer.POSITION + k) + axis * INSET);
			}
			int at = i * stride + IQuadTransformer.NORMAL;
			int n = v[at];
			int nx = -(byte) (n & 0xFF), ny = -(byte) (n >> 8 & 0xFF), nz = -(byte) (n >> 16 & 0xFF);
			v[at] = (n & 0xFF000000) | (nz & 0xFF) << 16 | (ny & 0xFF) << 8 | (nx & 0xFF);
		}
		return new BakedQuad(v, quad.getTintIndex(), face, quad.getSprite(), quad.isShade());
	}

	private static float getFloat(int[] v, int vertex, int offset) {
		return Float.intBitsToFloat(v[vertex * IQuadTransformer.STRIDE + offset]);
	}

	private static void setFloat(int[] v, int vertex, int offset, float value) {
		v[vertex * IQuadTransformer.STRIDE + offset] = Float.floatToRawIntBits(value);
	}

	private static final class Pile extends BakedModelWrapper<BakedModel> {

		private final boolean inside;
		private final TallTexture tall;
		private final TextureAtlas atlas;
		@Nullable
		private final TextureAtlasSprite tree;
		/** Keyed by side (null as 6) and render type (null as all): pile quads do not vary with position. */
		private final Map<Integer, Map<Object, List<BakedQuad>>> cache = new ConcurrentHashMap<>();
		private static final Object ALL_TYPES = new Object();

		Pile(BakedModel base, boolean inside, TallTexture tall, TextureAtlas atlas, @Nullable TextureAtlasSprite tree) {
			super(base);
			this.inside = inside;
			this.tall = tall;
			this.atlas = atlas;
			this.tree = tree;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
										ModelData data, @Nullable RenderType renderType) {
			int sideKey = side == null ? 6 : side.ordinal();
			Object typeKey = renderType == null ? ALL_TYPES : renderType;
			return cache.computeIfAbsent(sideKey, k -> new ConcurrentHashMap<>()).computeIfAbsent(typeKey, k -> {
				List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
				List<BakedQuad> out = new ArrayList<>(quads.size() * 2);
				for (BakedQuad q : quads) {
					BakedQuad r = retexture(q, tallSprite(q.getSprite(), tall, atlas, tree));
					out.add(r);
					if (inside) out.add(inside(r));
				}
				return List.copyOf(out);
			});
		}
	}
}
