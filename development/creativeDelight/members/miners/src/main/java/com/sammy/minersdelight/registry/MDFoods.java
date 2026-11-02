package com.sammy.minersdelight.registry;

import com.mojang.datafixers.util.Pair;
import com.sammy.minersdelight.mixin.FoodPropertiesAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.function.Supplier;

/**
 * Food values, equal to Miner's Delight 1.4.5 (1.1.2's shared values are the same). A cup holds half a bowl:
 * {@link #cup(FoodProperties)}.
 */
public final class MDFoods {
    private static final int POOR = 200;
    private static final int BRIEF = 600;
    private static final int SHORT = 1200;
    private static final int MEDIUM = 3600;
    private static final int LONG = 6000;

    public static final FoodProperties CAVE_CARROT = food(3, 0.25F).build();
    public static final FoodProperties BAKED_CAVE_CARROT = food(5, 0.45F).build();
    public static final FoodProperties COPPER_CARROT = food(3, 0.45F).effect(() -> new MobEffectInstance(MobEffects.POISON, BRIEF), 1.0F).build();
    public static final FoodProperties PASTA_WITH_VEGGIEBALLS = nourishing(12, 1.0F, MEDIUM);
    public static final FoodProperties CAVE_SOUP = nourishing(10, 0.4F, MEDIUM);
    public static final FoodProperties VEGAN_PATTY = food(3, 0.15F).build();
    public static final FoodProperties VEGAN_HAMBURGER = food(11, 0.8F).build();
    public static final FoodProperties VEGAN_WRAP = food(10, 0.7F).build();
    public static final FoodProperties VEGAN_STEAK_AND_POTATOES = nourishing(12, 0.6F, MEDIUM);
    public static final FoodProperties MOSS = food(1, 0.05F).effect(() -> nourishment(POOR), 1.0F).fast().build();
    public static final FoodProperties BAT_WING = food(1, 0.1F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, BRIEF), 0.4F).meat().fast().build();
    public static final FoodProperties SMOKED_BAT_WING = food(3, 0.15F).meat().fast().build();
    public static final FoodProperties BAT_ROLLS = food(7, 0.45F).build();
    public static final FoodProperties BAT_COOKIE = food(1, 0.05F).meat().fast().build();
    public static final FoodProperties IMPROVISED_BARBECUE_STICK = food(7, 0.7F).fast().build();
    public static final FoodProperties BAT_SOUP = nourishing(7, 0.45F, MEDIUM);
    public static final FoodProperties SILVERFISH_EGGS = food(1, 0.05F).build();
    public static final FoodProperties WEIRD_CAVIAR = food(5, 0.15F).build();
    public static final FoodProperties ARTHROPOD = food(3, 0.15F).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, BRIEF), 0.4F).meat().build();
    public static final FoodProperties COOKED_ARTHROPOD = food(5, 0.15F).meat().build();
    public static final FoodProperties INSECT_SANDWICH = food(10, 0.55F).build();
    public static final FoodProperties INSECT_WRAP = food(12, 0.55F).build();
    public static final FoodProperties INSECT_STEW = nourishing(8, 0.45F, SHORT);
    public static final FoodProperties SEASONED_ARTHROPODS = nourishing(14, 0.75F, LONG);
    public static final FoodProperties SPIDER_LEG = food(6, 0.2F).meat().build();
    public static final FoodProperties BAKED_SPIDER_LEG = food(10, 0.2F).meat().build();
    public static final FoodProperties PLATE_OF_GLAZED_ARACHNID_LIMBS = nourishing(12, 0.8F, LONG);
    public static final FoodProperties PLATE_OF_FAKE_MEATLOAF = nourishing(8, 0.65F, SHORT);
    public static final FoodProperties CAVE_HAMBURGER = food(12, 0.6F).build();
    public static final FoodProperties CRUNCHY_BAR = food(7, 0.45F).build();
    public static final FoodProperties NUTRITIONAL_BAR = nourishing(10, 0.55F, BRIEF);
    public static final FoodProperties GOLDEN_NUTRITIONAL_BAR = nourishing(10, 0.8F, SHORT);
    public static final FoodProperties TENTACLES_ON_A_STICK = food(4, 0.2F).fast().build();
    public static final FoodProperties SQUID = food(3, 0.15F).effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, BRIEF), 0.4F).build();
    public static final FoodProperties GLOW_SQUID = food(3, 0.15F).effect(() -> new MobEffectInstance(MobEffects.GLOWING, BRIEF), 1.0F).build();
    public static final FoodProperties BAKED_SQUID = food(6, 0.4F).build();
    public static final FoodProperties TENTACLES = food(1, 0.05F).build();
    public static final FoodProperties BAKED_TENTACLES = food(2, 0.1F).fast().build();
    public static final FoodProperties SQUID_SANDWICH = food(10, 0.45F).build();
    public static final FoodProperties TAKOYAKI = food(12, 0.55F).build();
    public static final FoodProperties BOWL_OF_STUFFED_SQUID = nourishing(10, 0.8F, LONG);
    public static final FoodProperties GLOW_INK_PASTA = food(14, 1.1F).effect(() -> nourishment(LONG), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, SHORT), 1.0F).build();
    /** The vanilla soups as Miner's Delight serves them (with Nourishment), the base of their cups. */
    public static final FoodProperties BEETROOT_SOUP = nourishing(6, 0.6F, MEDIUM);
    public static final FoodProperties MUSHROOM_STEW = nourishing(6, 0.6F, MEDIUM);
    public static final FoodProperties RABBIT_STEW = nourishing(10, 0.6F, LONG);
    /** Farmer's Delight's onion soup (newer FD versions; 1.19.2-1.2.4 has none), the base of the onion soup cup. */
    public static final FoodProperties ONION_SOUP = nourishing(12, 0.8F, MEDIUM);

    private MDFoods() {
    }

    private static FoodProperties.Builder food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
    }

    private static FoodProperties nourishing(int nutrition, float saturation, int duration) {
        return food(nutrition, saturation).effect(() -> nourishment(duration), 1.0F).build();
    }

    private static MobEffectInstance nourishment(int duration) {
        return new MobEffectInstance(ModEffects.NOURISHMENT.get(), duration, 0);
    }

    /** Without resolving the effects (safe while items register). */
    public static boolean hasEffects(FoodProperties food) {
        return !((FoodPropertiesAccessor) food).minersdelight$effects().isEmpty();
    }

    /** Half the nutrition (rounded down), saturation and effect durations of the bowl; always fast to drink. */
    public static FoodProperties cup(FoodProperties bowl) {
        FoodProperties.Builder builder = food(bowl.getNutrition() / 2, bowl.getSaturationModifier() / 2.0F);
        for (Pair<Supplier<MobEffectInstance>, Float> pair : ((FoodPropertiesAccessor) bowl).minersdelight$effects()) {
            Supplier<MobEffectInstance> effect = pair.getFirst();
            builder.effect(() -> {
                MobEffectInstance full = effect.get();
                return new MobEffectInstance(full.getEffect(), full.getDuration() / 2, full.getAmplifier());
            }, pair.getSecond());
        }
        if (bowl.canAlwaysEat()) {
            builder.alwaysEat();
        }
        if (bowl.isMeat()) {
            builder.meat();
        }
        return builder.fast().build();
    }
}
