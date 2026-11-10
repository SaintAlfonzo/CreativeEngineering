package com.SaintAlfonzo.creativedelight.hearth.entity;

import com.SaintAlfonzo.creativedelight.hearth.effect.HHEffects;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Thrown manure: Pungent and Poison for 5 s on what it hits. 1.4.1's fly particles are not ported. */
public class ManureProjectile extends ThrowableItemProjectile {
    public ManureProjectile(EntityType<? extends ManureProjectile> type, Level level) {
        super(type, level);
    }

    public ManureProjectile(Level level, LivingEntity shooter) {
        super(HHEntities.MANURE_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return HHItems.MANURE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target && target != getOwner()) {
            target.addEffect(new MobEffectInstance(HHEffects.PUNGENT.get(), 100, 0));
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level.isClientSide) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 1.0F, 0.6F + random.nextFloat() * 0.4F);
            discard();
        }
    }
}
