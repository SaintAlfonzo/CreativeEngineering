package net.ribs.vintagedelight.compat;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.block.custom.MasonJarBlock;
import net.ribs.vintagedelight.item.ModItems;
import net.ribs.vintagedelight.recipe.FermentingRecipe;
import net.ribs.vintagedelight.screen.FermentingJarScreen;

@JeiPlugin
public class JEIVintagePlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("vintagedelight", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FermentingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(new ItemStack(ModBlocks.SALT_BLOCK.get()), VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.salt_block"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.EVAPORATOR.get()), VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.evaporator"));
        registration.addIngredientInfo(new ItemStack(ModItems.CHEESE_WHEEL.get()), VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.cheese_wheel"));
        registration.addIngredientInfo(new ItemStack(ModItems.ORGANIC_MASH.get()), VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.organic_compost"));
        // Pack pages (jei/jei-coverage #15): salt layer growth, the Cheese Mold, jar stacking
        registration.addIngredientInfo(new ItemStack(ModBlocks.SALT_LAYER.get()), VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.salt_layer"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.CHEESE_MOLD.get()), VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.cheese_mold"));
        List<ItemStack> jars = ModBlocks.BLOCKS.getEntries().stream()
            .map(entry -> entry.get())
            .filter(block -> block instanceof MasonJarBlock)
            .map(ItemStack::new)
            .filter(stack -> !stack.isEmpty())
            .toList();
        if (!jars.isEmpty()) {
            registration.addIngredientInfo(jars, VanillaTypes.ITEM_STACK, Component.translatable("vintagedelight.jei.info.jar_stacking"));
        }
        if (Minecraft.getInstance().level != null) {
            List<FermentingRecipe> recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(FermentingRecipe.Type.INSTANCE);
            registration.addRecipes(FermentingCategory.FERMENTING_TYPE, recipes);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(FermentingJarScreen.class, 90, 47, 30, 20, FermentingCategory.FERMENTING_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FERMENTING_JAR.get()), FermentingCategory.FERMENTING_TYPE);
    }
}
