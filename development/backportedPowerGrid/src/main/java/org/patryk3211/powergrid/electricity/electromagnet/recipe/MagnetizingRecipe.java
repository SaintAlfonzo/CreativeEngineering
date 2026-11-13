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
package org.patryk3211.powergrid.electricity.electromagnet.recipe;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.utility.Lang;

import java.util.List;
import java.util.Set;

public class MagnetizingRecipe extends ProcessingRecipe<Container> implements IAssemblyRecipe {
    public static final TypeInfo TYPE_INFO = new TypeInfo();

    public MagnetizingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(TYPE_INFO, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 3;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionForAssembly() {
        return Lang.translateDirect("recipe.assembly.magnetizing");
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(ModdedBlocks.ELECTROMAGNET.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {

    }

    // Port: Create 0.5.1 signature (supplier of a JEI sub-category). Null as upstream: no sequenced assembly uses a
    // magnetizing step, so JEI never asks for it.
    @Override
    public java.util.function.Supplier<java.util.function.Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return null;
    }

    @Override
    public boolean matches(Container inventory, Level world) {
        if(inventory.isEmpty())
            return false;
        return ingredients.get(0).test(inventory.getItem(0));
    }
}
