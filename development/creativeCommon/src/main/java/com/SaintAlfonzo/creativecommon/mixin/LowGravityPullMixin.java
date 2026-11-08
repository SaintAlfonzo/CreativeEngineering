package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Low gravity (Create: Gravity's biome factor) for the entities whose tick adds a fixed -0.04 pull: dropped items,
 * falling blocks, primed TNT, minecarts off rails (on rails the track sets the height) and a Shulker bullet that lost
 * its target. Scales that constant by LowGravity.pullScale, on the server and on the client alike (the client predicts
 * items, falling blocks and TNT itself), so no motion packets are needed. An item in water or lava never reaches the
 * constant (vanilla swims it instead). Replaces CreateGravityEntitiesMixin of 2026-11-08 00:01 (a velocity add-back
 * that needed a motion packet per entity per tick).
 */
@Mixin(value = {ItemEntity.class, FallingBlockEntity.class, PrimedTnt.class, AbstractMinecart.class, ShulkerBullet.class}, remap = false)
public abstract class LowGravityPullMixin {

	@ModifyConstant(method = "m_8119_", constant = @Constant(doubleValue = -0.04D), remap = false, require = 1)
	private double creativecommon$lowGravity(double pull) {
		return pull * LowGravity.pullScale((Entity) (Object) this);
	}
}
