package com.SaintAlfonzo.creativedelight.blockheads.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.blockheads.BlockheadsAdditionsMod;
import com.mojang.logging.LogUtils;
import net.blay09.mods.cookingforblockheads.block.FridgeBlock;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

/**
 * Cooking for Blockheads' 2-tall fridge reads as a full block (TODO cooking/kitchen #6, textures/art #1). CfB 13.3.4's
 * upper half stops at y 13, so decor on top floated 3 px above it. Our own model
 * {@code blockheads_additions:block/fridge_top_fill} is one box (x 1-15, z 1-15, y 13-16) textured with the white inside
 * of CfB's {@code fridge_side}, tintindex 0, so a dyed fridge's box takes the dye from CfB's block colour. Its quads are
 * added to the fridge's baked model for every {@code model_type=large_upper} state; CfB's own models are not touched
 * (until 2026-10-31 this was a copy of CfB's fridge_large_upper model in kubejs/assets, which an All Rights Reserved
 * file may not be).
 * <p>
 * CfB's fridge model is a Balm dynamic model, put in the model map in CfB's own {@code ModelEvent.BakingCompleted}
 * listener; this mod loads after CfB (mods.toml ordering), so its listener sees Balm's model and wraps it. The box
 * model is registered as an additional model, which also puts its texture on the block atlas.
 */
public final class FridgeTopFill {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation FILL = new ResourceLocation(BlockheadsAdditionsMod.MODID, "block/fridge_top_fill");

    private FridgeTopFill() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((ModelEvent.RegisterAdditional e) -> e.register(FILL));
        modBus.addListener(EventPriority.LOWEST, FridgeTopFill::wrap);
    }

    private static void wrap(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel fill = models.get(FILL);
        if (fill == null || fill == event.getModelManager().getMissingModel()) {
            LOGGER.warn("[blockheads_additions] {} did not load: the 2-tall fridge keeps CfB's height", FILL);
            return;
        }
        int wrapped = 0;
        for (BlockState state : ModBlocks.fridge.getStateDefinition().getPossibleStates()) {
            if (state.getValue(FridgeBlock.MODEL_TYPE) != FridgeBlock.FridgeModelType.LARGE_UPPER) continue;
            ResourceLocation key = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = models.get(key);
            if (model == null || model instanceof Filled) continue;
            models.put(key, new Filled(model, fill));
            wrapped++;
        }
        LOGGER.debug("[blockheads_additions] fridge top fill on {} states", wrapped);
    }

    /** CfB's fridge model plus the fill box, in the first render pass the fridge draws in. */
    private static final class Filled extends BakedModelWrapper<BakedModel> {
        private final BakedModel fill;

        Filled(BakedModel original, BakedModel fill) {
            super(original);
            this.fill = fill;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return with(originalModel.getQuads(state, side, rand), state, side, rand);
        }

        @Override
        @Nonnull
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
                                        @Nonnull ModelData data, @Nullable RenderType renderType) {
            List<BakedQuad> quads = originalModel.getQuads(state, side, rand, data, renderType);
            if (renderType != null && state != null) {
                ChunkRenderTypeSet types = originalModel.getRenderTypes(state, rand, data);
                if (types.isEmpty() || types.asList().get(0) != renderType) return quads;
            }
            return with(quads, state, side, rand);
        }

        private List<BakedQuad> with(List<BakedQuad> quads, @Nullable BlockState state, @Nullable Direction side,
                                     RandomSource rand) {
            List<BakedQuad> extra = fill.getQuads(state, side, rand);
            if (extra.isEmpty()) return quads;
            List<BakedQuad> out = new ArrayList<>(quads.size() + extra.size());
            out.addAll(quads);
            out.addAll(extra);
            return out;
        }
    }
}
