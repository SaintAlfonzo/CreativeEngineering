package com.SaintAlfonzo.creativeengineering.mixin;

import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises Create's output limit for crushing (7) and milling (4) recipes to 24 (plan_flesh_recipes.md 5.3): a flesh
 * crush lists 17 items across its result pools. Create only warns on the limit in {@code validate}; this keeps the
 * log clean. The crushing wheel holds its results in a {@code ProcessingInventory}, grown to match by
 * {@link ProcessingInventoryMixin}; the millstone's 9 output slots fit what one item rolls.
 */
@Mixin(value = {CrushingRecipe.class, MillingRecipe.class}, remap = false)
public class CrushingOutputCapMixin {

    private static final int creativeengineering$MAX_OUTPUTS = 24;

    @Inject(method = "getMaxOutputCount()I", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$raiseOutputCap(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(creativeengineering$MAX_OUTPUTS);
    }
}
