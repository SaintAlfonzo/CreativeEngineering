package com.SaintAlfonzo.creativebrewing.mixin.create;

import com.SaintAlfonzo.creativebrewing.fluidbrewing.PotionMixDedup;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Create's {@code PotionMixingRecipes.createRecipes()} phase 4 walks
 * {@code BrewingRecipeRegistry.getRecipes()}. The pack re-adds Alex's Mobs' modifier steps as
 * {@code PotionBrewing.Mix} entries (phase 2 already covers them), so without this every AM step
 * produces a duplicate Automatic-Brewing / Mechanical-Mixer recipe. Drop the covered ones.
 * See {@link PotionMixDedup}.
 */
@Mixin(value = PotionMixingRecipes.class, remap = false)
public class PotionMixingRecipesMixin {

    @Redirect(
            method = "createRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/brewing/BrewingRecipeRegistry;getRecipes()Ljava/util/List;"
            )
    )
    private static List<IBrewingRecipe> creativeengineering$dedupBrewingRecipes() {
        return PotionMixDedup.withoutMixDuplicates(BrewingRecipeRegistry.getRecipes());
    }
}
