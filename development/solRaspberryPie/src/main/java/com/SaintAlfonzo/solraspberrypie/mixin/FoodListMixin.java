package com.SaintAlfonzo.solraspberrypie.mixin;

import com.SaintAlfonzo.solraspberrypie.logic.Drinks;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drinks leave the food diet (7): with shouldForbiddenCount on, Apple Pie would still age every remembered food and
 * bump foodsEaten for a blacklisted drink. Returning early keeps the food memory and the counter untouched.
 */
@Mixin(value = FoodList.class, remap = false)
public abstract class FoodListMixin {
    @Inject(method = "addFood(Lnet/minecraft/world/item/Item;)V", at = @At("HEAD"), cancellable = true)
    private void solrp$skipDrinks(Item item, CallbackInfo ci) {
        if (Drinks.isDrink(item)) ci.cancel();
    }
}
