package com.SaintAlfonzo.solraspberrypie.mixin;

import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** Apple Pie's private food memory: food -> meals since last eaten (6.2). */
@Mixin(value = FoodList.class, remap = false)
public interface FoodListAccessor {
    @Accessor("uniqueFoods")
    Map<FoodInstance, Integer> solrp$uniqueFoods();
}
