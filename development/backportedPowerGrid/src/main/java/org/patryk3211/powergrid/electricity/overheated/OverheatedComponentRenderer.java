// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Draws the overheated device's own model with the vanilla destroy-stage crack overlay (D4).
package org.patryk3211.powergrid.electricity.overheated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.level.block.RenderShape;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class OverheatedComponentRenderer implements BlockEntityRenderer<OverheatedComponentBlockEntity> {
    /** Destroy stage used for the overlay (0 to 9). */
    private static final int CRACK_STAGE = 7;

    public OverheatedComponentRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(@NotNull OverheatedComponentBlockEntity be, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffer, int light, int overlay) {
        var state = be.getOriginalState();
        var level = be.getLevel();
        if(state == null || level == null || state.getRenderShape() != RenderShape.MODEL)
            return;
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(state, ms, buffer, light, overlay, ModelData.EMPTY, null);
        var pose = ms.last();
        var crack = new SheetedDecalTextureGenerator(buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(CRACK_STAGE)),
                pose.pose(), pose.normal());
        dispatcher.renderBreakingTexture(state, be.getBlockPos(), level, ms, crack, ModelData.EMPTY);
    }
}
