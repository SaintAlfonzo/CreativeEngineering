package com.SaintAlfonzo.creativedelight.hearth.mixin;

import com.SaintAlfonzo.creativedelight.hearth.crow.CrowEatDroppedFoodGoal;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps AM's item targeting off crow food for wild crows, so the eat-from-the-ground goal (and its taming) gets it. Without
 * this AM always reaches the item first. Wheat seeds are left to AM's own thrown-seed taming. AM's own method: no remap.
 */
@Mixin(value = EntityCrow.class, remap = false)
public abstract class EntityCrowFoodMixin {
    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void hearthandharvest$leaveCrowFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!((TamableAnimal) (Object) this).isTame() && CrowEatDroppedFoodGoal.eatsFood(stack)) {
            cir.setReturnValue(false);
        }
    }
}
