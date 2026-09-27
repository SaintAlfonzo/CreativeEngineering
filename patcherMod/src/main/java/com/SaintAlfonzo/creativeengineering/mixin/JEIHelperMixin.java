package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import com.SaintAlfonzo.creativeengineering.compat.AutomatedPotionMixingCategory;
import com.SaintAlfonzo.creativeengineering.compat.LingerificationCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * The pack-generated potion brews live in their own JEI categories
 * ({@link AutomatedPotionMixingCategory}, {@link LingerificationCategory}), so strip them out of
 * IE's generic Mixer / Refinery categories — redirect the {@code getFiltered(<X>.RECIPES, ...)}
 * calls in {@code JEIHelper.registerRecipes} to also drop the pack-generated recipes. Each
 * {@code @Slice} pins the redirect to the call that follows the matching {@code RECIPES} field
 * read.
 */
@Mixin(value = JEIHelper.class, remap = false)
public class JEIHelperMixin {

    @Redirect(
            method = "registerRecipes",
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lblusunrize/immersiveengineering/api/crafting/MixerRecipe;"
                                   + "RECIPES:Lblusunrize/immersiveengineering/api/crafting/cache/CachedRecipeList;"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/common/util/compat/jei/JEIHelper;"
                           + "getFiltered(Lblusunrize/immersiveengineering/api/crafting/cache/CachedRecipeList;"
                           + "Ljava/util/function/Predicate;)Ljava/util/List;",
                    ordinal = 0
            )
    )
    private List<MixerRecipe> creativeengineering$noPotionsInIeMixerCategory(
            JEIHelper self, CachedRecipeList<MixerRecipe> list, Predicate<MixerRecipe> ieFilter) {

        Level level = Minecraft.getInstance().level;
        Collection<MixerRecipe> all = level == null ? List.of() : list.getRecipes(level);
        boolean movePotions = AutomatedPotionMixingCategory.ACTIVE;

        List<MixerRecipe> result = new ArrayList<>();
        for (MixerRecipe r : all) {
            if (ieFilter.test(r) && !(movePotions && AutomatedPotionMixingCategory.isPotionMixer(r))) {
                result.add(r);
            }
        }
        return result;
    }

    @Redirect(
            method = "registerRecipes",
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lblusunrize/immersiveengineering/api/crafting/RefineryRecipe;"
                                   + "RECIPES:Lblusunrize/immersiveengineering/api/crafting/cache/CachedRecipeList;"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/common/util/compat/jei/JEIHelper;"
                           + "getFiltered(Lblusunrize/immersiveengineering/api/crafting/cache/CachedRecipeList;"
                           + "Ljava/util/function/Predicate;)Ljava/util/List;",
                    ordinal = 0
            )
    )
    private List<RefineryRecipe> creativeengineering$noLingeringInIeRefineryCategory(
            JEIHelper self, CachedRecipeList<RefineryRecipe> list, Predicate<RefineryRecipe> ieFilter) {

        Level level = Minecraft.getInstance().level;
        Collection<RefineryRecipe> all = level == null ? List.of() : list.getRecipes(level);
        boolean moveLingering = LingerificationCategory.ACTIVE;

        List<RefineryRecipe> result = new ArrayList<>();
        for (RefineryRecipe r : all) {
            if (ieFilter.test(r) && !(moveLingering && LingerificationCategory.isLingerification(r))) {
                result.add(r);
            }
        }
        return result;
    }
}
