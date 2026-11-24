// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.FoodTooltips;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * AppleSkin's food tooltip row (plan_diet_books_r2.md A11): the saturation icons sit on the shank row, after the
 * shanks, unless {@code [tooltips] saturationOwnLine} keeps AppleSkin's own second row. AppleSkin 2.4.2 only
 * (RpMixinPlugin): the width is the sum of the two rows instead of the larger, the height one row, and the saturation
 * drawing (which starts at the first read of saturationBars, after the row step) is moved up one row and right past
 * the shanks. Names are SRG (remap off, like FoodBookScreenMixin).
 */
@Mixin(targets = "squeek.appleskin.client.TooltipOverlayHandler$FoodTooltipRenderer", remap = false)
public abstract class AppleSkinTooltipRendererMixin {
	@Unique
	private static final int SOLRP_GAP = 4, SOLRP_ROW = 10;

	@Unique
	private int solrp$shankWidth;

	@Unique
	private boolean solrp$shifted;

	@Inject(method = "m_142103_", at = @At("HEAD"), cancellable = true)
	private void solrp$height(CallbackInfoReturnable<Integer> cir) {
		if (!FoodTooltips.saturationOwnLine()) cir.setReturnValue(SOLRP_ROW);
	}

	@Redirect(method = "m_142069_", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
	private int solrp$width(int shanks, int saturation) {
		solrp$shankWidth = shanks;
		return FoodTooltips.saturationOwnLine() ? Math.max(shanks, saturation) : shanks + SOLRP_GAP + saturation;
	}

	@Inject(method = "m_183452_", at = @At(value = "FIELD", opcode = 180, ordinal = 0,
			target = "Lsqueek/appleskin/client/TooltipOverlayHandler$FoodTooltip;saturationBars:I"))
	private void solrp$moveSaturation(Font font, int x, int y, PoseStack pose, ItemRenderer items, int z, CallbackInfo ci) {
		if (FoodTooltips.saturationOwnLine()) return;
		pose.pushPose();
		pose.translate(solrp$shankWidth + SOLRP_GAP, -SOLRP_ROW, 0);
		solrp$shifted = true;
	}

	@Inject(method = "m_183452_", at = @At("RETURN"))
	private void solrp$restore(Font font, int x, int y, PoseStack pose, ItemRenderer items, int z, CallbackInfo ci) {
		if (!solrp$shifted) return;
		solrp$shifted = false;
		pose.popPose();
	}
}
