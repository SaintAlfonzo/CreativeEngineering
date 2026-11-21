// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.mixin.sliceanddice;

import java.util.Set;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Any milk in Slice and Dice's mixer copies of Cooking Pot recipes (plan_wood_bucket_batch_1121.md R11). Slice and Dice
 * 2.4.0 turns a pot ingredient into a fluid through the first emptying recipe it matches, so a {@code #forge:milk} slot
 * became 250 mB of cow milk only ({@code FluidIngredient.fromFluidStack} in {@code LazyMixingRecipe.resolve()}, run lazily
 * once tags are bound). A milk stack (in the {@code forge:milk} fluid tag, or cow, goat or nut milk by id) becomes a
 * {@code forge:milk} tag ingredient of the same amount; anything else, or milk with NBT, is left as it was.
 */
@Pseudo
@Mixin(targets = {"com.possible_triangle.sliceanddice.compat.LazyMixingRecipe"}, remap = false)
public abstract class SliceAndDiceMilkTagMixin {

	private static final TagKey<Fluid> CREATIVEDELIGHT$MILK = FluidTags.create(new ResourceLocation("forge", "milk"));
	private static final Set<String> CREATIVEDELIGHT$MILK_IDS = Set.of("minecraft:milk", "minecraft:flowing_milk",
			"hearthandharvest:goat_milk", "creativedelight:nut_milk");

	@Redirect(method = "resolve", remap = false, require = 0,
			at = @At(value = "INVOKE", remap = false,
					target = "Lcom/simibubi/create/foundation/fluid/FluidIngredient;fromFluidStack(Lnet/minecraftforge/fluids/FluidStack;)Lcom/simibubi/create/foundation/fluid/FluidIngredient;"))
	private FluidIngredient creativedelight$anyMilk(FluidStack stack) {
		if (!stack.isEmpty() && !stack.hasTag()) {
			Fluid fluid = stack.getFluid();
			ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
			if (fluid.is(CREATIVEDELIGHT$MILK) || (id != null && CREATIVEDELIGHT$MILK_IDS.contains(id.toString()))) {
				return FluidIngredient.fromTag(CREATIVEDELIGHT$MILK, stack.getAmount());
			}
		}
		return FluidIngredient.fromFluidStack(stack);
	}
}
