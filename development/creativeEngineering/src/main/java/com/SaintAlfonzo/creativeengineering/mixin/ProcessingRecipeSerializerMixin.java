package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.fan.ExclusiveResults;
import com.SaintAlfonzo.creativeengineering.fan.ResultPools;
import com.SaintAlfonzo.creativeengineering.fan.duck.ProcessingRecipeExt;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Parses the optional {@code "exclusive_results"} boolean and {@code "result_pools"} lists out of any Create
 * processing recipe JSON and stashes them on the deserialized recipe (via {@link ProcessingRecipeExt}).
 *
 * <p>The exclusive flag is server-only: results are rolled on the server, and JEI shows each output's chance, which
 * reads the same either way. The pools also travel in the recipe sync packet, because JEI draws one slot per pool
 * (plan_flesh_recipes.md 5.3). They go after Create's own data in the final {@code toNetwork} / {@code fromNetwork},
 * which subclasses cannot override, as a varint count (0 = none) and one varint array per pool. Client and server
 * both run this jar.
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
        if (recipe == null) return;
        ProcessingRecipeExt ext = (ProcessingRecipeExt) recipe;

        if (json.has(ResultPools.JSON_KEY)) {
            String[] problem = new String[1];
            int[][] pools = ResultPools.parse(json.get(ResultPools.JSON_KEY), recipe.getRollableResults().size(), problem);
            if (problem[0] != null)
                creativeengineering$LOGGER.error("[result-pools] {}: {}; pools ignored, every result rolls on its own",
                        recipeId, problem[0]);
            ext.creativeengineering$setResultPools(pools);
        }

        if (!GsonHelper.getAsBoolean(json, ExclusiveResults.JSON_KEY, false)) return;
        if (ext.creativeengineering$getResultPools() != null)
            creativeengineering$LOGGER.warn("[result-pools] {}: also sets {}; the pools are used", recipeId,
                    ExclusiveResults.JSON_KEY);
        ext.creativeengineering$setExclusiveResults(true);
        float sum = ExclusiveResults.chanceSum(recipe.getRollableResults());
        if (sum > 1.0001f)
            creativeengineering$LOGGER.warn("[exclusive-results] {}: output chances add up to {} (> 1); later outputs can never roll",
                    recipeId, sum);
    }

    @Inject(
            method = "toNetwork(Lnet/minecraft/network/FriendlyByteBuf;Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;)V",
            at = @At("TAIL")
    )
    private void creativeengineering$writeResultPools(FriendlyByteBuf buf, ProcessingRecipe<?> recipe, CallbackInfo ci) {
        ResultPools.write(buf, ((ProcessingRecipeExt) recipe).creativeengineering$getResultPools());
    }

    @Inject(
            method = "fromNetwork(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;",
            at = @At("RETURN")
    )
    private void creativeengineering$readResultPools(ResourceLocation recipeId, FriendlyByteBuf buf,
                                                     CallbackInfoReturnable<ProcessingRecipe<?>> cir) {
        int[][] pools = ResultPools.read(buf);
        ProcessingRecipe<?> recipe = cir.getReturnValue();
        if (recipe != null) ((ProcessingRecipeExt) recipe).creativeengineering$setResultPools(pools);
    }
}
