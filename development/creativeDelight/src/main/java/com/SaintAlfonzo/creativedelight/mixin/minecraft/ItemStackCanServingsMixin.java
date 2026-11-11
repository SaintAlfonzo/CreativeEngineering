package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.food.CanServings;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Multi-serving cans ({@link CanServings}): eating takes one serving, and a partly eaten can
 * draws a durability-style bar. On ItemStack rather than the Canned Goods item classes so any
 * item tagged {@code #creativedelight:cans/servings/<n>} works, whatever mod it is from.
 *
 * <p>SRG names with {@code remap = false} (no refmap in this build): m_41671_ finishUsingItem,
 * m_150947_ isBarVisible, m_150948_ getBarWidth, m_150949_ getBarColor.
 *
 * <p>Limit: an auto-feeder that calls {@code Item#finishUsingItem} directly (not through the
 * stack) skips this and eats the whole can as one serving.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackCanServingsMixin {

    @Inject(method = "m_41671_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$eatServing(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        boolean can;
        try {
            can = entity instanceof Player && CanServings.isCan(self);
        } catch (Throwable t) {
            return; // plain Canned Goods behaviour
        }
        if (can) cir.setReturnValue(CanServings.finishServing(self, level, (Player) entity));
    }

    @Inject(method = "m_150947_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$servingsBarVisible(CallbackInfoReturnable<Boolean> cir) {
        if (CanServings.isPartial((ItemStack) (Object) this)) cir.setReturnValue(true);
    }

    @Inject(method = "m_150948_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$servingsBarWidth(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (CanServings.isPartial(self)) cir.setReturnValue(CanServings.barWidth(self));
    }

    @Inject(method = "m_150949_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$servingsBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (CanServings.isPartial(self)) cir.setReturnValue(CanServings.barColor(self));
    }
}
