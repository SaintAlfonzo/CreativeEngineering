package com.SaintAlfonzo.creativedelight.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntitySeagull;
import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Seagulls never go for a dropped lunchbox (Spice of Life food containers count as edible, so the
 * dropped-item goal would otherwise pick them up). Inventory stealing: {@link AlexsMobsSeagullStealMixin}.
 */
@Mixin(value = EntitySeagull.class, remap = false)
public abstract class AlexsMobsSeagullItemMixin {

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$skipLunchboxes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof FoodContainerItem) {
            cir.setReturnValue(false);
        }
    }
}
