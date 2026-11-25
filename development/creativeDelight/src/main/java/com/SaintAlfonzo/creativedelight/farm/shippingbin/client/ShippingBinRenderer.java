package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBinBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Up to 12 of the bin's input items lying in the crate at 0.36 scale; how many shows how full the bin is
 * (stored value / capacity). Items repeat in slot order.
 */
public class ShippingBinRenderer implements BlockEntityRenderer<ShippingBinBlockEntity> {
	private static final int MAX = 12;
	private static final float SCALE = 0.36F;
	private final ItemRenderer items;

	public ShippingBinRenderer(BlockEntityRendererProvider.Context context) {
		this.items = context.getItemRenderer();
	}

	@Override
	public void render(ShippingBinBlockEntity bin, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
		List<ItemStack> stacks = new ArrayList<>();
		for (ItemStack stack : bin.inputs()) if (!stack.isEmpty()) stacks.add(stack);
		if (stacks.isEmpty()) return;
		int shown = (int) Math.ceil(MAX * Math.min(1.0D, bin.storedValue() / (double) bin.clientCapacity()));
		shown = Math.max(1, Math.min(MAX, shown));
		int seed = (int) bin.getBlockPos().asLong();
		for (int i = 0; i < shown; i++) {
			ItemStack stack = stacks.get(i % stacks.size());
			int layer = i / 6, spot = i % 6;
			pose.pushPose();
			pose.translate(0.3D + (spot % 3) * 0.2D, 0.25D + layer * 0.12D + spot * 0.005D, 0.36D + (spot / 3) * 0.28D);
			pose.mulPose(Vector3f.YP.rotationDegrees(((seed + i * 47) & 0xff) * 1.4F));
			pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			pose.scale(SCALE, SCALE, SCALE);
			items.renderStatic(stack, ItemTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, seed + i);
			pose.popPose();
		}
	}
}
