package com.SaintAlfonzo.solraspberrypie.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Vanilla's hidden-instance chain (9.4 A and C): ours can sit under a stronger potion, or a weaker potion under ours. */
@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {
    @Accessor("hiddenEffect")
    MobEffectInstance solrp$getHidden();

    @Accessor("hiddenEffect")
    void solrp$setHidden(MobEffectInstance hidden);
}
