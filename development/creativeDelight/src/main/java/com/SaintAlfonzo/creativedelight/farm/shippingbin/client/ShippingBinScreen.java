package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBinBlockEntity;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBinMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The bin's screen, drawn from the donor's GUI texture and its two bar sprites (read from the donor jar at runtime,
 * blitted by path; 1.19.2 has no GUI sprite atlas). Hovering the fill bar shows the stored value.
 */
public class ShippingBinScreen extends AbstractContainerScreen<ShippingBinMenu> {
	private static final ResourceLocation BACKGROUND = ShippingBin.id("textures/gui/shipping_bin.png");
	private static final ResourceLocation FILL = ShippingBin.id("textures/gui/sprites/container/shipping_bin/fill_progress.png");
	private static final ResourceLocation SALE = ShippingBin.id("textures/gui/sprites/container/shipping_bin/sale_progress.png");
	public static final int FILL_X = 20, FILL_Y = 59, FILL_W = 52, FILL_H = 7;
	public static final int SALE_X = 79, SALE_Y = 28, SALE_W = 24, SALE_H = 16;

	public ShippingBinScreen(ShippingBinMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTick);
		renderTooltip(pose, mouseX, mouseY);
		if (isHovering(FILL_X, FILL_Y - 1, FILL_W, FILL_H + 2, mouseX, mouseY)) {
			renderTooltip(pose, Component.translatable("container.farmingforblockheads.shipping_bin.value", menu.value(), menu.capacity()),
					mouseX, mouseY);
		}
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BACKGROUND);
		blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		int fill = Math.min(FILL_W, menu.fillPermille() * FILL_W / 1000);
		if (fill > 0) {
			RenderSystem.setShaderTexture(0, FILL);
			blit(pose, leftPos + FILL_X, topPos + FILL_Y, 0, 0, fill, FILL_H, FILL_W, FILL_H);
		}
		int sale = menu.saleProgress() * SALE_W / ShippingBinBlockEntity.SALE_TICKS;
		if (sale > 0) {
			RenderSystem.setShaderTexture(0, SALE);
			blit(pose, leftPos + SALE_X, topPos + SALE_Y, 0, 0, sale, SALE_H, SALE_W, SALE_H);
		}
	}
}
