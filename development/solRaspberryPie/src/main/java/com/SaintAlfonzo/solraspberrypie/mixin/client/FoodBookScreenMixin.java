// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.DietLogPages;
import com.SaintAlfonzo.solraspberrypie.client.EffectScreen;
import com.SaintAlfonzo.solraspberrypie.data.GateFoods;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.DiversityPage;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Rectangle;

/**
 * The Diet Log (6.3, 12.4): a button that opens the effect screen; Raspberry Pie's ladder pages (progress bar and
 * milestones) after Apple Pie's diversity page; Apple Pie's own benefit pages removed; the diversity page's activation
 * count read from Raspberry Pie's gate counter (unique foods when configured). The whole book is drawn at
 * {@link DietLogPages#bookScale} around the screen center (G5b): a model-view scale, so Apple Pie's fixed-pixel UI kit
 * and its item models grow together, with the mouse mapped back for hovers and clicks.
 * <p>
 * plan_diet_books.md phase 1: the background stretched sideways by {@code [dietLog] extraWidth} (left half, the center
 * column repeated, right half; the pages widen and the page buttons move out), Apple Pie's diversity page drawn at its
 * configured position and scale, and the mouse wheel flips pages.
 */
@Mixin(value = FoodBookScreen.class, remap = false)
public abstract class FoodBookScreenMixin extends Screen {
	protected FoodBookScreenMixin(Component title) {
		super(title);
	}

	@Redirect(method = "initPages", at = @At(value = "INVOKE", target = "Lcom/tarinoita/solsweetpotato/tracking/FoodList;getFoodsEaten()I"))
	private int solrp$gateCount(FoodList list) {
		return GateFoods.count(list);
	}

	/** Widens the background before the pages take its frame (plan_diet_books.md 1.5). */
	@Inject(method = "initPages", at = @At("HEAD"))
	private void solrp$widen(CallbackInfo ci) {
		DietLogPages.OpenTiming.pagesBegin();
		int extra = DietLogPages.extraWidth();
		if (extra <= 0) return;
		UIImage bg = ((FoodBookScreenAccessor) (Object) this).solrp$background();
		bg.setWidth(bg.image.partOfTexture.width + extra);
		bg.setCenterX(this.width / 2);
	}

	@Inject(method = "initPages", at = @At("RETURN"))
	private void solrp$addLadderPages(CallbackInfo ci) {
		DietLogPages.insert((FoodBookScreen) (Object) this);
		DietLogPages.OpenTiming.pagesEnd();
	}

	@Unique
	private long solrp$frameStart;

	@Inject(method = "m_7856_", at = @At("TAIL"))
	private void solrp$addButton(CallbackInfo ci) {
		int extra = DietLogPages.extraWidth();
		int center = this.width / 2;
		for (GuiEventListener l : this.children()) {
			// Apple Pie's page flip buttons (a package-private class) move out by half the extra width.
			if (extra > 0 && l instanceof AbstractWidget w && w.getClass().getSimpleName().equals("PageFlipButton")) {
				w.x += w.x < center ? -extra / 2 : extra / 2;
			}
		}
		int w = 110;
		Rectangle book = ((FoodBookScreenAccessor) (Object) this).solrp$background().frame;
		this.addRenderableWidget(new Button(book.x + book.width / 2 - w / 2, this.height / 2 + 98, w, 20,
				Component.translatable("solraspberrypie.dietlog.button"), b -> EffectScreen.open()));
	}

