// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.bucket.client;

import com.SaintAlfonzo.creativedelight.farm.bucket.WoodenCauldronBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/**
 * Draws the wooden cauldron's fluid surface: one quad of the fluid's still texture and tint (biome water color for
 * water), inside the walls (2 to 14), at vanilla's content heights (6 + 3 per level out of 16, so 9, 12, 15) scaled by
 * the exact amount. One renderer covers every accepted fluid, including ones added by KubeJS, with no per-fluid models.
 */
public class WoodenCauldronRenderer implements BlockEntityRenderer<WoodenCauldronBlockEntity> {

	private static final float MIN = 2.0F / 16.0F;
	private static final float MAX = 14.0F / 16.0F;

	public WoodenCauldronRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public void render(WoodenCauldronBlockEntity cauldron, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		FluidStack fluid = cauldron.fluid();
		if (fluid.isEmpty()) {
			return;
		}
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
		FluidState state = fluid.getFluid().defaultFluidState();
		var texture = ext.getStillTexture(fluid);
		if (texture == null) {
			return;
		}
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
		int color = cauldron.getLevel() != null
				? ext.getTintColor(state, cauldron.getLevel(), cauldron.getBlockPos())
				: ext.getTintColor(fluid);
		int a = (color >>> 24) & 0xFF;
		if (a == 0) {
			a = 0xFF;
		}
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		float fill = Math.min(1.0F, fluid.getAmount() / (float) WoodenCauldronBlockEntity.CAPACITY);
		float y = (6.0F + 9.0F * fill) / 16.0F;
		float u0 = sprite.getU(2.0D), u1 = sprite.getU(14.0D);
		float v0 = sprite.getV(2.0D), v1 = sprite.getV(14.0D);

		VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
		Matrix4f m = pose.last().pose();
		Matrix3f n = pose.last().normal();
		vc.vertex(m, MIN, y, MIN).color(r, g, b, a).uv(u0, v0).uv2(light).normal(n, 0, 1, 0).endVertex();
		vc.vertex(m, MIN, y, MAX).color(r, g, b, a).uv(u0, v1).uv2(light).normal(n, 0, 1, 0).endVertex();
		vc.vertex(m, MAX, y, MAX).color(r, g, b, a).uv(u1, v1).uv2(light).normal(n, 0, 1, 0).endVertex();
		vc.vertex(m, MAX, y, MIN).color(r, g, b, a).uv(u1, v0).uv2(light).normal(n, 0, 1, 0).endVertex();
	}
}
