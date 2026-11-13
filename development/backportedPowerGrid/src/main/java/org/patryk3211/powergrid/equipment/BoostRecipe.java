// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
// Port (P9d): Create 0.5.1's ItemApplicationRecipe matches on a RecipeWrapper (Create 6: Container). The recipe type is
// Create's deploying type (DeployerApplicationRecipe), so deployers on belts and depots, Create's JEI Deploying
// category and the hand path (ItemBoostUtils.findRecipe) all find it; only the serializer is Power Grid's.
package org.patryk3211.powergrid.equipment;

import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;

public class BoostRecipe extends DeployerApplicationRecipe {
    public static final RecipeSerializer<BoostRecipe> SERIALIZER = new ProcessingRecipeSerializer<>(BoostRecipe::new);

    public BoostRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(params);
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level level) {
        return super.matches(inv, level) && !ItemBoostUtils.isBoosted(inv.getItem(0));
    }

    @Override
    public List<ItemStack> rollResults(List<ProcessingOutput> rollableResults) {
        List<ItemStack> results = new ArrayList<>();
        ProcessingOutput output = rollableResults.get(0);
        ItemStack stack = output.rollOutput();
        if (!stack.isEmpty()) {
            ItemBoostUtils.setBoosted(stack, true);
            results.add(stack);
        }
        return results;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
