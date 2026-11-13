package net.satisfy.vinery.core.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class LavaWalkerEffect extends MobEffect {
    public LavaWalkerEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xCC3300);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (!(pLivingEntity instanceof Player player && player.isSpectator())) {
            Vec3 pos = pLivingEntity.position();
            Vec3 movement = pLivingEntity.getDeltaMovement();
            Vec3 futurePos = pos.add(movement);
            BlockPos onPos = pLivingEntity.getOnPos();
            BlockPos futureBlockPos = new BlockPos((int) futurePos.x, (int) futurePos.y, (int) futurePos.z);
            if (pLivingEntity.isInLava()) {
                pLivingEntity.setDeltaMovement(movement.add(0, 0.1, 0));
            } else if (pLivingEntity.level.getFluidState(onPos).is(FluidTags.LAVA)) {
                if (pLivingEntity.level instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.LAVA, pos.x(), pos.y() + 0.1D, pos.z(), 10, 0.2, 0.1, 0.2, 1.5);
                }
                pLivingEntity.setDeltaMovement(movement.x(), Math.max(movement.y(), 0D), movement.z());
                pLivingEntity.setOnGround(true);
                // Pack change (raspberry_pie.md 4): standing on lava burns like a magma block unless the walker has
                // Fire Resistance (or is fire immune, or sneaks, as on magma). Falling into lava burns as usual.
                if (!pLivingEntity.level.isClientSide && !pLivingEntity.fireImmune() && !pLivingEntity.isSteppingCarefully()
                        && !pLivingEntity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    pLivingEntity.hurt(DamageSource.HOT_FLOOR, 1.0F);
                }
            } else if (pLivingEntity.level.getFluidState(futureBlockPos).is(FluidTags.LAVA) && movement.y() > -0.8) {
                if (pLivingEntity.level instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.LAVA, pos.x(), pos.y() + 0.1D, pos.z(), 10, 0.2, 0.1, 0.2, 1.5);
                }
                pLivingEntity.setDeltaMovement(movement.x(), Math.max(movement.y(), movement.y() * 0.5), movement.z());
            }
            super.applyEffectTick(pLivingEntity, pAmplifier);
            return;
        }
        return;
    }

    @Override
    public boolean isDurationEffectTick(int i, int j) {
        return true;
    }
}