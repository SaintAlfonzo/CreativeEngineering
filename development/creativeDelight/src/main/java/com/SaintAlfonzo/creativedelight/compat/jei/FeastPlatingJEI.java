package com.SaintAlfonzo.creativedelight.compat.jei;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/** JEI page for {@link FeastPlatingCategory}: right-clicking a placed feast. Inert without Farmer's Delight. */
@ParametersAreNonnullByDefault
@JeiPlugin
public class FeastPlatingJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeDelight.NS, "feast_plating");

    private boolean active;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        active = ModList.get().isLoaded("farmersdelight");
        if (active) {
            registration.addRecipeCategories(new FeastPlatingCategory(registration.getJeiHelpers().getGuiHelper()));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!active) {
            return;
        }
        List<FeastPlatingCategory.Display> feasts = FeastPlatingCategory.collect();
        registration.addRecipes(FeastPlatingCategory.TYPE, feasts);
        LOGGER.info("[jei] Feast Plating: {} page(s)", feasts.size());
    }
}
