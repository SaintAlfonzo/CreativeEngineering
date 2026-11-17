package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassProjectiles;
import com.SaintAlfonzo.creativecompat.surface.PassSnagItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.content.tools.entity.rang.AbstractPickarang;

/**
 * Quark pickarangs in webs (plan_passthrough.md 5.1 {@code snag}, phase 0 brief 5.5): {@code AbstractPickarang#tick}
 * moves with {@code setPos} and never calls {@code checkInsideBlocks}, so every return of its tick runs the whole web
 * check ({@code surface.PassProjectiles#tick}). Snagged, {@code liveTime} is held at 0 so it never turns for home; it
 * is picked up by touch and drops when the web breaks ({@link PassSnagItem}). Returning pickarangs fly with
 * {@code noPhysics} and are skipped. Applies only with Quark (CompatMixinPlugin).
 */
@Mixin(value = AbstractPickarang.class, remap = false)
public abstract class QuarkPickarangPassMixin implements PassSnagItem {

	@Shadow
	protected int liveTime;

	@Shadow
	public abstract ItemStack getStack();

	@Inject(method = "m_8119_", at = @At("RETURN"))
	private void creativecompat$pickarangWeb(CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (self.isRemoved()) return;
		try {
			PassProjectiles.tick(self);
		} catch (Throwable ignored) {
			// a broken lookup must never stop the pickarang
		}
	}

	@Override
	public ItemStack creativecompat$snagStack() {
		return getStack();
	}

	@Override
	public void creativecompat$holdTimers() {
		liveTime = 0;
	}
}
