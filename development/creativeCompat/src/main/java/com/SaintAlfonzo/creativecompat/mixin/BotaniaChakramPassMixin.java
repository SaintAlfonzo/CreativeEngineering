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
import vazkii.botania.common.entity.ThornChakramEntity;

/**
 * The Botania thorn chakram in webs (plan_passthrough.md 5.1 {@code snag}, phase 0 brief 5.5): its tick saves its
 * motion, runs {@code ThrowableProjectile#tick} (where the web check snags it) and puts the saved motion back, so the
 * RETURN of its tick holds the snag again ({@code surface.PassProjectiles#snagTail}). Snagged, {@code tickCount} stays
 * below its 60-tick return and a return already set is cleared (server), so it waits in the web; it is picked up by
 * touch and drops when the web breaks ({@link PassSnagItem}). Applies only with Botania (CompatMixinPlugin).
 */
@Mixin(value = ThornChakramEntity.class, remap = false)
public abstract class BotaniaChakramPassMixin implements PassSnagItem {

	@Shadow
	private ItemStack stack;

	@Shadow
	private boolean isReturning() {
		throw new AssertionError();
	}

	@Shadow
	private void setEntityToReturnTo(int id) {
		throw new AssertionError();
	}

	@Inject(method = "m_8119_", at = @At("RETURN"))
	private void creativecompat$chakramWeb(CallbackInfo ci) {
		try {
			PassProjectiles.snagTail((Entity) (Object) this);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("BotaniaChakramPassMixin", t);
			// a broken lookup must never stop the chakram
		}
	}

	@Override
	public ItemStack creativecompat$snagStack() {
		return stack;
	}

	@Override
	public void creativecompat$holdTimers() {
		Entity self = (Entity) (Object) this;
		if (self.tickCount > 50) self.tickCount = 50;
		if (!self.level.isClientSide && isReturning()) setEntityToReturnTo(-1);
	}
}
