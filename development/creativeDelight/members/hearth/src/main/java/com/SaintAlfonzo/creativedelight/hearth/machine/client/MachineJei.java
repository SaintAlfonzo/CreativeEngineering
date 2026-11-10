package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.machine.HHMachines;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The machines' JEI registration, called from compat/HHJeiPlugin (JEI is optional). */
public final class MachineJei {
    private MachineJei() {
    }

    public static void categories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new StompingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    public static void catalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(HHMachines.STOMPING_BASIN_ITEM.get()), StompingCategory.TYPE);
    }

    public static void recipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level != null) {
            registration.addRecipes(StompingCategory.TYPE, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(HHMachines.STOMPING_TYPE.get()));
        }
        info(registration, HHMachines.JUG_ITEM.get(), "jug");
        info(registration, HHMachines.TROUGH_ITEM.get(), "trough");
        info(registration, HHMachines.SPRINKLER_ITEM.get(), "sprinkler");
        info(registration, HHMachines.STOMPING_BASIN_ITEM.get(), "stomping_basin");
    }

    private static void info(IRecipeRegistration registration, Item item, String key) {
        registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, Component.translatable("hearthandharvest.jei.info." + key));
    }
}
