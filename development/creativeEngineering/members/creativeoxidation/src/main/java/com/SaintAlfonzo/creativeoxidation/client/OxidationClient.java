// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation.client;

import com.SaintAlfonzo.creativeoxidation.OxidationItemData;
import com.SaintAlfonzo.creativeoxidation.OxidationStage;
import com.SaintAlfonzo.creativeoxidation.OxidationTargets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Client half: wraps the baked block and item models of the target ids (Forge 43's ModelEvent.BakingCompleted stands
 * in for 1.20's ModifyBakingResult; the mod loads after Create, Create Deco and Design n' Decor, so the wrapper sits
 * outside their model swaps and sees the final quads) and registers the staged partial models.
 */
public final class OxidationClient {
	private OxidationClient() {
	}

	public static void init(IEventBus modBus) {
		OxidizedPartialModels.init();
		modBus.addListener(EventPriority.LOWEST, OxidationClient::onBakingCompleted);
	}

	private static void onBakingCompleted(ModelEvent.BakingCompleted event) {
		QuadRemapper.clearCaches();
		OxidizedModel.STAGE_MODELS.clear();
		// block ids plus the item ids of their items (createdeco:copper_coinstack_block drops createdeco:copper_coinstack)
		Set<String> ids = new HashSet<>(OxidationTargets.BLOCK_IDS);
		for (String blockId : OxidationTargets.BLOCK_IDS) {
			ResourceLocation id = ResourceLocation.tryParse(blockId);
			if (id == null || !Registry.BLOCK.containsKey(id)) continue;
			Item item = Registry.BLOCK.get(id).asItem();
			ResourceLocation itemId = Registry.ITEM.getKey(item);
			if (itemId != null && !"minecraft:air".equals(itemId.toString())) ids.add(itemId.toString());
		}
		Map<ResourceLocation, BakedModel> models = event.getModels();
		models.replaceAll((location, model) -> {
			if (model instanceof OxidizedModel || !ids.contains(location.getNamespace() + ":" + location.getPath())) return model;
			return new OxidizedModel(model, null);
		});
	}

	/** A target's model: block quads follow the state's stage, item quads the item's stage (or a forced one). */
	static final class OxidizedModel extends BakedModelWrapper<BakedModel> {
		static final Map<StageModelKey, BakedModel> STAGE_MODELS = new ConcurrentHashMap<>();

		private final OxidationStage forcedStage;
		private final ItemOverrides overrides;

		OxidizedModel(BakedModel delegate, OxidationStage forcedStage) {
			super(delegate);
			this.forcedStage = forcedStage;
			this.overrides = forcedStage == null ? new OxidizedItemOverrides(delegate) : delegate.getOverrides();
		}

		private List<BakedQuad> remap(List<BakedQuad> quads, BlockState state) {
			return forcedStage == null ? QuadRemapper.remap(quads, state) : QuadRemapper.remap(quads, forcedStage);
		}

		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
			return remap(originalModel.getQuads(state, side, random), state);
		}

		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType renderType) {
			return remap(originalModel.getQuads(state, side, random, data, renderType), state);
		}

		@Override
		public ItemOverrides getOverrides() {
			return overrides;
		}

		/** The wrapper itself is the pass, so item quads come through remap (BakedModelWrapper would hand back the original). */
		@Override
		public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
			return List.of(this);
		}

		private static final class OxidizedItemOverrides extends ItemOverrides {
			private final BakedModel baseModel;

			private OxidizedItemOverrides(BakedModel baseModel) {
				this.baseModel = baseModel;
			}

			@Override
			public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
				BakedModel resolved = baseModel.getOverrides().resolve(baseModel, stack, level, entity, seed);
				if (resolved == null) resolved = baseModel;
				OxidationStage stage = OxidationItemData.read(stack).map(OxidationItemData.StoredState::stage).orElse(OxidationStage.UNAFFECTED);
				if (stage == OxidationStage.UNAFFECTED) return resolved;
				return STAGE_MODELS.computeIfAbsent(new StageModelKey(resolved, stage), key -> new OxidizedModel(key.model(), key.stage()));
			}
		}

		record StageModelKey(BakedModel model, OxidationStage stage) {
		}
	}
}
