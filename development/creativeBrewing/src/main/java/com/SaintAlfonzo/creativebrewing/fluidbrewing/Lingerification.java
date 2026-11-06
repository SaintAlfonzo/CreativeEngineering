package com.SaintAlfonzo.creativebrewing.fluidbrewing;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * "Lingerification" — the IE Refinery counterpart to {@link DragonBreathBrewing}. Where that
 * class drives <em>splash&nbsp;potion&nbsp;fluid&nbsp;+&nbsp;dragon's&nbsp;breath&nbsp;fluid&nbsp;-&gt;&nbsp;lingering&nbsp;potion&nbsp;fluid</em>
 * on the Create Mechanical Mixer, this registers the same conversion on the IE Refinery
 * multiblock, auto-generated for every registered potion, for <b>both</b> the REGULAR and the
 * SPLASH bottle type as input.
 *
 * <p>Mechanism mirrors IE's own {@code immersiveengineering:mixer_potion_list}: a
 * {@link GeneratedListRecipe} generator is registered in
 * {@link GeneratedListRecipe#LIST_GENERATORS} under {@code creativebrewing:lingerification_list},
 * and a one-line datapack file
 * ({@code data/creativebrewing/recipes/lingerification_list.json} =
 * {@code {"type":"immersiveengineering:generated_list"}}) pulls it in. The sub-recipes are real
 * {@link RefineryRecipe} instances, so {@code RefineryRecipe.RECIPES} (and therefore the Refinery
 * block entity + JEI) pick them up automatically via {@code CachedRecipeList.updateCache}'s
 * {@code IListRecipe} flat-map.
 *
 * <p>NBT-carrying {@code create:potion} inputs match because patcherMod's
 * {@code FluidTagInputMixin} already relaxes the shared {@code FluidTagInput.testIgnoringAmount}
 * NBT check to a subset match (Bottler + Mixer + Refinery).
 *
 * <p>Runs once from {@code FMLCommonSetupEvent} (main thread, before recipe load).
 * {@link GeneratedListRecipe#LIST_GENERATORS} is a plain static map and the put survives
 * {@code /reload}, so a one-shot registration is stable for the session. The generator supplier
 * itself is re-invoked each reload, when {@link ForgeRegistries#POTIONS} is fully populated.
 */
public final class Lingerification {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation LIST_ID =
            new ResourceLocation(CreativeBrewing.NS, "lingerification_list");

    private static final ResourceLocation CREATE_POTION =
            new ResourceLocation("create", "potion");

    /** Pack design: Lingerification runs in 1500 mB increments (matches the IE-Mixer potion amount). */
    private static final int POTION_AMOUNT = 1500;

    /** Dragon's breath is a catalyst-style reagent: a quarter-bucket per operation. */
    private static final int DRAGON_BREATH_AMOUNT = 250;

    /** Between IE's own biodiesel (80) and phenolic resin (240). Tunable. */
    private static final int ENERGY = 120;

    private static final TagKey<Fluid> DRAGON_BREATH_TAG = TagKey.create(
            Registry.FLUID_REGISTRY, new ResourceLocation("forge", "fluid/dragon_breath"));

    private static boolean registered = false;

    private Lingerification() {}

    /** Register the generated-list generator. Idempotent; call from common setup. */
    public static void registerListGenerator() {
        if (registered) return;
        registered = true;
        try {
            GeneratedListRecipe.LIST_GENERATORS.put(
                    LIST_ID,
                    GeneratedListRecipe.RecipeListGenerator.simple(
                            Lingerification::generate,
                            RefineryRecipe.SERIALIZER.getId(),
                            IERecipeTypes.REFINERY));
            LOGGER.info("[CE] Lingerification: registered refinery list generator {}", LIST_ID);
        } catch (Throwable t) {
            LOGGER.error("[CE] Lingerification: failed to register refinery list generator", t);
        }
    }

    /** Build one REGULAR->LINGERING and one SPLASH->LINGERING refinery recipe per registered potion. */
    private static List<RefineryRecipe> generate() {
        List<RefineryRecipe> out = new ArrayList<>();
        for (Potion potion : ForgeRegistries.POTIONS) {
            if (potion == Potions.EMPTY || potion == Potions.WATER) continue;
            ResourceLocation id = ForgeRegistries.POTIONS.getKey(potion);
            if (id == null) continue;
            String slug = id.toString().replaceAll("[^a-z0-9_.-]", "_");

            out.add(build(potion, BottleType.REGULAR, slug + "_from_regular"));
            out.add(build(potion, BottleType.SPLASH, slug + "_from_splash"));
        }
        LOGGER.info("[CE] Lingerification: generated {} refinery recipes", out.size());
        return out;
    }

    private static RefineryRecipe build(Potion potion, BottleType fromType, String path) {
        FluidStack in = PotionFluidHandler.getFluidFromPotion(potion, fromType, POTION_AMOUNT);
        FluidStack lingering =
                PotionFluidHandler.getFluidFromPotion(potion, BottleType.LINGERING, POTION_AMOUNT);
        CompoundTag inTag = in.getTag();

        FluidTagInput input0 = new FluidTagInput(
                Either.right(List.of(CREATE_POTION)), POTION_AMOUNT, inTag);
        FluidTagInput input1 = new FluidTagInput(DRAGON_BREATH_TAG, DRAGON_BREATH_AMOUNT);

        return new RefineryRecipe(
                new ResourceLocation(CreativeBrewing.NS, "lingerification/" + path),
                lingering, input0, input1, Ingredient.EMPTY, ENERGY);
    }
}
