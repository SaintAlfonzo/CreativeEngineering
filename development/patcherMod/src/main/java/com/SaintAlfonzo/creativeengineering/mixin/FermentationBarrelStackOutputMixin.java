package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import satisfyu.vinery.block.entity.FermentationBarrelBlockEntity;
import satisfyu.vinery.util.WineYears;

/**
 * Lets the Vinery Aging Barrel stack its output. Stock, {@code canCraft} needs slot 5 empty, so the
 * barrel makes one wine and stalls until someone empties the slot. Now it keeps crafting while the
 * output holds the same wine (same item and same {@code Year} tag) and has room.
 *
 * Two injections, both reusing Vinery's own code for the rest of the craft:
 *  - {@code canCraft}: replaces only the "output empty" test with "empty, or stackable with the result".
 *  - {@code craft}: when slot 5 is already occupied, grows it before Vinery's code runs. Vinery then
 *    skips its own "create output" branch (slot is non-empty) and consumes bottle + ingredients as usual.
 *
 * A different-year wine in the slot still blocks the barrel, because the two would not stack.
 */
@Mixin(value = FermentationBarrelBlockEntity.class, remap = false)
public abstract class FermentationBarrelStackOutputMixin {

	private static final int OUTPUT_SLOT = 5;

	@Shadow
	private boolean canCraft(Recipe<?> recipe) {
		throw new AssertionError();
	}

	@Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
	private void ce$canCraftStacking(Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
		Container self = (Container) (Object) this;
		if (recipe == null || recipe.getResultItem().isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		boolean inputs = false;
		for (int i = 1; i < 5; i++) {
			if (!self.getItem(i).isEmpty()) {
				inputs = true;
				break;
			}
		}
		ItemStack bottle = self.getItem(0);
		Item wineBottle = ForgeRegistries.ITEMS.getValue(new ResourceLocation("vinery", "wine_bottle"));
		if (!inputs || bottle.isEmpty() || bottle.getItem() != wineBottle) {
			cir.setReturnValue(false);
			return;
		}
		cir.setReturnValue(ce$outputAccepts(self.getItem(OUTPUT_SLOT), recipe));
	}

	@Inject(method = "craft", at = @At("HEAD"))
	private void ce$growOutput(Recipe<?> recipe, CallbackInfo ci) {
		Container self = (Container) (Object) this;
		ItemStack out = self.getItem(OUTPUT_SLOT);
		if (!out.isEmpty() && canCraft(recipe)) {
			out.grow(recipe.getResultItem().getCount());
		}
	}

	private boolean ce$outputAccepts(ItemStack out, Recipe<?> recipe) {
		if (out.isEmpty()) {
			return true;
		}
		ItemStack result = recipe.getResultItem();
		BlockEntity be = (BlockEntity) (Object) this;
		WineYears.setWineYear(result, be.getLevel());
		return out.sameItem(result)
				&& ItemStack.tagMatches(out, result)
				&& out.getCount() + result.getCount() <= Math.min(out.getMaxStackSize(), ((Container) (Object) this).getMaxStackSize());
	}
}
