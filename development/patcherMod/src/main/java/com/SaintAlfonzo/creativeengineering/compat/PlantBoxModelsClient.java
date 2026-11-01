package com.SaintAlfonzo.creativeengineering.compat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;

/** Client half of {@link PlantBoxModels}: the extra models and the flower box plant renderer. */
public final class PlantBoxModelsClient {

    private PlantBoxModelsClient() {}

    /** Client mod bus. */
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(PlantBoxModels.GLOW_BERRIES);
        event.register(PlantBoxModels.GLOW_LICHEN);
        event.register(PlantBoxModels.GLOW_LICHEN_POT);
    }

    /**
     * Draws {@code state}'s plant with its Supplementaries flower box model at the current pose. Returns false when the
     * plant has none (the caller draws the block as before). The upper half of a double plant draws nothing: the
     * special model covers the whole plant and is drawn with the lower half.
     */
    public static boolean renderSpecial(BlockState state, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation location = PlantBoxModels.specialModel(state.getBlock().asItem());
        if (location == null) return false;
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) return true;
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(location);
        int color;
        try {
            color = mc.getBlockColors().getColor(state, null, null, 0);
        } catch (RuntimeException e) {
            color = -1;
        }
        float r = 1, g = 1, b = 1;
        if (color != -1) {
            r = (color >> 16 & 255) / 255f;
            g = (color >> 8 & 255) / 255f;
            b = (color & 255) / 255f;
        }
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()),
                state, model, r, g, b, light, overlay, ModelData.EMPTY, RenderType.cutout());
        return true;
    }
}
