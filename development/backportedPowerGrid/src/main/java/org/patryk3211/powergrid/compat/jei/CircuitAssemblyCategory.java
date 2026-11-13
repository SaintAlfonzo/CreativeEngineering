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
// Port: JEI 11 API (getBackground required; addSlot(role, x, y); the standard slot background is Create's rendered
// slot) and PoseStack drawing.
package org.patryk3211.powergrid.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.ItemIcon;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedTags;
import org.patryk3211.powergrid.utility.GuiGraphics;
import org.patryk3211.powergrid.utility.Lang;

public class CircuitAssemblyCategory implements IRecipeCategory<Object> {
    public static final ResourceLocation IDENTIFIER = PowerGrid.asResource("circuit_assembly");
    public static final RecipeType<Object> TYPE = new RecipeType<>(IDENTIFIER, Object.class);

    public static final ResourceLocation TEXTURE = PowerGrid.texture("gui/circuit_assembly_rei_overlay");
    private final AnimatedMechanicalArm arm = new AnimatedMechanicalArm();
    private final ItemIcon depot = new ItemIcon(AllBlocks.DEPOT::asStack);

    private final IDrawable icon;
    private final IDrawable background;

    public CircuitAssemblyCategory(IGuiHelper guiHelper) {
        icon = new ItemIcon(AllBlocks.MECHANICAL_ARM::asStack);
        background = guiHelper.createBlankDrawable(getWidth(), getHeight());
    }

    @NotNull
    @Override
    public RecipeType<Object> getRecipeType() {
        return TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Lang.translateDirect("recipe.category.circuit_assembly");
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
    public void setRecipe(IRecipeLayoutBuilder builder, Object recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 4 + 32)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(Ingredient.of(ModdedTags.Item.CIRCUIT_COMPONENT.tag));
        builder.addSlot(RecipeIngredientRole.INPUT, 95, 4 + 30 - 8)
                .addItemStack(ModdedItems.INCOMPLETE_CIRCUIT.asStack());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 4 + 60)
                .addItemStack(ModdedBlocks.CIRCUIT_BOARD.asStack());
    }

    @Override
    public int getHeight() {
        return 90;
    }

    @Override
    public int getWidth() {
        return 150;
    }

    private void renderScene(PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(60, 4 + 30, 0);

        arm.draw(poseStack, -14, -16);

        poseStack.pushPose();
        poseStack.translate(35 - 8 - 2,  -8 - 2, -10);
        poseStack.scale(2, 2, 1);
        depot.draw(poseStack, 0, 0);
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public void draw(Object recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        new GuiGraphics(poseStack).blit(TEXTURE, 0, 0, 0, 0, getWidth(), getHeight());
        renderScene(poseStack);

        arm.heldItemSupplier = () ->
            recipeSlotsView.getSlotViews().get(0).getDisplayedItemStack().orElse(ItemStack.EMPTY);
    }
}
