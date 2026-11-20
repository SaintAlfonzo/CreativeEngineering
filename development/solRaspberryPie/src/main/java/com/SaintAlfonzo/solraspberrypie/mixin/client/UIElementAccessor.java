package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Apple Pie's protected child list, so the diversity page's labels can be swapped for fitted ones (plan_diet_books_r2.md A3). */
@Mixin(value = UIElement.class, remap = false)
public interface UIElementAccessor {
	@Accessor("children")
	List<UIElement> solrp$children();
}
