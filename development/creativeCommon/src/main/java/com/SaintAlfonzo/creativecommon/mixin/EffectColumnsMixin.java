// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import java.util.ArrayList;
import java.util.List;

import com.SaintAlfonzo.creativecommon.client.PermanentEffects;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Inventory effect list in two columns (plan_diet_books_r2.md B2): timed effects in the first column, permanent ones
 * (client.PermanentEffects) in a second column to its right. Vanilla single column when the list is compact (narrow
 * screen, where the hover tooltip path is untouched), when there is no room for two full columns, or when every effect
 * is of one kind.
 * <p>
 * SRG names, {@code remap = false} (memory srg-mixin-compile-gotchas): {@code m_194014_} = renderEffects,
 * {@code m_194002_} = renderBackgrounds, {@code m_194008_} = renderIcons, {@code m_98722_} = renderLabels.
 * Redirects the three draw calls in {@code renderEffects} and calls the original once per column with the column's x
 * and its own row spacing. The {@code getActiveEffects} call stays free: Raspberry Pie redirects it (diet effect
 * filter) and two redirects on one call site crash. JEED's hover capture sits inside {@code renderBackgrounds}, so it
 * still sees each column at its real position.
 */
@Mixin(value = EffectRenderingInventoryScreen.class, remap = false)
public abstract class EffectColumnsMixin {

	/** Width of one wide entry (the effect background texture). */
	@Unique
	private static final int CC_COLUMN = 120;

	/** The split of the last iterable seen this frame (the same list object goes to all three draw calls). */
	@Unique
	private Iterable<MobEffectInstance> cc$splitOf;
	@Unique
	private List<MobEffectInstance> cc$timed;
	@Unique
	private List<MobEffectInstance> cc$permanent;

	@Shadow
	private void m_194002_(PoseStack pose, int x, int spacing, Iterable<MobEffectInstance> effects, boolean wide) {
		throw new AssertionError();
	}

	@Shadow
	private void m_194008_(PoseStack pose, int x, int spacing, Iterable<MobEffectInstance> effects, boolean wide) {
		throw new AssertionError();
	}

	@Shadow
	private void m_98722_(PoseStack pose, int x, int spacing, Iterable<MobEffectInstance> effects) {
		throw new AssertionError();
	}

	@Redirect(method = "m_194014_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;m_194002_(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/lang/Iterable;Z)V"))
	private void cc$columnBackgrounds(EffectRenderingInventoryScreen<?> self, PoseStack pose, int x, int spacing,
			Iterable<MobEffectInstance> effects, boolean wide) {
		cc$split(x, effects, wide);
		if (cc$timed == null) {
			m_194002_(pose, x, spacing, effects, wide);
			return;
		}
		m_194002_(pose, x, cc$spacing(cc$timed), cc$timed, true);
		m_194002_(pose, x + CC_COLUMN, cc$spacing(cc$permanent), cc$permanent, true);
	}

	@Redirect(method = "m_194014_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;m_194008_(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/lang/Iterable;Z)V"))
	private void cc$columnIcons(EffectRenderingInventoryScreen<?> self, PoseStack pose, int x, int spacing,
			Iterable<MobEffectInstance> effects, boolean wide) {
		if (cc$timed == null || cc$splitOf != effects) {
			m_194008_(pose, x, spacing, effects, wide);
			return;
		}
		m_194008_(pose, x, cc$spacing(cc$timed), cc$timed, true);
		m_194008_(pose, x + CC_COLUMN, cc$spacing(cc$permanent), cc$permanent, true);
	}

	@Redirect(method = "m_194014_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;m_98722_(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/lang/Iterable;)V"))
	private void cc$columnLabels(EffectRenderingInventoryScreen<?> self, PoseStack pose, int x, int spacing,
			Iterable<MobEffectInstance> effects) {
		if (cc$timed == null || cc$splitOf != effects) {
			m_98722_(pose, x, spacing, effects);
		} else {
			m_98722_(pose, x, cc$spacing(cc$timed), cc$timed);
			m_98722_(pose, x + CC_COLUMN, cc$spacing(cc$permanent), cc$permanent);
		}
		cc$clear();
	}

	/** Sets cc$timed / cc$permanent when two columns apply, else clears them. */
	@Unique
	private void cc$split(int x, Iterable<MobEffectInstance> effects, boolean wide) {
		cc$clear();
		if (!wide || ((Screen) (Object) this).width - x < 2 * CC_COLUMN) {
			return;
		}
		List<MobEffectInstance> timed = new ArrayList<>();
		List<MobEffectInstance> permanent = new ArrayList<>();
		for (MobEffectInstance i : effects) {
			(PermanentEffects.isPermanent(i) ? permanent : timed).add(i);
		}
		if (timed.isEmpty() || permanent.isEmpty()) {
			return;
		}
		cc$splitOf = effects;
		cc$timed = timed;
		cc$permanent = permanent;
	}

	@Unique
	private void cc$clear() {
		cc$splitOf = null;
		cc$timed = null;
		cc$permanent = null;
	}

	/** Vanilla's row spacing for a column of n entries (33, squeezed into 132 px above five). */
	@Unique
	private static int cc$spacing(List<MobEffectInstance> column) {
		int n = column.size();
		return n > 5 ? 132 / (n - 1) : 33;
	}
}
