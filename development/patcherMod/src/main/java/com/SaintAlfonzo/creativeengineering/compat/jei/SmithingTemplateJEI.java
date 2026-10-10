package com.SaintAlfonzo.creativeengineering.compat.jei;

import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraftforge.fml.ModList;

/**
 * Makes JEI show smithing the way Armor Trims' smithing table actually works.
 *
 * <p>Armor Trims' table needs the Netherite Upgrade Smithing Template for any recipe whose material is a
 * netherite ingot, but JEI's vanilla Smithing page lists those recipes in a two-slot, template-less layout
 * (diamond shield + netherite ingot) that the table refuses. And Armor Trims' own JEI pages are empty: its
 * plugin crashes on a recipe-type mismatch ({@link ArmorTrimsSmithing}). So:
 * <ol>
 *   <li>{@link #registerRecipes}: fills Armor Trims' Armor Trimming page from its own maker, and its Item
 *       Upgrading page with base + template + material for every template-only smithing recipe;</li>
 *   <li>{@link #onRuntimeAvailable}: hides exactly those re-shown recipes from the vanilla Smithing page.
 *       Nothing is hidden unless step 1 succeeded for it.</li>
 * </ol>
 * Re-run on every JEI reload and reads Armor Trims' config each time, so turning
 * {@code "Disable Vanilla Netherite Upgrading"} off brings the vanilla layout back.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class SmithingTemplateJEI implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ID = new ResourceLocation(CreativeEngineering.MODID, "smithing_template");

    /** Ids of smithing recipes now shown on Armor Trims' Item Upgrading page (set by the last reload). */
    private Set<ResourceLocation> reshown = Set.of();

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        reshown = Set.of();
        if (!ModList.get().isLoaded("armor_trims") || Minecraft.getInstance().level == null) {
            return;
        }
        try {
            if (!ArmorTrimsSmithing.active()) {
                return;
            }
            int trims = ArmorTrimsSmithing.registerTrims(registration);
            List<UpgradeRecipe> smithing = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
            reshown = ArmorTrimsSmithing.registerUpgrades(registration, smithing);
            LOGGER.info("[jei] Armor Trims pages: {} trim recipe(s), {} template upgrade recipe(s)", trims, reshown.size());
        } catch (Throwable t) {
            reshown = Set.of();
            LOGGER.warn("[jei] couldn't fill Armor Trims' JEI pages, leaving the vanilla Smithing page as is", t);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (reshown.isEmpty()) {
            return;
        }
        try {
            IRecipeManager recipes = runtime.getRecipeManager();
            List<UpgradeRecipe> hide = recipes.createRecipeLookup(RecipeTypes.SMITHING).get()
                    .filter(r -> reshown.contains(r.getId()))
                    .toList();
            if (!hide.isEmpty()) {
                recipes.hideRecipes(RecipeTypes.SMITHING, hide);
            }
            LOGGER.info("[jei] hid {} template-only smithing recipe(s) from the vanilla Smithing page; "
                    + "they're on Armor Trims' Item Upgrading page with the template", hide.size());
        } catch (Throwable t) {
            LOGGER.warn("[jei] couldn't hide template-only smithing recipes", t);
        }
    }
}
