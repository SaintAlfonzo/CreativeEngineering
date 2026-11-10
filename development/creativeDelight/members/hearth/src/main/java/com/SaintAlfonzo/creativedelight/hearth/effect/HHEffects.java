package com.SaintAlfonzo.creativedelight.hearth.effect;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Effects kept from 1.4.1 (hearth_and_harvest.md 3.1). Drunk is not ported; Horse Boost came back for the Sugar Horse Treat (N23-7). */
public final class HHEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HearthAndHarvestMod.MODID);

    public static final RegistryObject<MobEffect> HORSE_BOOST = EFFECTS.register("horse_boost", () -> new Plain(MobEffectCategory.BENEFICIAL, 16777215)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "7c0f7fd4-6f3e-4e0a-9f55-3b1f4c0e9a21", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.JUMP_STRENGTH, "7c0f7fd4-6f3e-4e0a-9f55-3b1f4c0e9a22", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> PUNGENT = EFFECTS.register("pungent", () -> new Plain(MobEffectCategory.HARMFUL, 13822306));
    public static final RegistryObject<MobEffect> TEMPTING = EFFECTS.register("tempting", () -> new Plain(MobEffectCategory.BENEFICIAL, 15888151));
    public static final RegistryObject<MobEffect> PRICKLY = EFFECTS.register("prickly", PricklyEffect::new);
    /** Registered for the evaluation against Fruits Delight's Brightening (N26-9). */
    public static final RegistryObject<MobEffect> CLARITY = EFFECTS.register("clarity", () -> new Plain(MobEffectCategory.BENEFICIAL, 6599153));
    /** Applied only by the thrown pitchfork (N23-5, N25). */
    public static final RegistryObject<MobEffect> PINNED = EFFECTS.register("pinned", () -> new Plain(MobEffectCategory.HARMFUL, 9127187)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "7c0f7fd4-6f3e-4e0a-9f55-3b1f4c0e9a11", -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));

    /** Clarity refuses Blindness and Darkness (1.4.1 EffectEvents). */
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        MobEffect incoming = event.getEffectInstance().getEffect();
        if ((incoming == MobEffects.BLINDNESS || incoming == MobEffects.DARKNESS) && event.getEntity().hasEffect(CLARITY.get())) {
            event.setResult(Event.Result.DENY);
        }
    }

    /** Gaining Clarity clears Blindness and Darkness already on the entity. */
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() != CLARITY.get()) return;
        LivingEntity entity = event.getEntity();
        entity.removeEffect(MobEffects.BLINDNESS);
        entity.removeEffect(MobEffects.DARKNESS);
    }

    /** 1.19.2 has no jump attribute, so Pinned cancels the jump here. */
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(PINNED.get())) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
        }
    }

    /** MobEffect's constructor is protected. */
    private static final class Plain extends MobEffect {
        Plain(MobEffectCategory category, int colour) {
            super(category, colour);
        }
    }

    private HHEffects() {
    }
}
