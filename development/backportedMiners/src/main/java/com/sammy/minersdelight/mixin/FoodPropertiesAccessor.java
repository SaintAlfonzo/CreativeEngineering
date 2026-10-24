package com.sammy.minersdelight.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Supplier;

/**
 * The raw effect suppliers of a food. {@link FoodProperties#getEffects()} resolves them, which fails while items are
 * registered (Farmer's Delight's effects are not registered yet); the cup foods need them lazily.
 */
@Mixin(FoodProperties.class)
public interface FoodPropertiesAccessor {
    @Accessor("effects")
    List<Pair<Supplier<MobEffectInstance>, Float>> minersdelight$effects();
}
