package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.compat.jei.CraftableFirstRecipeLayoutList;
import mezz.jei.gui.recipes.RecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.layouts.IRecipeLayoutList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Craftable-first recipe order for JEI 11.8.1 (backport of JEI 19's {@code RecipeSorterStage.CRAFTABLE};
 * see {@link CraftableFirstRecipeLayoutList}).
 *
 * <p>11.8.1's {@code createRecipeLayoutsWithButtons} ends with {@code IRecipeLayoutList.create(menu, player,
 * layouts)}, which ignores the menu and player and returns an {@code UnsortedRecipeLayoutList}; the redirect
 * swaps in the sorted list when it applies.
 *
 * <p>11.8.1 also only rebuilds its cached list on a new lookup or a category change, so a list sorted for one
 * menu could be reused over another. JEI 19 rebuilds when the container id or sorter stages change; the head
 * inject does the same for the menu and the config toggle.
 */
@Mixin(value = RecipeGuiLogic.class, remap = false)
public abstract class JeiRecipeGuiLogicMixin {

    @Shadow
    @Nullable
    private IRecipeLayoutList cachedRecipeLayoutsWithButtons;

    @Unique
    @Nullable
    private AbstractContainerMenu creativeengineering$cachedForMenu;

    @Unique
    private boolean creativeengineering$cachedCraftableFirst;

    @Inject(method = "getVisibleRecipeLayoutsWithButtons", at = @At("HEAD"))
    private void creativeengineering$dropStaleOrder(int availableHeight, int minRecipePadding,
                                                    @Nullable AbstractContainerMenu container,
                                                    CallbackInfoReturnable<List<RecipeLayoutWithButtons<?>>> cir) {
        boolean craftableFirst = CEConfig.JEI.craftableFirst.get();
        if (container != creativeengineering$cachedForMenu || craftableFirst != creativeengineering$cachedCraftableFirst) {
            cachedRecipeLayoutsWithButtons = null;
            creativeengineering$cachedForMenu = container;
            creativeengineering$cachedCraftableFirst = craftableFirst;
        }
    }

    @Redirect(
            method = "createRecipeLayoutsWithButtons",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/gui/recipes/layouts/IRecipeLayoutList;create("
                           + "Lnet/minecraft/world/inventory/AbstractContainerMenu;"
                           + "Lnet/minecraft/world/entity/player/Player;"
                           + "Ljava/util/List;)Lmezz/jei/gui/recipes/layouts/IRecipeLayoutList;"
            )
    )
    private IRecipeLayoutList creativeengineering$craftableFirst(@Nullable AbstractContainerMenu menu, @Nullable Player player,
                                                                List<? extends RecipeLayoutWithButtons<?>> layouts) {
        IRecipeLayoutList sorted = CraftableFirstRecipeLayoutList.sortIfCraftingMenu(menu, player, layouts);
        return sorted != null ? sorted : IRecipeLayoutList.create(menu, player, layouts);
    }
}
