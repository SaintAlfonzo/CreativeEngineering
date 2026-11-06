package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.fan.ExclusiveResults;
import com.SaintAlfonzo.creativeengineering.fan.duck.ProcessingRecipeExt;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Parses the optional {@code "exclusive_results"} boolean out of any Create processing recipe JSON
 * and stashes it on the deserialized recipe (via {@link ProcessingRecipeExt}).
 *
 * <p>Only the JSON read path is touched: results are rolled on the server, and the client (JEI)
 * just shows each output's chance, which reads the same either way.
 */
@Mixin(value = ProcessingRecipeSerializer.class, remap = false)
public class ProcessingRecipeSerializerMixin {

    @Unique
    private static final Logger creativeengineering$LOGGER = LogUtils.getLogger();

    @Inject(
            method = "readFromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;",
            at = @At("RETURN")
    )
    private void creativeengineering$readExclusiveResults(ResourceLocation recipeId, JsonObject json,
                                                          CallbackInfoReturnable<ProcessingRecipe<?>> cir) {
        ProcessingRecipe<?> recipe = cir.getReturnValue();
        if (recipe == null || !GsonHelper.getAsBoolean(json, ExclusiveResults.JSON_KEY, false)) return;
        ((ProcessingRecipeExt) recipe).creativeengineering$setExclusiveResults(true);
        float sum = ExclusiveResults.chanceSum(recipe.getRollableResults());
        if (sum > 1.0001f)
            creativeengineering$LOGGER.warn("[exclusive-results] {}: output chances add up to {} (> 1); later outputs can never roll",
                    recipeId, sum);
    }
}
