package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.food.FlavoredItemModel;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Flavored food icons ({@link FlavoredItemModel}): the resolved item model gets the stack's flavor overlays.
 * Vanilla-class target by SRG name ({@code m_174264_} = {@code getModel(ItemStack, Level, LivingEntity, int)}) with
 * {@code remap = false}, as {@link ToastFilterMixin}.
 */
@Mixin(value = ItemRenderer.class, remap = false)
public class FlavoredItemModelMixin {

	@Inject(method = "m_174264_", at = @At("RETURN"), cancellable = true)
	private void creativeengineering$flavoredOverlay(ItemStack stack, Level level, LivingEntity entity, int seed,
			CallbackInfoReturnable<BakedModel> cir) {
		BakedModel base = cir.getReturnValue();
		BakedModel flavored = FlavoredItemModel.resolve(stack, base);
		if (flavored != base) {
			cir.setReturnValue(flavored);
		}
	}
}
