package net.satisfy.vinery.core.effect;

import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class JellyEffect extends MobEffect {
    public JellyEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98D982);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int i = 50 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0f);
        }
        return;
    }

    /** Port: absorption on start, taken back when the effect ends (1.21's start / add pair cancelled out). */
    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + (float) (4 * (amplifier + 1)));
        super.addAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        entity.setAbsorptionAmount(Math.max(0, entity.getAbsorptionAmount() - (float) (4 * (amplifier + 1))));
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}