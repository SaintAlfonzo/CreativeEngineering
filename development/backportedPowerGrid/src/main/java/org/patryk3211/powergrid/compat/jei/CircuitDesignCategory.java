/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port: JEI 11 API (getBackground is required: a blank drawable of the old size; addSlot(role, x, y) for
// addInputSlot / addOutputSlot; item stacks for addItemLike) and PoseStack drawing.
package org.patryk3211.powergrid.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.ItemIcon;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedTags;
import org.patryk3211.powergrid.utility.GuiGraphics;
import org.patryk3211.powergrid.utility.Lang;

public class CircuitDesignCategory implements IRecipeCategory<Object> {
    public static final ResourceLocation IDENTIFIER = PowerGrid.asResource("circuit_design");
    public static final RecipeType<Object> TYPE = new RecipeType<>(IDENTIFIER, Object.class);
    public static final ResourceLocation TEXTURE = PowerGrid.texture("gui/circuit_design_rei_overlay");

    private final IDrawable icon;
    private final IDrawable background;

    public CircuitDesignCategory(IGuiHelper guiHelper) {
        icon = new ItemIcon(ModdedBlocks.CIRCUIT_DESIGN_TABLE::asStack);
        background = guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @Override
    public RecipeType<Object> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Lang.translateDirect("recipe.category.circuit_design");
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
    public int getWidth() {
        return 150;
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Object recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 38)
                .addItemStack(AllItems.EMPTY_SCHEMATIC.asStack());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 38)
                .addItemStack(ModdedItems.CIRCUIT_SCHEMATIC.asStack());
        builder.addSlot(RecipeIngredientRole.INPUT, 67, 15)
                .addIngredients(Ingredient.of(ModdedTags.Item.CIRCUIT_COMPONENT.tag));
    }

    @Override
    public void draw(Object recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        new GuiGraphics(poseStack).blit(TEXTURE, 0, 0, 0, 0, getWidth(), getHeight());

        poseStack.pushPose();
        poseStack.translate(75 - 24 - 3, 4 + 65 - 24 - 3, 0);
        poseStack.scale(3, 3, 1);
        icon.draw(poseStack, 0, 0);
        poseStack.popPose();
    }
}
