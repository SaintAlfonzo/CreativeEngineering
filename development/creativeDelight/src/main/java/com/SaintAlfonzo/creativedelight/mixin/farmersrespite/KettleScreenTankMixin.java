package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBoxSlot;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTankHolder;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.client.KettleTankRender;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.farmersrespite.client.gui.KettleScreen;
import umpaz.farmersrespite.common.block.entity.container.KettleMenu;

/**
 * Farmer's Respite Kettle screen with the tank (farmers_respite.md K1, K4): 2.0's three-step water bar becomes a
 * four-unit bar drawn in the stored fluid's own texture and tint, its tooltip names the fluid and "n / 4", and the
 * two transfer boxes get frames (cut from this screen's container slot), a pointer at the box the transfer runs from
 * and a hint while empty. Methods are named twice: SRG for the pack, official for dev runs.
 */
@Mixin(value = KettleScreen.class, remap = false)
public abstract class KettleScreenTankMixin extends AbstractContainerScreen<KettleMenu> {

	@Unique private static final ResourceLocation CE_TEXTURE = new ResourceLocation("farmersrespite", "textures/gui/kettle.png");

	private KettleScreenTankMixin(KettleMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	/** 2.0 draws its bar from waterLevel(); it reads 0 here and the tank bar is drawn at TAIL. */
	@Redirect(method = {"m_7286_", "renderBg"}, require = 1, at = @At(value = "INVOKE",
			target = "Lumpaz/farmersrespite/common/block/entity/container/KettleMenu;waterLevel()I"))
	private int ce$hideWaterBar(KettleMenu menu) {
		return 0;
	}

	@Inject(method = {"m_7286_", "renderBg"}, at = @At("TAIL"), require = 1)
	private void ce$drawTank(PoseStack pose, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
		if (!(menu.tileEntity instanceof KettleTankHolder tank)) return;
		KettleTankRender.drawBar(pose, leftPos, topPos, tank);
		KettleTankRender.drawBoxes(pose, CE_TEXTURE, leftPos, topPos, tank);
	}

	@Inject(method = "renderWaterBarIndicatorTooltip", at = @At("HEAD"), cancellable = true, require = 1)
	private void ce$tankTooltip(PoseStack pose, int mouseX, int mouseY, CallbackInfo ci) {
		ci.cancel();
		if (!(menu.tileEntity instanceof KettleTankHolder tank) || !isHovering(35, 17, 3, 32, mouseX, mouseY)) return;
		renderComponentTooltip(pose, KettleTankRender.tankLines(tank), mouseX, mouseY);
	}

	@Inject(method = {"m_6305_", "render"}, at = @At("TAIL"), require = 1)
	private void ce$boxHint(PoseStack pose, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		Slot slot = hoveredSlot;
		if (!(slot instanceof KettleBoxSlot box) || slot.hasItem() || !menu.getCarried().isEmpty()) return;
		List<Component> lines = new ArrayList<>(KettleTankRender.boxLines(box.box()));
		renderComponentTooltip(pose, lines, mouseX, mouseY);
	}
}
