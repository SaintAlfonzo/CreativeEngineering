package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.compat.DrinkFluidEffects;
import mezz.jei.forge.platform.FluidHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * JEI's one fluid tooltip source ({@code FluidHelper.getTooltip}: name, then the id on advanced tooltips; JEI
 * adds the amount and mod name after). Inserts a drink fluid's effect lines under its name, as a potion item
 * shows them. See {@link DrinkFluidEffects}. The list is a fresh {@code ArrayList}, so it can be edited.
 */
@Mixin(value = FluidHelper.class, remap = false)
public class JeiFluidHelperTooltipMixin {

    @Inject(method = "getTooltip(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            remap = false, require = 1, at = @At("RETURN"))
    private void creativecommon$drinkEffects(FluidStack fs, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (tooltip.isEmpty()) return;
        tooltip.addAll(1, DrinkFluidEffects.lines(fs));
    }
}
