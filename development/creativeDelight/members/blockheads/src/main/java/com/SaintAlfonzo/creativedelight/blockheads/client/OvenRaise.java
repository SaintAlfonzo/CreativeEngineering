package com.SaintAlfonzo.creativedelight.blockheads.client;

import com.SaintAlfonzo.creativedelight.blockheads.BlockheadsAdditionsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.blay09.mods.cookingforblockheads.block.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooking for Blockheads' oven draws its cook surface at the top of the block (TODO cooking/kitchen, scratchpad
 * 2026-11-03). CfB 13.3.4's oven top sits at 15 px, so a pot or skillet on it floated 1 px above. CfB's models are All
 * Rights Reserved and are not copied or edited: their baked quads are moved at load time instead.
 * <ul>
 * <li>Every quad goes up 1 px, so the cook surface is at 16 px and the backsplash reaches 2 px into the block above.</li>
 * <li>The bottom plate (CfB's {@code oven_bottom} texture) and the downward faces at y 0 stay, so the oven still stands
 * on the floor.</li>
 * <li>Each side face that started at y 0 or 1 px (sides, back wall, door) gets a 1 px strip under it showing its own
 * bottom texel row, which fills the gap the move leaves.</li>
 * </ul>
 * The oven's block and item models are wrapped in the model map; the door and handle models Balm bakes on its own are
 * wrapped as the renderer draws them (CfbOvenRendererMixin, which also lifts the tools on top). The outline and
 * collision box are raised in CfbOvenShapeMixin.
 */
public final class OvenRaise {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float PIXEL = 1f / 16f;
    private static final float EPS = 1e-4f;
    private static final ResourceLocation BOTTOM_SPRITE = new ResourceLocation("cookingforblockheads", "block/oven_bottom");
    private static final Map<BakedModel, BakedModel> DOORS = Collections.synchronizedMap(new WeakHashMap<>());

    private OvenRaise() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST, OvenRaise::wrap);
    }

    /** The raised version of a door or handle model the oven renderer is about to draw. */
    public static BakedModel door(BakedModel model) {
        if (model == null || model instanceof Raised) return model;
        return DOORS.computeIfAbsent(model, Raised::new);
    }

    private static void wrap(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = event.getModelManager().getMissingModel();
        DOORS.clear();
        int wrapped = 0;
        for (BlockState state : ModBlocks.oven.getStateDefinition().getPossibleStates()) {
            if (wrapIn(models, BlockModelShaper.stateToModelLocation(state), missing)) wrapped++;
        }
        ResourceLocation id = new ResourceLocation("cookingforblockheads", "oven");
        if (wrapIn(models, new ModelResourceLocation(id, "inventory"), missing)) wrapped++;
        if (wrapped == 0) {
            LOGGER.warn("[{}] no oven model found: the oven keeps CfB's height", BlockheadsAdditionsMod.MODID);
        } else {
            LOGGER.debug("[{}] oven raised on {} models", BlockheadsAdditionsMod.MODID, wrapped);
        }
    }

    private static boolean wrapIn(Map<ResourceLocation, BakedModel> models, ResourceLocation key, BakedModel missing) {
        BakedModel model = models.get(key);
        if (model == null || model == missing || model instanceof Raised) return false;
        models.put(key, new Raised(model));
        return true;
    }

    /** A model with every quad run through {@link #raise}; the result is cached per state, side and render type. */
    private static final class Raised extends BakedModelWrapper<BakedModel> {
        private record Key(@Nullable BlockState state, @Nullable Direction side, @Nullable RenderType type) {
        }

        private final Map<Key, List<BakedQuad>> cache = new ConcurrentHashMap<>();

        Raised(BakedModel original) {
            super(original);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return cache.computeIfAbsent(new Key(state, side, null), k -> raise(originalModel.getQuads(state, side, rand)));
        }

        @Override
        @Nonnull
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
                                        @Nonnull ModelData data, @Nullable RenderType renderType) {
            return cache.computeIfAbsent(new Key(state, side, renderType),
                    k -> raise(originalModel.getQuads(state, side, rand, data, renderType)));
        }

        @Override
        public BakedModel applyTransform(ItemTransforms.TransformType type, PoseStack poseStack, boolean leftHand) {
            originalModel.applyTransform(type, poseStack, leftHand);
            return this;
        }
    }

    static List<BakedQuad> raise(List<BakedQuad> quads) {
        if (quads.isEmpty()) return quads;
        List<BakedQuad> out = new ArrayList<>(quads.size() + 8);
        for (BakedQuad quad : quads) {
            if (BOTTOM_SPRITE.equals(quad.getSprite().getName())) {
                out.add(quad);
                continue;
            }
            int[] v = quad.getVertices();
            int stride = v.length / 4;
            float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                float y = Float.intBitsToFloat(v[i * stride + 1]);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            if (quad.getDirection() == Direction.DOWN && minY < EPS) {
                out.add(quad);
                continue;
            }
            int[] moved = v.clone();
            for (int i = 0; i < 4; i++) {
                moved[i * stride + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(v[i * stride + 1]) + PIXEL);
            }
            out.add(copy(quad, moved));
            if (quad.getDirection().getAxis().isHorizontal() && minY <= PIXEL + EPS && maxY - minY > PIXEL + EPS) {
                int[] strip = bottomStrip(v, stride, minY, maxY);
                if (strip != null) out.add(copy(quad, strip));
            }
        }
        return out;
    }

    /**
     * The face's bottom 1 px: bottom vertices as they were, each top vertex pulled down to 1 px above its bottom
     * partner (same x and z), its texture coordinates pulled the same fraction, so the strip shows the bottom texel row.
     */
    @Nullable
    private static int[] bottomStrip(int[] v, int stride, float minY, float maxY) {
        int[] s = v.clone();
        float f = PIXEL / (maxY - minY);
        int uv = uvOffset();
        for (int i = 0; i < 4; i++) {
            int a = i * stride;
            if (Float.intBitsToFloat(v[a + 1]) - minY < EPS) continue;
            int b = -1;
            for (int j = 0; j < 4; j++) {
                int c = j * stride;
                if (Float.intBitsToFloat(v[c + 1]) - minY < EPS
                        && Math.abs(Float.intBitsToFloat(v[c]) - Float.intBitsToFloat(v[a])) < EPS
                        && Math.abs(Float.intBitsToFloat(v[c + 2]) - Float.intBitsToFloat(v[a + 2])) < EPS) {
                    b = c;
                    break;
                }
            }
            if (b < 0) return null;
            s[a + 1] = Float.floatToRawIntBits(minY + PIXEL);
            for (int k = uv; k < uv + 2; k++) {
                float bot = Float.intBitsToFloat(v[b + k]);
                s[a + k] = Float.floatToRawIntBits(bot + (Float.intBitsToFloat(v[a + k]) - bot) * f);
            }
        }
        return s;
    }

    /** Index of U within a vertex of the block format: position (3 ints), colour (1), then UV0. */
    private static int uvOffset() {
        return 4;
    }

    private static BakedQuad copy(BakedQuad quad, int[] vertices) {
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }
}
