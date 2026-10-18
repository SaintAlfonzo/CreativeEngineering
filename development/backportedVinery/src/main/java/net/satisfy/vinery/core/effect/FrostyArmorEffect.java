package net.satisfy.vinery.core.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.satisfy.vinery.core.Vinery;

public class FrostyArmorEffect extends MobEffect {
    private static final ResourceLocation MOVEMENT_SPEED_ID = new ResourceLocation(Vinery.MOD_ID, "frosty_movement_speed");
    private static final ResourceLocation DAMAGE_ID = new ResourceLocation(Vinery.MOD_ID, "frosty_damage");
    private static final ResourceLocation ARMOR_ID = new ResourceLocation(Vinery.MOD_ID, "frosty_armor");

    public static final double FROST_MULTIPLIER = -0.05D;

    public FrostyArmorEffect() {
        super(MobEffectCategory.NEUTRAL, 0x56CBFD);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, VineryEffect.uuid(MOVEMENT_SPEED_ID),
                FROST_MULTIPLIER,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        addAttributeModifier(Attributes.ATTACK_DAMAGE, VineryEffect.uuid(DAMAGE_ID),
                2.0,
                AttributeModifier.Operation.ADDITION
        );
        addAttributeModifier(Attributes.ARMOR, VineryEffect.uuid(ARMOR_ID),
                4.0,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public void applyEffectTick(LivingEntity living, int amplifier) {
        living.setIsInPowderSnow(true);
        if (amplifier > 0 && living.canFreeze()) {
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze(), living.getTicksFrozen() + amplifier));
        }
        return;
    }

    @Override
    public boolean isDurationEffectTick(int i, int j) {
        return true;
    }
}