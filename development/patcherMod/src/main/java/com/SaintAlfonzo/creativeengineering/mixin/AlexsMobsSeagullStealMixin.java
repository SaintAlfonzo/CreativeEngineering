package com.SaintAlfonzo.creativeengineering.mixin;

import com.github.alexthe666.alexsmobs.entity.ai.SeagullAIStealFromPlayers;
import com.tarinoita.solsweetpotato.item.foodcontainer.FoodContainerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Seagulls never steal a lunchbox from a player's inventory: every Spice of Life food container counts as
 * blacklisted, whatever {@code seagullStealingBlacklist} in config/alexsmobs.toml says. Both the "has food"
 * check and the pick of what to steal go through {@code isBlacklisted}. Dropped boxes:
 * {@link AlexsMobsSeagullItemMixin}.
 */
@Mixin(value = SeagullAIStealFromPlayers.class, remap = false)
public abstract class AlexsMobsSeagullStealMixin {

    @Inject(method = "isBlacklisted", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$skipLunchboxes(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof FoodContainerItem) {
            cir.setReturnValue(true);
        }
    }
}
