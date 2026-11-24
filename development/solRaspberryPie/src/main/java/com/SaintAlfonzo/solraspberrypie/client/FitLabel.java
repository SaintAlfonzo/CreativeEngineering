// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.elements.UILabel;

import java.awt.Rectangle;

/**
 * An Apple Pie label drawn at a smaller text scale (plan_diet_books_r2.md A3): the diversity page's "Current Food
 * Diversity" and gate lines, each on its own line, at the largest scale (at most {@link #MAX}) that fits the page.
 */
public final class FitLabel extends UILabel {
	public static final float MAX = 0.85F;
	private final float scale;

	public FitLabel(UILabel from, float scale) {
		super(new Rectangle(Math.max(1, Math.round((fontRenderer.width(from.text) - 1) * scale)), Math.max(1, Math.round(7 * scale))), from.text);
		this.color = from.color;
		this.tooltip = from.tooltip;
		this.scale = scale;
	}

	/** The largest scale, at most {@link #MAX}, at which every text fits {@code width}. */
	public static float fit(int width, Iterable<String> texts) {
		float s = MAX;
		for (String t : texts) {
			int w = fontRenderer.width(t) - 1;
			if (w > 0) s = Math.min(s, width / (float) w);
		}
		return Math.max(0.25F, s);
	}

	@Override
	protected void render(PoseStack pose) {
		pose.pushPose();
		pose.translate(frame.x, frame.y, 0);
		pose.scale(scale, scale, 1);
		if (color.getTransparency() == 3) RenderSystem.enableBlend();
		fontRenderer.draw(pose, text, 0, 0, color.getRGB());
		pose.popPose();
	}
}