	/** The book background, stretched by repeating its center column {@code extraWidth} times (1.5). */
	@Redirect(method = "m_6305_", at = @At(value = "INVOKE", ordinal = 0,
			target = "Lcom/tarinoita/solsweetpotato/client/gui/elements/UIElement;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/tarinoita/solsweetpotato/client/gui/elements/UIElement;II)V"))
	private void solrp$drawBackground(PoseStack pose, UIElement element, int mouseX, int mouseY) {
		int extra = DietLogPages.extraWidth();
		if (extra <= 0 || !(element instanceof UIImage img)) {
			UIElement.render(pose, element, mouseX, mouseY);
			return;
		}
		Rectangle f = img.frame, t = img.image.partOfTexture;
		int half = t.width / 2;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, img.image.textureLocation);
		GuiComponent.blit(pose, f.x, f.y, 0, t.x, t.y, half, t.height, 256, 256);
		GuiComponent.blit(pose, f.x + half, f.y, extra, t.height, t.x + half, t.y, 1, t.height, 256, 256);
		GuiComponent.blit(pose, f.x + half + extra, f.y, 0, t.x + half, t.y, t.width - half, t.height, 256, 256);
	}

	/** Apple Pie's diversity page at its configured position and scale, around its top center (1.4). */
	@Redirect(method = "m_6305_", at = @At(value = "INVOKE", ordinal = 1,
			target = "Lcom/tarinoita/solsweetpotato/client/gui/elements/UIElement;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/tarinoita/solsweetpotato/client/gui/elements/UIElement;II)V"))
	private void solrp$drawPage(PoseStack pose, UIElement page, int mouseX, int mouseY) {
		if (!(page instanceof DiversityPage)) {
			UIElement.render(pose, page, mouseX, mouseY);
			return;
		}
		DietLogPages.Layout l = DietLogPages.Layout.diversity();
		int px = page.getCenterX(), py = page.getMinY();
		l.begin(px, py);
		UIElement.render(pose, page, l.unmapX(mouseX, px), l.unmapY(mouseY, py));
		DietLogPages.Layout.end();
	}

	/** Screen position -> book position: the inverse of the scale around the screen center. */
	@Unique
	private double solrp$unscale(double v, int size) {
		float s = DietLogPages.bookScale(this.width, this.height);
		return size / 2.0 + (v - size / 2.0) / s;
	}

	@ModifyVariable(method = "m_6305_", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private int solrp$mouseX(int x) {
		return (int) Math.floor(solrp$unscale(x, this.width));
	}

	@ModifyVariable(method = "m_6305_", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private int solrp$mouseY(int y) {
		return (int) Math.floor(solrp$unscale(y, this.height));
	}

	@Inject(method = "m_6305_", at = @At("HEAD"))
	private void solrp$scaleBegin(PoseStack pose, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (DietLogPages.OpenTiming.frameWanted()) solrp$frameStart = System.nanoTime();
		float s = DietLogPages.bookScale(this.width, this.height);
		PoseStack mv = RenderSystem.getModelViewStack();
		mv.pushPose();
		mv.translate(this.width / 2.0, this.height / 2.0, 0);
		mv.scale(s, s, 1);
		mv.translate(-this.width / 2.0, -this.height / 2.0, 0);
		RenderSystem.applyModelViewMatrix();
	}

	@Inject(method = "m_6305_", at = @At("RETURN"))
	private void solrp$scaleEnd(PoseStack pose, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();
		if (DietLogPages.OpenTiming.frameWanted()) DietLogPages.OpenTiming.frameEnd(System.nanoTime() - solrp$frameStart);
	}

	/** Clicks land on the scaled page-flip and Diet Bonuses buttons (reobf renames this override to its SRG name). */
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		return super.mouseClicked(solrp$unscale(x, this.width), solrp$unscale(y, this.height), button);
	}

	/** The mouse wheel flips one page per notch (plan_diet_books.md 1.6; reobf renames this override too). */
	@Override
	public boolean mouseScrolled(double x, double y, double delta) {
		FoodBookScreen self = (FoodBookScreen) (Object) this;
		int target = self.getCurrentPageNumber() + (delta < 0 ? 1 : -1);
		if (delta == 0 || !self.isWithinRange(target)) return super.mouseScrolled(x, y, delta);
		self.switchToPage(target);
		if (this.minecraft != null) this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
		return true;
	}
}
