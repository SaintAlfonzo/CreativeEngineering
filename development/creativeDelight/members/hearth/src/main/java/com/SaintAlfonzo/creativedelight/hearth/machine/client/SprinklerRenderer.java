package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.SprinklerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/** Field Sprinkler's water level, tinted by the biome's water colour. */
public class SprinklerRenderer implements BlockEntityRenderer<SprinklerBlockEntity> {
    public SprinklerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SprinklerBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        FluidStack fluid = be.tank().getFluid();
        if (fluid.isEmpty()) return;
        float y = 0.0625F + (float) fluid.getAmount() / SprinklerBlockEntity.CAPACITY * 0.375F;
        int color = be.getLevel() != null ? BiomeColors.getAverageWaterColor(be.getLevel(), be.getBlockPos()) : 4159204;
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        FluidQuads.quad(buffer, pose.last().pose(), 0.1875F, 0.8125F, 0.1875F, 0.8125F, y, (color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, 0.8F, FluidQuads.sprite(ext.getStillTexture(fluid)), light);
    }
}
