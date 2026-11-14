package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Apple Pie's page list, food data and book background. Pages are plain UIElements, so Raspberry Pie can insert its own. */
@Mixin(value = FoodBookScreen.class, remap = false)
public interface FoodBookScreenAccessor {
    @SuppressWarnings("rawtypes")
    @Accessor("pages")
    List solrp$pages();

    @Accessor("foodData")
    Set<Map.Entry<FoodInstance, Integer>> solrp$foodData();

    @Accessor("background")
    UIImage solrp$background();
}
