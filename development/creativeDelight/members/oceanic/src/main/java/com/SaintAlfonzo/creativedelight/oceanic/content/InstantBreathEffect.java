package com.SaintAlfonzo.creativedelight.oceanic.content;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.Nullable;

/** Instant Breath: fills the air bar at once, with the bubble column whirl sound (ocean_delight.md 5.6). */
public class InstantBreathEffect extends MobEffect {
    public InstantBreathEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF66C2FF);
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

    /** Applied as a one-tick effect by the foods: runs when it is added. */
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        breathe(entity);
    }

    /** Potions, arrows and clouds of an instant effect. */
    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirect, LivingEntity entity, int amplifier, double health) {
        breathe(entity);
    }

    private static void breathe(LivingEntity entity) {
        entity.setAirSupply(entity.getMaxAirSupply());
        if (!entity.level.isClientSide) {
            entity.level.playSound(null, entity.blockPosition(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
