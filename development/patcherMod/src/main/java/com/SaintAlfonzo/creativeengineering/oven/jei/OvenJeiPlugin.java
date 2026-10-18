package com.SaintAlfonzo.creativeengineering.oven.jei;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.SaintAlfonzo.creativeengineering.oven.BakingRecipe;
import com.SaintAlfonzo.creativeengineering.oven.OvenMenu;
import com.SaintAlfonzo.creativeengineering.oven.OvenRegistry;
import com.SaintAlfonzo.creativeengineering.oven.client.OvenScreen;

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

/** JEI page for {@code creativeengineering:baking}: category, oven as catalyst, arrow click area, "+" transfer. */
@JeiPlugin
public class OvenJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(CreativeEngineering.MODID, "oven");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BakingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        registration.addRecipes(BakingRecipeCategory.TYPE,
                Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(OvenRegistry.BAKING_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(OvenRegistry.OVEN_ITEM.get()), BakingRecipeCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(OvenScreen.class, OvenScreen.ARROW_X, OvenScreen.ARROW_Y,
                OvenScreen.ARROW_W, OvenScreen.ARROW_H, BakingRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Ingredient slots 0-5; the player inventory starts after the oven's 10 slots.
        registration.addRecipeTransferHandler(OvenMenu.class, OvenRegistry.OVEN_MENU.get(), BakingRecipeCategory.TYPE,
                0, BakingRecipe.INPUT_SLOTS, 10, 36);
    }
}
