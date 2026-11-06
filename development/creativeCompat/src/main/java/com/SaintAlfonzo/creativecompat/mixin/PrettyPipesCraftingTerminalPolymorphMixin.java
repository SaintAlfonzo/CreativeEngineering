package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.CraftingMenuPolymorphCompat;
import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import de.ellpeck.prettypipes.terminal.containers.CraftingTerminalContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
 * Lets Polymorph choose the recipe in the Pretty Pipes Crafting Terminal (see
 * {@link CraftingMenuPolymorphCompat}). Its {@code slotsChanged} (m_6199_) looks the recipe up
 * (m_44015_ is getRecipeFor); that lookup goes to Polymorph's player recipe data instead of the
 * first match. The grid is shared by everyone looking at the terminal, and each viewer's menu
 * recomputes with that viewer's own pick. Slot 0 is the result slot.
 */
@Mixin(value = CraftingTerminalContainer.class, remap = false)
public abstract class PrettyPipesCraftingTerminalPolymorphMixin implements CraftingMenuPolymorphCompat.PolymorphCraftingMenu {

    @Shadow(remap = false) @Final private Player player;

    @Redirect(method = "m_6199_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;m_44015_(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private <C extends Container, T extends Recipe<C>> Optional<T> creativeengineering$polymorphRecipe(
            RecipeManager manager, RecipeType<T> type, C inventory, Level level) {
        return RecipeSelection.getPlayerRecipe((AbstractContainerMenu) (Object) this, type, inventory, level, player);
    }

    @Override
    public void creativeengineering$refreshCraftingResult() {
        CraftingTerminalContainer self = (CraftingTerminalContainer) (Object) this;
        self.slotsChanged(self.craftInventory);
    }

    @Override
    public Slot creativeengineering$resultSlot() {
        return ((AbstractContainerMenu) (Object) this).slots.get(0);
    }
}
