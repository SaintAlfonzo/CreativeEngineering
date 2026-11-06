package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.crafting.serializers.SawmillRecipeSerializer;
import com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillRecipeExt;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Parses the optional {@code "blade"} string field out of {@code immersiveengineering:sawmill}
 * recipe JSON and stashes it on the deserialized {@link SawmillRecipe} (via
 * {@link SawmillRecipeExt}). Absent / empty → the profile-less default recipe for that input.
 *
 * <p>Only the JSON read path is touched — the network path is deliberately left alone; the
 * client re-derives the chosen recipe from the id persisted on the {@code SawmillProcess}.
 */
@Mixin(value = SawmillRecipeSerializer.class, remap = false)
public class SawmillRecipeSerializerMixin {

    @Inject(
            method = "readFromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lblusunrize/immersiveengineering/api/crafting/SawmillRecipe;",
            at = @At("RETURN")
    )
    private void creativeengineering$readBladeProfile(ResourceLocation recipeId, JsonObject json,
                                                     ICondition.IContext context,
                                                     CallbackInfoReturnable<SawmillRecipe> cir) {
        SawmillRecipe recipe = cir.getReturnValue();
        if (recipe == null) return;
        ((SawmillRecipeExt) recipe).creativeengineering$setBlade(GsonHelper.getAsString(json, "blade", ""));
    }
}
