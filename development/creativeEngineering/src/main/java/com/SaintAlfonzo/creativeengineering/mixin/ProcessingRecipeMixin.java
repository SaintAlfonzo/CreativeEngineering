package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.List;
import java.util.function.Supplier;

import com.SaintAlfonzo.creativeengineering.fan.ExclusiveResults;
import com.SaintAlfonzo.creativeengineering.fan.duck.ProcessingRecipeExt;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Carries the {@code "exclusive_results"} flag (parsed by {@link ProcessingRecipeSerializerMixin})
 * and, when it is set, replaces Create's per-output chance rolls with one weighted pick
 * ({@link ExclusiveResults#roll}). {@code rollResults} is called once per processed item by
 * {@code RecipeApplier} (fans, D&D sanding) and the basin / millstone / crusher paths alike.
 *
 * <p>Sequenced assembly's forced result keeps Create's path.
 */
@Mixin(value = ProcessingRecipe.class, remap = false)
public abstract class ProcessingRecipeMixin implements ProcessingRecipeExt {

    @Shadow
    private Supplier<ItemStack> forcedResult;

    @Unique
    private boolean creativeengineering$exclusiveResults;

    @Override
    public boolean creativeengineering$isExclusiveResults() {
        return creativeengineering$exclusiveResults;
    }

    @Override
    public void creativeengineering$setExclusiveResults(boolean exclusive) {
        this.creativeengineering$exclusiveResults = exclusive;
    }

    @Inject(method = "rollResults(Ljava/util/List;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void creativeengineering$rollExclusive(List<ProcessingOutput> rollableResults,
                                                   CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!creativeengineering$exclusiveResults || forcedResult != null) return;
        cir.setReturnValue(ExclusiveResults.roll(rollableResults));
    }
}
