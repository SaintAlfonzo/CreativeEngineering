package net.satisfy.vinery.client.render.block.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import net.satisfy.foundation.render.ClientUtil;
import net.satisfy.foundation.storage.StorageBlockEntity;
import net.satisfy.foundation.storage.StorageTypeRenderer;
import net.satisfy.vinery.client.util.ExtraModels;
import net.satisfy.vinery.core.block.BottleStorageBlock;
import net.satisfy.vinery.core.block.WineBottleBlock;

import java.util.Map;
import java.util.WeakHashMap;

public class BottleRenderer implements StorageTypeRenderer {
    private static final long INSERT_ANIMATION_MS = 250;
    private static final float INSERT_DISTANCE = 0.4f;

    /** A rack slot is the small bottle template's length, 15 px. */
    private static final float SLOT_LENGTH = 15f / 16f;
    /**
     * alcohol.md 10.17: a box look in the 4x rack is drawn at most this big, so the 8 px wide Strad shape comes out 6 px
     * and the diagonal neighbours (0.35 / 0.33 apart) don't overlap.
     */
    private static final float FOUR_RACK_BOX_SCALE = 0.75f;
    /** Bottle model -> its scale in a slot, measured once per baked model (a resource reload bakes new ones). */
    private static final Map<BakedModel, Float> FIT_SCALES = new WeakHashMap<>();
    private static final Direction[] SIDES = {null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private final BottleStorageBlock.Layout layout;
    private final float[][] offsets;
    private final Map<StorageBlockEntity, long[]> insertTimes = new WeakHashMap<>();

    public BottleRenderer(BottleStorageBlock.Layout layout) {
        this.layout = layout;
        this.offsets = layout.offsets;
    }

    @Override
    public void render(StorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {
        matrices.translate(-0.13, 0.335, 0.125);
        matrices.scale(0.9f, 0.9f, 0.9f);

        long now = Util.getMillis();
        boolean firstRender = !insertTimes.containsKey(entity);
        long[] times = insertTimes.computeIfAbsent(entity, e -> new long[offsets.length]);

        for (int i = 0; i < itemStacks.size() && i < offsets.length; i++) {
            ItemStack stack = itemStacks.get(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
                times[i] = 0;
                continue;
            }
            if (times[i] == 0) times[i] = firstRender ? now - INSERT_ANIMATION_MS : now;

            float progress = Math.min(1f, (now - times[i]) / (float) INSERT_ANIMATION_MS);
            float eased = 1f - (1f - progress) * (1f - progress) * (1f - progress);

            matrices.pushPose();
            matrices.translate(offsets[i][0], offsets[i][1], -INSERT_DISTANCE * (1f - eased));
            matrices.mulPose(Vector3f.XN.rotationDegrees(90f));
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.hasProperty(WineBottleBlock.FAKE_MODEL)) {
                state = state.setValue(WineBottleBlock.FAKE_MODEL, false);
            }
            // alcohol.md 10.17: a medium wine in the 4x rack lies as its wine box look, smaller (the wide ones overlapped)
            if (layout == BottleStorageBlock.Layout.FOUR) {
                renderLying(state, matrices, vertexConsumers, entity, FOUR_RACK_BOX_SCALE, ExtraModels.BOX);
            } else {
                renderLying(state, matrices, vertexConsumers, entity, 1f);
            }
            matrices.popPose();
        }
    }

    /**
     * Draws the first of the wine's variant models (ExtraModels kinds) it has, else its own model. alcohol.md 10.12:
     * either way a bottle longer than the slot is shrunk about its own axis so it doesn't poke out. A variant model is
     * also drawn no bigger than {@code variantScale} (10.17).
     */
    static void renderLying(BlockState state, PoseStack matrices, MultiBufferSource buffers, StorageBlockEntity entity, float variantScale, String... kinds) {
        Level level = entity.getLevel();
        if (level == null) {
            return;
        }
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = null;
        for (String kind : kinds) {
            model = ExtraModels.bottleVariant(kind, state.getBlock());
            if (model != null) break;
        }
        float maxScale = model == null ? 1f : variantScale;
        if (model == null) {
            model = dispatcher.getBlockModel(state);
        }
        fitToSlot(matrices, state, model, maxScale);
        // as BlockRenderDispatcher.renderSingleBlock, for a model the block state doesn't name
        int light = ClientUtil.getLightLevel(level, entity.getBlockPos());
        for (RenderType type : model.getRenderTypes(state, RandomSource.create(42L), ModelData.EMPTY)) {
            dispatcher.getModelRenderer().renderModel(matrices.last(), buffers.getBuffer(RenderTypeHelper.getEntityRenderType(type, false)),
                    state, model, 1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, type);
        }
    }

    private static void fitToSlot(PoseStack matrices, BlockState state, BakedModel model, float maxScale) {
        float scale = Math.min(maxScale, fitScale(state, model));
        if (scale < 1f) {
            matrices.translate(0.5, 0, 0.5);
            matrices.scale(scale, scale, scale);
            matrices.translate(-0.5, 0, -0.5);
        }
    }

    /** Slot length / the model's height (its length lying down), at most 1. */
    private static float fitScale(BlockState state, BakedModel model) {
        return FIT_SCALES.computeIfAbsent(model, m -> {
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;
            RandomSource random = RandomSource.create(42L);
            int stride = DefaultVertexFormat.BLOCK.getIntegerSize();
            for (Direction side : SIDES) {
                for (BakedQuad quad : m.getQuads(state, side, random, ModelData.EMPTY, null)) {
                    int[] vertices = quad.getVertices();
                    for (int v = 0; v + stride <= vertices.length; v += stride) {
                        float y = Float.intBitsToFloat(vertices[v + 1]);
                        min = Math.min(min, y);
                        max = Math.max(max, y);
                    }
                }
            }
            float height = max - min;
            return height > SLOT_LENGTH ? SLOT_LENGTH / height : 1f;
        });
    }
}
