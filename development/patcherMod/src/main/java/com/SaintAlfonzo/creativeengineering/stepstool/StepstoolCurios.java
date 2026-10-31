package com.SaintAlfonzo.creativeengineering.stepstool;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** The Curios half of {@link Stepstool}: only loaded when Curios is. */
public final class StepstoolCurios {

    private StepstoolCurios() {
    }

    /** The first stepstool the entity wears in any Curios slot (the tag puts them in feet), or EMPTY. */
    public static ItemStack find(LivingEntity entity) {
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, stack -> stack.is(Stepstool.TAG))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}
