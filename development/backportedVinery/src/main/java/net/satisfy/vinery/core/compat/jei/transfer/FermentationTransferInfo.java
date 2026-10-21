package net.satisfy.vinery.core.compat.jei.transfer;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.satisfy.vinery.core.menu.FermentationBarrelMenu;
import net.satisfy.vinery.core.compat.jei.VineryJEIPlugin;
import net.satisfy.vinery.core.compat.jei.category.FermentationBarrelCategory;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;
import net.satisfy.vinery.core.registry.MenuTypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FermentationTransferInfo implements IRecipeTransferInfo<FermentationBarrelMenu, FermentationBarrelRecipe> {
    @Override
    public @NotNull Class<? extends FermentationBarrelMenu> getContainerClass() {
        return FermentationBarrelMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<FermentationBarrelMenu>> getMenuType() {
        return Optional.of(MenuTypeRegistry.FERMENTATION_BARREL_MENU.get());
    }

    @Override
    public @NotNull RecipeType<FermentationBarrelRecipe> getRecipeType() {
        return FermentationBarrelCategory.FERMENTATION_BARREL;
    }

    @Override
    public boolean canHandle(FermentationBarrelMenu container, FermentationBarrelRecipe recipe) {
        return true;
    }

    /**
     * Port: in the order {@link VineryJEIPlugin#buildSlotsFromRecipe} adds the page's input slots, which JEI pairs up
     * one by one: juice (slot 0, only when the recipe needs juice), the ingredients (1-3), the wine bottle (4).
     */
    @Override
    public @NotNull List<Slot> getRecipeSlots(FermentationBarrelMenu container, FermentationBarrelRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        if (recipe.getJuiceData().amount() > 0) {
            slots.add(container.getSlot(0));
        }
        for (int i = 1; i <= recipe.getIngredients().size() && i < 4; i++) {
            slots.add(container.getSlot(i));
        }
        slots.add(container.getSlot(4));
        return slots;
    }

    @Override
    public @NotNull List<Slot> getInventorySlots(FermentationBarrelMenu container, FermentationBarrelRecipe recipe) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 6; i < 6 + 36; i++) {
            Slot slot = container.getSlot(i);
            slots.add(slot);
        }
        return slots;
    }
}
