package com.SaintAlfonzo.creativecompat.corpse;

import com.hidoni.customizableelytra.items.CustomizableElytraItem;
import com.hidoni.customizableelytra.renderers.CustomizableElytraLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Customizable Elytra side of {@link CorpseSkeletonLayer}: its own elytra layer draws a customizable elytra on a corpse
 * skeleton with its dye colors, banner patterns and split wings, as on the player (plan_corpses.md 3.6, ruling 37).
 * The layer finds the elytra in the chest slot, where the skeleton layer puts it for the draw. Only touched
 * when Customizable Elytra is loaded.
 */
final class CorpseCustomElytra {

	private CorpseCustomElytra() {}

	static <T extends LivingEntity, M extends EntityModel<T>> ElytraLayer<T, M> layer(RenderLayerParent<T, M> parent,
			EntityModelSet models) {
		return new CustomizableElytraLayer<>(parent, models);
	}

	static boolean is(ItemStack stack) {
		return stack.getItem() instanceof CustomizableElytraItem;
	}
}
