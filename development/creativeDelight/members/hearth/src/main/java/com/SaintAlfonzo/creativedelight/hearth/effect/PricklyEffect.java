package com.SaintAlfonzo.creativedelight.hearth.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** 1 + level thorns damage every 0.5 s to anything touching the holder, except pets (N23-4). */
public class PricklyEffect extends MobEffect {
    public PricklyEffect() {
        super(MobEffectCategory.BENEFICIAL, 12526889);
    }

    @Override
    public void applyEffectTick(LivingEntity holder, int amplifier) {
        if (holder.level.isClientSide) return;
        float damage = 1.0F + amplifier;
        DamageSource source = DamageSource.thorns(holder);
        for (LivingEntity target : holder.level.getEntitiesOfClass(LivingEntity.class, holder.getBoundingBox().inflate(0.1D),
                e -> e != holder && e.isAlive() && !isPet(e))) {
            target.hurt(source, damage);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    /** Tamed animals of any owner, tamed horses, and modded pets that report an owner. */
    static boolean isPet(LivingEntity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) return true;
        if (entity instanceof AbstractHorse horse && horse.isTamed()) return true;
        return entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null;
    }
}
