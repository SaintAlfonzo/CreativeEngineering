package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.cooking.CookwareContents;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A cooking pot, copper pot or kettle item that holds contents shows the enchantment glint
 * ({@link CookwareContents#holdsContents}, kitchen.md 2.12, TODO cooking/kitchen #40).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackCookwareGlintMixin {

    @Inject(method = "m_41790_", at = @At("RETURN"), cancellable = true, remap = false)
    private void ce$cookwareGlint(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CookwareContents.holdsContents((ItemStack) (Object) this)) cir.setReturnValue(true);
    }
}
