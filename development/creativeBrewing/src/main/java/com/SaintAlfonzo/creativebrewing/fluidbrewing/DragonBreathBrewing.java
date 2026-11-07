package com.SaintAlfonzo.creativebrewing.fluidbrewing;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Dragon's breath is the one potion conversion the pack drives from a FLUID
 * (Create Central Kitchen's {@code create_central_kitchen:dragon_breath}) rather
 * than the vanilla item. Stock Create only surfaces potion conversions in its
 * "Automatic Brewing" JEI category / Mechanical Mixer path when they live in
 * {@link PotionMixingRecipes#ALL} / {@code BY_ITEM}, and those are keyed on an
 * item reagent — a fluid reagent is impossible there.
 *
 * <p>This builds, for every registered potion, a heated {@link MixingRecipe}
 * <pre>splash-potion fluid + dragon's breath fluid -&gt; lingering-potion fluid</pre>
 * and:
 * <ul>
 *   <li>adds it to {@link PotionMixingRecipes#ALL} so Create's JEI
 *       "Automatic Brewing" category lists it, and</li>
 *   <li>keeps it in {@link #FLUID_BREWING} so
 *       {@code MechanicalMixerBlockEntityMixin} can offer it to the basin (the
 *       vanilla mixer loop only checks item-keyed recipes).</li>
 * </ul>
 *
 * <p>The vanilla item-based dragon-breath container mixes are still stripped by
 * {@code kubejs/startup_scripts/cooking/potions.js} (part 3, its StartupEvents.postInit
 * BLACKLIST); that loop filters on
 * an item ingredient test, so it never touches these fluid recipes.
 *
 * <p>Run once from {@code FMLCommonSetupEvent} (after Create's static init,
 * before JEI loads). {@link PotionMixingRecipes#ALL} is not rebuilt on
 * {@code /reload}, so a one-shot append is stable for the session.
 */
public final class DragonBreathBrewing {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation DRAGON_BREATH_FLUID =
            new ResourceLocation("create_central_kitchen", "dragon_breath");

    private static final int AMOUNT = 1000;

    /** Dragon's breath is a catalyst-style reagent: a quarter-bucket per brew. */
    private static final int DRAGON_BREATH_AMOUNT = 250;

    /** Recipes the mixer mixin offers when their fluids are present in the basin. */
    public static final List<MixingRecipe> FLUID_BREWING = new ArrayList<>();

    private static boolean built = false;

    private DragonBreathBrewing() {}

    public static void build() {
        if (built) return;
        built = true;

        // getValue answers the registry default (minecraft:empty) for a missing id, never null: test containsKey.
        var dragonBreath = ForgeRegistries.FLUIDS.containsKey(DRAGON_BREATH_FLUID)
                ? ForgeRegistries.FLUIDS.getValue(DRAGON_BREATH_FLUID) : null;
        if (dragonBreath == null || dragonBreath == net.minecraft.world.level.material.Fluids.EMPTY) {
            LOGGER.warn("[CE] {} not found - dragon's breath fluid brewing disabled "
                    + "(is Create Central Kitchen installed?)", DRAGON_BREATH_FLUID);
            return;
        }
        FluidIngredient dragonBreathIngredient =
                FluidIngredient.fromFluidStack(new FluidStack(dragonBreath, DRAGON_BREATH_AMOUNT));

        int added = 0;
        for (Potion potion : ForgeRegistries.POTIONS) {
            if (potion == Potions.EMPTY || potion == Potions.WATER) continue;
            ResourceLocation id = ForgeRegistries.POTIONS.getKey(potion);
            if (id == null) continue;
            String slug = id.toString().replaceAll("[^a-z0-9_.-]", "_");

            FluidStack splash = PotionFluidHandler.getFluidFromPotion(potion, BottleType.SPLASH, AMOUNT);
            FluidStack lingering = PotionFluidHandler.getFluidFromPotion(potion, BottleType.LINGERING, AMOUNT);

            MixingRecipe recipe = new ProcessingRecipeBuilder<>(MixingRecipe::new,
                    new ResourceLocation(CreativeBrewing.NS, "potion_mixing_dragonbreath_" + slug))
                    .require(FluidIngredient.fromFluidStack(splash))
                    .require(dragonBreathIngredient)
                    .output(lingering)
                    .requiresHeat(HeatCondition.HEATED)
                    .build();

            FLUID_BREWING.add(recipe);
            added++;
        }

        try {
            PotionMixingRecipes.ALL.addAll(FLUID_BREWING);
        } catch (UnsupportedOperationException e) {
            LOGGER.warn("[CE] PotionMixingRecipes.ALL is immutable; dragon-breath fluid "
                    + "brewing will still work in the mixer but won't show in JEI", e);
        }

        LOGGER.info("[CE] registered {} dragon-breath fluid-brewing recipes", added);
    }
}
