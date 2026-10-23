package com.SaintAlfonzo.creativeengineering.compat.jei.cycling;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Crafting pages for the special recipes JEI can't draw (feature_writeup/jei_coverage.md part 1):
 * one cycling page per recipe kind, built by {@link CyclingCraftingPages} from the mods' own recipes.
 * Also hides AE2's facades from the ingredient list (thousands of generated items); the facade page
 * (uses of the cable anchor) shows how to make them.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class CyclingCraftingJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, "cycling_crafting");
    private static final ResourceLocation FACADE = new ResourceLocation("ae2", "facade");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(CyclingCraftingDisplay.class, CyclingCraftingExtension::new);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        long start = System.nanoTime();
        List<CyclingCraftingDisplay> pages = CyclingCraftingPages.build(Minecraft.getInstance().level);
        registration.addRecipes(RecipeTypes.CRAFTING, List.copyOf(pages));
        LOGGER.info("[jei cycle] built in {} ms", (System.nanoTime() - start) / 1_000_000);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (!ForgeRegistries.ITEMS.containsKey(FACADE)) {
            return;
        }
        IIngredientManager ingredients = runtime.getIngredientManager();
        List<ItemStack> facades = ingredients.getAllIngredients(VanillaTypes.ITEM_STACK).stream()
                .filter(s -> FACADE.equals(ForgeRegistries.ITEMS.getKey(s.getItem())))
                .toList();
        if (!facades.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, facades);
            LOGGER.info("[jei cycle] hid {} AE2 facades from the ingredient list", facades.size());
        }
    }
}
