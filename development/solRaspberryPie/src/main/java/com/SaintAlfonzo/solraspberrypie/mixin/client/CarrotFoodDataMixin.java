package com.SaintAlfonzo.solraspberrypie.mixin.client;

import com.SaintAlfonzo.solraspberrypie.client.FoodDictionarySort;
import com.cazsius.solcarrot.tracking.FoodList;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * The Food Dictionary's eaten and uneaten lists in Raspberry Pie's orders (plan_diet_books.md 2.2, 2.3;
 * {@link FoodDictionarySort}). Carrot builds both from one name-sorted list each time its book opens. Gated on
 * solcarrot (RpMixinPlugin).
 */
@Mixin(targets = "com.cazsius.solcarrot.client.gui.FoodData", remap = false)
public abstract class CarrotFoodDataMixin {
	@Shadow
	@Final
	public FoodList foodList;

	@Shadow
	@Final
	public List<Item> eatenFoods;

	@Shadow
	@Final
	public List<Item> uneatenFoods;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void solrp$sort(FoodList list, CallbackInfo ci) {
		uneatenFoods.sort(FoodDictionarySort.uneaten());
		eatenFoods.sort(FoodDictionarySort.eaten(list));
	}
}
