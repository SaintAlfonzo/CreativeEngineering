package com.SaintAlfonzo.solraspberrypie.mixin;

import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.tarinoita.solsweetpotato.tracking.FoodTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Apple Pie's single entry for eaten foods (items and cake bites): every rung is re-evaluated after it (8). */
@Mixin(value = FoodTracker.class, remap = false)
public abstract class FoodTrackerMixin {
    @Inject(method = "updateFoodList", at = @At("RETURN"))
    private static void solrp$afterEat(Item item, Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer sp) DietEngine.onFoodEaten(sp, item);
    }
}
