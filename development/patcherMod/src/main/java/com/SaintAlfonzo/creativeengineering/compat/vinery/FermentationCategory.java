package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.util.Comparator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * {@code vinery:wine_fermentation} recipes in the Fermentation (Aging) Barrel. Layout copies
 * Vinery's REI FermentationBarrelCategory: a 2x2 ingredient grid with unused cells still drawn,
 * the wine bottle the barrel always needs (slot 0) below-right of it, an arrow that fills over
 * the 1800-tick fermentation, then the result. Recipes are read from the recipe manager, so
 * datapack/KubeJS fermentation recipes show up too.
 */
@ParametersAreNonnullByDefault
class FermentationCategory extends VineryCategory<FermentationCategory.Display> {
    static final RecipeType<Display> TYPE =
            new RecipeType<>(new ResourceLocation(CreativeEngineering.MODID, "vinery_fermentation"), Display.class);

    /** REI slot points shifted by (+9, +5) so the top-left slot frame sits at 0,0. */
    private static final int[][] GRID = {{1, 1}, {19, 1}, {1, 19}, {19, 19}};
    private static final int BOTTLE_X = 45, BOTTLE_Y = 23;
    private static final int ARROW_X = 63, ARROW_Y = 4;
    private static final int RESULT_X = 99, RESULT_Y = 5;

    /** One fermentation recipe; {@code inputs} holds 1-4 ingredients, without the wine bottle. */
    record Display(ResourceLocation id, List<Ingredient> inputs, ItemStack output) {
    }

    private final IDrawableAnimated arrow;

    FermentationCategory(IGuiHelper gui) {
        // REI's title key; Vinery's lang names it "Aging Barrel" (the block's display name).
        super(gui, TYPE, Component.translatable("rei.vinery.fermentation_barrel_category"),
                VineryIds.FERMENTATION_BARREL, 120, 40);
        this.arrow = animatedArrow(VineryIds.FERMENTATION_TICKS);
    }

    static List<Display> collect() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        net.minecraft.world.item.crafting.RecipeType<?> type =
                ForgeRegistries.RECIPE_TYPES.getValue(VineryIds.FERMENTATION_TYPE);
        if (connection == null || type == null) {
            return List.of();
        }
        return connection.getRecipeManager().getRecipes().stream()
                .filter(r -> r.getType() == type)
                .filter(r -> !r.getResultItem().isEmpty())
                .sorted(Comparator.comparing(r -> r.getId().toString()))
                .map(r -> new Display(r.getId(), List.copyOf(r.getIngredients()), r.getResultItem().copy()))
                .toList();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.inputs().size() && i < GRID.length; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, GRID[i][0], GRID[i][1])
                    .addIngredients(recipe.inputs().get(i));
        }
        ItemStack bottle = VineryIds.stack(VineryIds.WINE_BOTTLE);
        if (!bottle.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, BOTTLE_X, BOTTLE_Y).addItemStack(bottle);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, RESULT_Y).addItemStack(recipe.output());
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView view, PoseStack pose, double mouseX, double mouseY) {
        for (int[] cell : GRID) {
            drawSlot(pose, cell[0], cell[1]);
        }
        drawSlot(pose, BOTTLE_X, BOTTLE_Y);
        drawArrow(pose, arrow, ARROW_X, ARROW_Y);
        drawBigSlot(pose, RESULT_X, RESULT_Y);
    }

    @Override
    public List<Component> getTooltipStrings(Display recipe, IRecipeSlotsView view, double mouseX, double mouseY) {
        return tooltipIf(over(mouseX, mouseY, ARROW_X, ARROW_Y, ARROW_W, ARROW_H),
                ticksAsSeconds(VineryIds.FERMENTATION_TICKS));
    }

    @Override
    public ResourceLocation getRegistryName(Display recipe) {
        return recipe.id();
    }
}
