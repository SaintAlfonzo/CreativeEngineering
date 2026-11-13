package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Apple Pie's page list. Pages are drawn as plain UIElements, so Raspberry Pie's own pages can be inserted. */
@Mixin(value = FoodBookScreen.class, remap = false)
public interface FoodBookScreenAccessor {
    @SuppressWarnings("rawtypes")
    @Accessor("pages")
    List solrp$pages();
}
