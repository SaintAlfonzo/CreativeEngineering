package com.SaintAlfonzo.creativeengineering.mixin;

import net.blay09.mods.cookingforblockheads.tile.OvenBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cooking for Blockheads oven (13.3.4) smokes as well as smelts (TODO cooking/oven #4). CfB's
 * {@code getSmeltingResult} checks its own registry, then {@code RecipeType.SMELTING} (food results only, or CfB's
 * non-food list). When that finds nothing, this tries {@code RecipeType.SMOKING} with the same rule, so smoker-only
 * food cooks too. The insert filter calls the same method, so such food is also accepted into the input slots.
 */
@Mixin(value = OvenBlockEntity.class, remap = false)
public abstract class CfbOvenSmokingMixin {

    @Shadow
    @Final
    private Container singleSlotRecipeWrapper;

    @Inject(method = "getSmeltingResult", at = @At("RETURN"), cancellable = true)
    private void ce$smokingFallback(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty() || stack.isEmpty()) {
            return;
        }
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level == null) {
            return;
        }
        singleSlotRecipeWrapper.setItem(0, stack);
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.SMOKING, singleSlotRecipeWrapper, level)
                .map(r -> r.getResultItem()).orElse(ItemStack.EMPTY);
        if (!result.isEmpty() && result.getItem().isEdible()) {
            cir.setReturnValue(result);
        }
    }
}
