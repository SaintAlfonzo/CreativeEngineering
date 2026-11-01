package net.satisfy.vinery.legacy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.ModList;
import net.satisfy.foundation.render.ClientUtil;

/**
 * Pack change: draws one plant in a Vinery flower box or flower pot. Supplementaries' special flower model when it has
 * one for the item, else the plant block (double plants: lower and upper half), else nothing.
 */
@OnlyIn(Dist.CLIENT)
public final class PlantRenderer {
    private static final boolean SUPPLEMENTARIES = ModList.get().isLoaded("supplementaries");

    private PlantRenderer() {
    }

    /** Draws the plant with its bottom at the current pose origin, as a block would be drawn. */
    public static void render(BlockItem item, PoseStack matrices, MultiBufferSource buffers, BlockEntity entity) {
        Level level = entity.getLevel();
        if (level == null) {
            return;
        }
        BlockState state = item.getBlock().defaultBlockState();
        if (SUPPLEMENTARIES) {
            ResourceLocation special = SupplementariesFlowerModels.get(item);
            if (special != null) {
                renderModel(special, state, matrices, buffers, ClientUtil.getLightLevel(level, entity.getBlockPos()));
                return;
            }
        }
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            ClientUtil.renderBlock(state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), matrices, buffers, entity);
            matrices.pushPose();
            matrices.translate(0.0, 1.0, 0.0);
            ClientUtil.renderBlock(state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), matrices, buffers, entity);
            matrices.popPose();
        } else {
            ClientUtil.renderBlock(state, matrices, buffers, entity);
        }
    }

    private static void renderModel(ResourceLocation id, BlockState state, PoseStack matrices, MultiBufferSource buffers, int light) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(id);
        int color = mc.getBlockColors().getColor(state, null, null, 0);
        float r = 1.0F, g = 1.0F, b = 1.0F;
        if (color != -1) {
            r = (color >> 16 & 255) / 255.0F;
            g = (color >> 8 & 255) / 255.0F;
            b = (color & 255) / 255.0F;
        }
        mc.getBlockRenderer().getModelRenderer().renderModel(matrices.last(), buffers.getBuffer(RenderType.cutout()), state, model,
                r, g, b, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
    }
}
