package net.satisfy.vinery.core.recipe.input;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Port: 1.21's RecipeInput as a container (slot 0 the wine bottle, then the ingredients) plus the barrel's juice. */
public class FermentationBarrelRecipeInput extends SimpleContainer {
    public static final int WINE_BOTTLE_SLOT = 0;
    private final List<ItemStack> ingredientStacks;
    private final JuiceData data;

    public FermentationBarrelRecipeInput(List<ItemStack> ingredientStacks, ItemStack wineBottle, JuiceData data) {
        super(ingredientStacks.size() + 1);
        this.ingredientStacks = ingredientStacks;
        this.data = data;
        setItem(WINE_BOTTLE_SLOT, wineBottle);
        for (int i = 0; i < ingredientStacks.size(); i++) {
            setItem(i + 1, ingredientStacks.get(i));
        }
    }

    public List<ItemStack> getIngredientSlots() {
        return ingredientStacks;
    }

    public JuiceData data() {
        return data;
    }

    public record JuiceData(String type, int amount) {
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(type);
            buf.writeInt(amount);
        }

        public static JuiceData read(FriendlyByteBuf buf) {
            return new JuiceData(buf.readUtf(), buf.readInt());
        }
    }
}
