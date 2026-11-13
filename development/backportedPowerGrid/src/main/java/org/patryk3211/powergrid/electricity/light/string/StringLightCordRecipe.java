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
package org.patryk3211.powergrid.electricity.light.string;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.collections.ModdedItems;

import java.util.ArrayList;

// Port: 1.19.2 has no CraftingBookCategory, RegistryAccess arguments or CraftingContainer.getItems(): the 1.19.2
// CustomRecipe / SimpleRecipeSerializer constructors and an index loop over the grid. Same matching and result.
public class StringLightCordRecipe extends CustomRecipe {
    public static final RecipeSerializer<StringLightCordRecipe> SERIALIZER = new SimpleRecipeSerializer<>(StringLightCordRecipe::new);

    public StringLightCordRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        var hasCord = false;
        for(int i = 0; i < container.getContainerSize(); ++i) {
            var stack = container.getItem(i);
            if(ModdedItems.STRING_LIGHT_CORD.isIn(stack)) {
                if(hasCord)
                    return false;
                hasCord = true;
            } else if(!stack.isEmpty() && !(stack.getItem() instanceof DyeItem)) {
                return false;
            }
        }
        return hasCord;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        var result = ModdedItems.STRING_LIGHT_CORD.asStack();
        var colors = new ArrayList<Byte>();
        for(int i = 0; i < container.getContainerSize(); ++i) {
            if(container.getItem(i).getItem() instanceof DyeItem dye) {
                colors.add((byte) dye.getDyeColor().ordinal());
            }
        }
        // Port (B34): a cord with no dye clears the pattern to a plain cord. Upstream wrote an empty Pattern tag, so the
        // result never stacked with new cords (and showed no pattern either).
        if(colors.isEmpty())
            return result;
        var tag = result.getOrCreateTag();
        tag.putByteArray("Pattern", colors);
        return result;
    }

    @Override
    public ItemStack getResultItem() {
        return ModdedItems.STRING_LIGHT_CORD.asStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 0 && height > 0;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
