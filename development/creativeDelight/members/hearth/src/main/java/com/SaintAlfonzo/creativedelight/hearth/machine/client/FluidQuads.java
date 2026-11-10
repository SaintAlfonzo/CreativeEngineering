package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/** Shared drawing for machine tanks and the items lying in them. */
final class FluidQuads {
    private FluidQuads() {
    }

    /** A flat surface of the fluid's still texture, tinted by the fluid. */
    static void surface(FluidStack fluid, Matrix4f matrix, MultiBufferSource buffer, float x0, float x1, float z0, float z1, float y, int light, float alpha) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation still = ext.getStillTexture(fluid);
        if (still == null) return;
        int color = ext.getTintColor(fluid);
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = (color >> 24 & 0xFF) / 255.0F;
        quad(buffer, matrix, x0, x1, z0, z1, y, r, g, b, a == 0.0F ? alpha : a, sprite(still), light);
    }

    static TextureAtlasSprite sprite(ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
    }

    static void quad(MultiBufferSource buffer, Matrix4f m, float x0, float x1, float z0, float z1, float y, float r, float g, float b, float a, TextureAtlasSprite sprite, int light) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));
        float u0 = sprite.getU0();
        float u1 = u0 + (sprite.getU1() - u0) * (x1 - x0);
        float v0 = sprite.getV0();
        float v1 = v0 + (sprite.getV1() - v0) * (z1 - z0);
        int ov = OverlayTexture.NO_OVERLAY;
        vc.vertex(m, x0, y, z0).color(r, g, b, a).uv(u0, v0).overlayCoords(ov).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        vc.vertex(m, x0, y, z1).color(r, g, b, a).uv(u0, v1).overlayCoords(ov).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        vc.vertex(m, x1, y, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(ov).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
        vc.vertex(m, x1, y, z0).color(r, g, b, a).uv(u1, v0).overlayCoords(ov).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    /** Keeps translucent item layers out of the translucent fluid pass, as 1.4.1. */
    static MultiBufferSource solidOnly(MultiBufferSource buffer) {
        return type -> type == RenderType.translucent() || type == RenderType.translucentMovingBlock() || type.toString().contains("translucent")
                ? buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS))
                : buffer.getBuffer(type);
    }

    /** Position, rotation and height per item from the block position (same pile each frame). */
    static float[] scatter(long seed, int index) {
        long r = seed ^ index * -7046029254386353131L;
        float[] out = new float[4];
        for (int i = 0; i < 4; i++) {
            r = r * 7809847782465536322L + 7113472399480571277L;
            out[i] = (r >>> 33 & 65535L) / 65535.0F;
        }
        return out;
    }
}
