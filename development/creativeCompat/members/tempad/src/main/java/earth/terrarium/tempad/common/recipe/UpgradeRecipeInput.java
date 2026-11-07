// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/recipe/TempadUpgradeRecipe.kt (UpgradeRecipeInput). Translated
// to Java for Forge 1.19.2: RecipeInput -> a read-only two-slot Container (slot 0 the device, slot 1 the upgrade).
package earth.terrarium.tempad.common.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record UpgradeRecipeInput(ItemStack upgradable, ItemStack upgrade) implements Container {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> upgradable;
            case 1 -> upgrade;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return upgradable.isEmpty() && upgrade.isEmpty();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
