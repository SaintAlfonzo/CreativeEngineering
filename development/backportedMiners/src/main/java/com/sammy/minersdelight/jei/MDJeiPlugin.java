package com.sammy.minersdelight.jei;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.client.CopperPotScreen;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.sammy.minersdelight.registry.MDItems;
import com.sammy.minersdelight.registry.MDMenuTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.List;

/** The Copper Pot's JEI category, catalyst, click area and [+] transfer. */
@JeiPlugin
public class MDJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = MinersDelightMod.path("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CopperPotCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        List<CookingPotRecipe> fits = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COOKING.get()).stream()
                .filter(recipe -> recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).count() <= CopperPotBlockEntity.INPUT_SLOTS)
                .toList();
        registration.addRecipes(CopperPotCategory.TYPE, fits);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(MDItems.COPPER_POT.get()), CopperPotCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CopperPotScreen.class, 78, 28, 28, 15, CopperPotCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(CopperPotMenu.class, MDMenuTypes.COPPER_POT.get(), CopperPotCategory.TYPE,
                0, CopperPotBlockEntity.INPUT_SLOTS, CopperPotBlockEntity.OUTPUT_SLOT + 1, 36);
    }
}
