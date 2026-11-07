// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/widgets/CraftingRecipeWidget.kt. Translated to Java for Forge
// 1.19.2 on the widget kit (TP-D4): GuiGraphics item / sprite calls -> ItemRenderer GUI calls and Sprites.blit; the
// 1.21 widget tooltip -> tooltipAt (the screen draws it, PORT_NOTES 6.13). Upstream lays the ingredient list into a
// 2x2 grid when it has 4 or fewer entries and 3x3 otherwise, whatever the recipe's shape; kept.
package earth.terrarium.tempad.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.KitWidget;
import earth.terrarium.tempad.client.ui.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CraftingRecipeWidget extends KitWidget {
    private final Font font = Minecraft.getInstance().font;
    public final CraftingRecipe recipe;
    private int age = 0;

    public CraftingRecipeWidget(CraftingRecipe recipe) {
        super(90, 58);
        this.recipe = recipe;
    }

    @Nullable
    public static CraftingRecipeWidget create(ResourceLocation id) {
        if (Minecraft.getInstance().level == null) return null;
        Optional<? extends Recipe<?>> recipe = Minecraft.getInstance().level.getRecipeManager().byKey(id);
        if (recipe.isPresent() && recipe.get() instanceof CraftingRecipe craftingRecipe) return new CraftingRecipeWidget(craftingRecipe);
        return null;
    }

    private int tableSize() {
        return recipe.getIngredients().size() > 4 ? 3 : 2;
    }

    private ItemStack shown(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[(age / 30) % items.length];
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        RenderSystem.enableBlend();
        Sprites.blit(pose, TempadUI.element.get(true, false), x, y, 58, height);
        int tableSize = tableSize();
        for (int row = 0; row < tableSize; row++) {
            for (int col = 0; col < tableSize; col++) {
                if (row * tableSize + col >= recipe.getIngredients().size()) break;
                ItemStack stack = shown(recipe.getIngredients().get(row * tableSize + col));
                if (stack.isEmpty()) continue;
                itemRenderer.renderGuiItem(stack, x + 3 + col * 18, y + 3 + row * 18);
            }
        }
        Gfx.fill(pose, x + 58, y + 25, x + width - 20, y + 32, Tempad.ORANGE.getValue());
        RenderSystem.enableBlend();
        Sprites.blit(pose, TempadUI.element.get(true, false), x + width - 20, y + 18, 20, 20);
        ItemStack resultItem = recipe.getResultItem();
        itemRenderer.renderGuiItem(resultItem, x + width - 18, y + 20);
        itemRenderer.renderGuiItemDecorations(font, resultItem, x + width - 18, y + 20);
    }

    @Nullable
    @Override
    public Component tooltipAt(double mouseX, double mouseY) {
        if (!visible || !isHovered) return null;
        int tableSize = tableSize();
        for (int row = 0; row < tableSize; row++) {
            for (int col = 0; col < tableSize; col++) {
                if (row * tableSize + col >= recipe.getIngredients().size()) break;
                ItemStack stack = shown(recipe.getIngredients().get(row * tableSize + col));
                if (stack.isEmpty()) continue;
                int itemX = x + 3 + col * 18;
                int itemY = y + 3 + row * 18;
                if (mouseX > itemX - 1 && mouseX < itemX + 17 && mouseY > itemY - 1 && mouseY < itemY + 17) return stack.getHoverName();
            }
        }
        if (mouseX > x + width - 18 && mouseX < x + width && mouseY > y + 20 && mouseY < y + 38) return recipe.getResultItem().getHoverName();
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void tick() {
        age++;
    }
}
