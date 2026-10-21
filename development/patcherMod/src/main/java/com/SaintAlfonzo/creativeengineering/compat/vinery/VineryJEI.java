package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * JEI pages for Vinery, which on 1.19.2 (1.3.x) only ships an REI plugin. Four categories:
 * Aging Barrel fermentation (the {@code vinery:wine_fermentation} recipe type, REI layout),
 * Apple Press (REI layout), Grape Stomping and Wine Aging (both hardcoded in Vinery and not in
 * its REI plugin). Inert when Vinery is not loaded; never touches Vinery classes directly.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class VineryJEI implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, "vinery");
    private static final Logger LOGGER = LogManager.getLogger("creativeengineering");

    private boolean active;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // The Vinery backport ships all of these pages itself (barrel, both press stages, stomping, aging).
        active = ModList.get().isLoaded(VineryIds.MODID) && !isBackport();
        if (!active) {
            return;
        }
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new FermentationCategory(gui),
                new WinePressCategory(gui),
                new GrapeStompingCategory(gui),
                new WineAgingCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!active) {
            return;
        }
        List<FermentationCategory.Display> fermentation = FermentationCategory.collect();
        registration.addRecipes(FermentationCategory.TYPE, fermentation);
        registration.addRecipes(WinePressCategory.TYPE, WinePressCategory.collect());
        registration.addRecipes(GrapeStompingCategory.TYPE, GrapeStompingCategory.collect());
        registration.addRecipes(WineAgingCategory.TYPE, WineAgingCategory.collect(fermentation));
        if (fermentation.isEmpty()) {
            LOGGER.warn("[CE] Vinery JEI: no {} recipes found; fermentation and aging pages are empty",
                    VineryIds.FERMENTATION_TYPE);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (!active) {
            return;
        }
        catalyst(registration, VineryIds.FERMENTATION_BARREL, FermentationCategory.TYPE, WineAgingCategory.TYPE);
        catalyst(registration, VineryIds.WINE_PRESS, WinePressCategory.TYPE);
        catalyst(registration, VineryIds.GRAPEVINE_POT, GrapeStompingCategory.TYPE);
    }

    private static boolean isBackport() {
        try {
            Class.forName("net.satisfy.vinery.forge.VineryForge", false, VineryJEI.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private static void catalyst(IRecipeCatalystRegistration registration, ResourceLocation item,
                                 RecipeType<?>... types) {
        ItemStack stack = VineryIds.stack(item);
        if (stack.isEmpty()) {
            LOGGER.warn("[CE] Vinery JEI: catalyst {} is not registered", item);
            return;
        }
        registration.addRecipeCatalyst(stack, types);
    }
}
