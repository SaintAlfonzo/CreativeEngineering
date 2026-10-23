package com.SaintAlfonzo.more_mooblooms.compat.jei;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.item.MoobloomSpawnEggItem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * "Moobloom Breeding" (4.8): parent egg + parent egg, each with its food below, -> every calf outcome with its
 * chance. Pages: each combination pair (its results and the miss), each variant that breeds true, and one page
 * for two different kinds with no combination (a random calf).
 */
public class MoobloomBreedingCategory implements IRecipeCategory<MoobloomBreedingCategory.Recipe> {

    /** {@code random}: parents are "any two different kinds" (plain eggs). Food lists may be empty. */
    public record Recipe(ItemStack parentA, ItemStack parentB, List<ItemStack> foodA, List<ItemStack> foodB,
                         List<Outcome> outcomes, boolean random) {}

    /**
     * One calf slot. A single egg is that calf at {@code chance}; several eggs are a random calf at {@code chance},
     * each egg its {@code shares} part of it.
     */
    public record Outcome(List<ItemStack> eggs, List<Double> shares, double chance) {
        public static Outcome of(ItemStack egg, double chance) {
            return new Outcome(List.of(egg), List.of(1.0), chance);
        }
    }

    public static final RecipeType<Recipe> TYPE = RecipeType.create(MoreMooblooms.ID, "breeding", Recipe.class);

    /** Fits three outcomes (combination results plus the miss). */
    private static final int WIDTH = 150, HEIGHT = 40;
    private static final int SLOT_Y = 2, FOOD_Y = 22, OUT_X = 70, OUT_STEP = 27;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawable icon;

    public MoobloomBreedingCategory(IGuiHelper gui) {
        background = gui.createBlankDrawable(WIDTH, HEIGHT);
        slot = gui.getSlotDrawable();
        icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get()));
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.more_mooblooms.breeding");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        parent(builder, 1, recipe.parentA(), recipe.foodA(), recipe.random());
        parent(builder, 31, recipe.parentB(), recipe.foodB(), recipe.random());
        for (int i = 0; i < recipe.outcomes().size(); i++) {
            Outcome o = recipe.outcomes().get(i);
            IRecipeSlotBuilder out = builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X + i * OUT_STEP, SLOT_Y)
                    .addItemStacks(o.eggs()).setBackground(slot, -1, -1);
            if (o.eggs().size() > 1) {
                out.addTooltipCallback((view, tooltip) -> view.getDisplayedItemStack().ifPresent(shown -> {
                    for (int e = 0; e < o.eggs().size(); e++) {
                        if (ItemStack.isSameItemSameTags(o.eggs().get(e), shown)) {
                            tooltip.add(Component.translatable("jei.more_mooblooms.breeding.random",
                                    MoreMoobloomsJeiPlugin.percent(o.shares().get(e)),
                                    MoreMoobloomsJeiPlugin.percent(o.chance() * o.shares().get(e))));
                        }
                    }
                }));
            }
        }
    }

    private void parent(IRecipeLayoutBuilder builder, int x, ItemStack egg, List<ItemStack> food, boolean random) {
        IRecipeSlotBuilder parent = builder.addSlot(RecipeIngredientRole.INPUT, x, SLOT_Y).addItemStack(egg).setBackground(slot, -1, -1);
        if (random) parent.addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.more_mooblooms.breeding.any_kind")));
        if (!food.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, FOOD_Y).addItemStacks(food).setBackground(slot, -1, -1)
                    .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.more_mooblooms.breeding.food")));
        }
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        font.draw(pose, "+", 21, SLOT_Y + 5, 0x404040);
        font.draw(pose, "→", 53, SLOT_Y + 5, 0x404040);
        for (int i = 0; i < recipe.outcomes().size(); i++) {
            String text = MoreMoobloomsJeiPlugin.percent(recipe.outcomes().get(i).chance()) + "%";
            int centre = OUT_X + i * OUT_STEP + 8;
            font.draw(pose, text, centre - font.width(text) / 2.0F, SLOT_Y + 21, 0x404040);
        }
    }
}
