package com.SaintAlfonzo.creativecompat.mixin;

import blusunrize.immersiveengineering.common.gui.CraftingTableMenu;
import com.SaintAlfonzo.creativecompat.compat.CraftingMenuPolymorphCompat;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * Lets Polymorph choose the recipe in the IE Engineer's Crafting Table (see
 * {@link CraftingMenuPolymorphCompat}). Its {@code slotsChanged} looks the recipe up inside a
 * {@code ContainerLevelAccess} lambda (IE is built with Mojang names, so the lambda keeps them);
 * that lookup (m_44015_ is getRecipeFor) goes to Polymorph's player recipe data instead of the
 * first match. Slot 0 is the result slot.
 */
@Mixin(value = CraftingTableMenu.class, remap = false)
public abstract class IECraftingTablePolymorphMixin implements CraftingMenuPolymorphCompat.PolymorphCraftingMenu {

    @Shadow(remap = false) @Final private CraftingContainer craftingInventory;
    @Shadow(remap = false) @Final private Player player;

    @Redirect(method = "lambda$slotsChanged$0", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;m_44015_(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private <C extends Container, T extends Recipe<C>> Optional<T> creativeengineering$polymorphRecipe(
            RecipeManager manager, RecipeType<T> type, C inventory, Level level) {
        return RecipeSelection.getPlayerRecipe((AbstractContainerMenu) (Object) this, type, inventory, level, player);
    }

    @Override
    public void creativeengineering$refreshCraftingResult() {
        ((AbstractContainerMenu) (Object) this).slotsChanged(craftingInventory);
    }

    @Override
    public Slot creativeengineering$resultSlot() {
        return ((AbstractContainerMenu) (Object) this).slots.get(0);
    }
}
