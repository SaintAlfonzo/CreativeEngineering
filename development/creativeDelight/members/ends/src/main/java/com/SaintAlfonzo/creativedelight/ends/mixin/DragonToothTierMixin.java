package com.SaintAlfonzo.creativedelight.ends.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * End's Delight 2.1's Dragon Tooth tier (the anonymous {@code MaterialRegistry$4}, the fourth tier in the class:
 * egg shell, end stone, purpur, dragon tooth) repairs with a Bone Block, which looks like an upstream slip; 2.6.1
 * repairs it with a Dragon Tooth. This answers the tier's repair ingredient with {@code ends_delight:dragon_tooth}
 * (ends_delight.md 3.3, TODO largecontent/mod-additions #29). The tier's other values stay 2.1's; the 2.6.1 speed,
 * level and enchantability raises wait for the End gear review (gear #12).
 *
 * Creative Delight overlay of the End's Delight repack. The tier is created in a static initializer, before the
 * item registry is filled, so the tooth is looked up on each call (anvils ask rarely).
 */
@Mixin(targets = "cn.foggyhillside.endsdelight.registry.MaterialRegistry$4", remap = false)
public class DragonToothTierMixin {

	@Inject(method = "m_6282_", remap = false, at = @At("HEAD"), cancellable = true, require = 1)
	private void creativedelight$toothRepair(CallbackInfoReturnable<Ingredient> cir) {
		Item tooth = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ends_delight", "dragon_tooth"));
		if (tooth != null) cir.setReturnValue(Ingredient.of(tooth));
	}
}
