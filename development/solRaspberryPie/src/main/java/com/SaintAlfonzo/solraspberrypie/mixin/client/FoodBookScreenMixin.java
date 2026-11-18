package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.DietLogPages;
import com.SaintAlfonzo.solraspberrypie.client.EffectScreen;
import com.SaintAlfonzo.solraspberrypie.data.GateFoods;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.client.gui.components.Button;
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

	@Inject(method = "initPages", at = @At("RETURN"))
	private void solrp$addLadderPages(CallbackInfo ci) {
		DietLogPages.insert((FoodBookScreen) (Object) this);
	}

	@Inject(method = "m_7856_", at = @At("TAIL"))
	private void solrp$addButton(CallbackInfo ci) {
		int w = 110;
		Rectangle book = ((FoodBookScreenAccessor) (Object) this).solrp$background().frame;
		this.addRenderableWidget(new Button(book.x + book.width / 2 - w / 2, this.height / 2 + 98, w, 20,
				Component.translatable("solraspberrypie.dietlog.button"), b -> EffectScreen.open()));
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
	}

	/** Clicks land on the scaled page-flip and Diet Bonuses buttons (reobf renames this override to its SRG name). */
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		return super.mouseClicked(solrp$unscale(x, this.width), solrp$unscale(y, this.height), button);
	}
}
