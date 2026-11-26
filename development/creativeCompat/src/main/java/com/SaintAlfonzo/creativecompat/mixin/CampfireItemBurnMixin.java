// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lit campfires burn dropped items (feature_writeup/item_physics.md IP-04, IP-26; item-physics #10). Vanilla's
 * {@code CampfireBlock.entityInside} ({@code m_7892_}) hurts only living entities, so an item lying on a lit campfire
 * never took fire damage and ItemPhysic's burn rules (burningItems, the fire/lava split, the burn transforms: ash,
 * explosive) never ran there. At its TAIL a dropped item in a lit campfire takes the same {@code IN_FIRE} damage;
 * {@code ItemPhysicServer.hurt} decides whether it burns (fireproof items survive). Server only. Applied only with
 * ItemPhysic (CompatMixinPlugin). SRG names, remap = false.
 */
@Mixin(value = CampfireBlock.class, remap = false)
public abstract class CampfireItemBurnMixin {

	@Shadow(remap = false) @Final private int f_51233_;

	@Inject(method = "m_7892_", at = @At("TAIL"), remap = false)
	private void creativecompat$burnItems(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (level.isClientSide || !(entity instanceof ItemEntity) || !state.getValue(CampfireBlock.LIT)) return;
		entity.hurt(DamageSource.IN_FIRE, (float) this.f_51233_);
	}
}
