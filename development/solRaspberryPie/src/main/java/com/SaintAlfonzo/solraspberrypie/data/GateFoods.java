package com.SaintAlfonzo.solraspberrypie.data;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * The activation gate's counter (12.4, #36). Apple Pie counts every food eaten since its counter last reset; with
 * {@code uniqueFoodsToActivate} on, only different foods count. The set lives on Apple Pie's FoodList (mixin, saved in
 * its NBT) so it resets exactly when Apple Pie's own counter resets.
 */
public interface GateFoods {
    Set<ResourceLocation> solrp$gateFoods();

    /** The number the gate compares with Apple Pie's minFoodsToActivate. */
    static int count(FoodList list) {
        if (RpConfig.SERVER_SPEC.isLoaded() && RpConfig.SERVER.uniqueFoodsToActivate.get()) {
            return ((GateFoods) (Object) list).solrp$gateFoods().size();
        }
        return list.getFoodsEaten();
    }
}
