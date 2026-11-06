package com.SaintAlfonzo.creativedelight.compat.farmersrespite.client;

import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBoxSlot;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTank;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTankHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/** Drawing and text for the Kettle tank in its screen (farmers_respite.md K1, K4, K11). */
public final class KettleTankRender {

	/** 2.0's water bar area in the Kettle texture: x 34, y 17, 5 wide, 32 high (three steps there, four units here);
	 * the pack draws it 1 px narrower on each side (x 35, 3 wide). */
	private static final int BAR_X = 35;
	private static final int BAR_Y = 17;
	private static final int BAR_W = 3;
	private static final int BAR_H = 32;
	/** The container slot frame in the Kettle texture, reused for the boxes. */
	private static final int FRAME_U = 85;
	private static final int FRAME_V = 54;

	private KettleTankRender() {}

	public static void drawBar(PoseStack pose, int left, int top, KettleTankHolder tank) {
		FluidStack fluid = tank.ce$tankFluid();
		if (fluid.isEmpty()) return;
		int h = BAR_H * tank.ce$tankLevel() / KettleTank.MAX_LEVEL;
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
		ResourceLocation still = ext.getStillTexture(fluid);
		if (still == null) return;
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		int tint = ext.getTintColor(fluid);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(((tint >> 16) & 0xFF) / 255F, ((tint >> 8) & 0xFF) / 255F, (tint & 0xFF) / 255F,
				((tint >> 24) & 0xFF) == 0 ? 1F : ((tint >> 24) & 0xFF) / 255F);
		int bottom = top + BAR_Y + BAR_H;
		for (int drawn = 0; drawn < h; drawn += 16) {
			int piece = Math.min(16, h - drawn);
			GuiComponent.blit(pose, left + BAR_X, bottom - drawn - piece, 0, BAR_W, piece, sprite);
		}
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.disableBlend();
	}

	public static void drawBoxes(PoseStack pose, ResourceLocation texture, int left, int top, KettleTankHolder tank) {
		RenderSystem.setShaderTexture(0, texture);
		int x = left + KettleBoxSlot.X - 1;
		GuiComponent.blit(pose, x, top + KettleBoxSlot.FILL_Y - 1, 0, FRAME_U, FRAME_V, 18, 18, 256, 256);
		GuiComponent.blit(pose, x, top + KettleBoxSlot.EMPTY_Y - 1, 0, FRAME_U, FRAME_V, 18, 18, 256, 256);
		// a small pointer right of the box the transfer runs from
		int y = top + (tank.ce$pouring() ? KettleBoxSlot.FILL_Y : KettleBoxSlot.EMPTY_Y) + 4;
		int px = left + KettleBoxSlot.X + 17;
		for (int i = 0; i < 4; ++i) {
			GuiComponent.fill(pose, px + i, y + i, px + i + 1, y + 8 - i, 0xFF8B8B8B);
		}
	}

	public static List<Component> tankLines(KettleTankHolder tank) {
		List<Component> lines = new ArrayList<>();
		FluidStack fluid = tank.ce$tankFluid();
		if (fluid.isEmpty()) {
			lines.add(Component.translatable("creativedelight.kettle.tank.empty"));
		} else {
			lines.add(Component.translatable("creativedelight.kettle.tank.level", fluid.getDisplayName(),
					tank.ce$tankLevel(), KettleTank.MAX_LEVEL));
			lines.add(Component.translatable("creativedelight.kettle.tank.mb", fluid.getAmount(), KettleTank.CAPACITY)
					.withStyle(ChatFormatting.GRAY));
		}
		return lines;
	}

	public static List<Component> boxLines(int box) {
		String key = box == KettleTankHolder.FILL_BOX ? "creativedelight.kettle.box.fill" : "creativedelight.kettle.box.empty";
		return List.of(Component.translatable(key), Component.translatable(key + ".hint").withStyle(ChatFormatting.GRAY));
	}

	/** The Kettle item's tooltip line: "Tank: Black Tea, 3 / 4" from its kept tank, or null. */
	public static Component itemLine(ItemStack stack) {
		var tag = stack.getTagElement("BlockEntityTag");
		if (tag == null || !tag.contains("CETankFluid") || tag.getInt("CETankLevel") <= 0) return null;
		FluidStack type = FluidStack.loadFluidStackFromNBT(tag.getCompound("CETankFluid"));
		if (type.isEmpty()) return null;
		return Component.translatable("creativedelight.kettle.item.tank", type.getDisplayName(), tag.getInt("CETankLevel"),
				KettleTank.MAX_LEVEL).withStyle(ChatFormatting.GRAY);
	}
}
