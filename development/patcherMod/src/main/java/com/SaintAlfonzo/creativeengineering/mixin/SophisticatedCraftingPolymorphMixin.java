package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.SophisticatedPolymorphCompat;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingItemHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * Lets Polymorph choose the recipe in the Sophisticated crafting upgrade (see
 * {@link SophisticatedPolymorphCompat}). In {@code updateCraftingResult}:
 * <ul>
 *   <li>the {@code lastRecipe.matches} shortcut always fails (m_5818_ is matches), so the recipe is
 *       looked up on every grid change like the vanilla table does. Otherwise the cached recipe
 *       would win over a different one picked in Polymorph. {@code lastRecipe} is still set from
 *       the lookup and still used for the crafting remainders when the result is taken;</li>
 *   <li>the lookup itself goes to Polymorph's player recipe data instead of the first match.</li>
 * </ul>
 */
@Mixin(value = CraftingUpgradeContainer.class, remap = false)
public abstract class SophisticatedCraftingPolymorphMixin implements SophisticatedPolymorphCompat.RefreshableCrafting {

    @Shadow(remap = false) @Final private CraftingItemHandler craftMatrix;

    @Shadow(remap = false)
    private void onCraftMatrixChanged(Container container) {}

    @Redirect(method = "updateCraftingResult", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;m_5818_(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z"))
    private boolean creativeengineering$skipCachedRecipe(CraftingRecipe recipe, Container inventory, Level level) {
        return false;
    }

    @Redirect(method = "updateCraftingResult", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/p3pp3rf1y/sophisticatedcore/util/RecipeHelper;safeGetRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private <C extends Container, T extends Recipe<C>> Optional<T> creativeengineering$polymorphRecipe(
            RecipeType<T> type, C inventory, Level level,
            Level world, Player player, CraftingContainer matrix, ResultContainer result, ResultSlot resultSlot) {
        return RecipeSelection.getPlayerRecipe(player.containerMenu, type, inventory, level, player);
    }

    @Override
    public void creativeengineering$refreshCraftingResult() {
        onCraftMatrixChanged(craftMatrix);
    }
}
