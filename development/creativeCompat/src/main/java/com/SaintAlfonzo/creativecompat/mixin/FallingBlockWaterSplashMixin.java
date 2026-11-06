package com.SaintAlfonzo.creativecompat.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code FallingBlockEntity.tick} (m_8119_) never calls {@code Entity.baseTick}, so the water-state
 * check that ends in {@code Entity.doWaterSplashEffect} (m_5841_) never runs and falling blocks
 * (anvils, sand, gravel, concrete powder) enter water silently. This fires the splash once, on the
 * first tick the block is inside water. The splash sound, bubbles and the Effective splash model
 * (it hooks the tail of doWaterSplashEffect) come from that call. Client only; the water state is
 * tracked here so the block is not given water-current pushing.
 */
@Mixin(value = FallingBlockEntity.class, remap = false)
public abstract class FallingBlockWaterSplashMixin extends Entity {

    private FallingBlockWaterSplashMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private boolean creativecompat$wasInWater;

    @Inject(method = "m_8119_", at = @At("TAIL"), remap = false)
    private void creativecompat$splash(CallbackInfo ci) {
        if (!(this.level instanceof ClientLevel level) || this.isRemoved()) {
            return;
        }
        boolean inWater = level.getFluidState(this.blockPosition()).is(FluidTags.WATER);
        if (inWater && !creativecompat$wasInWater) {
            this.doWaterSplashEffect();
        }
        creativecompat$wasInWater = inWater;
    }
}
