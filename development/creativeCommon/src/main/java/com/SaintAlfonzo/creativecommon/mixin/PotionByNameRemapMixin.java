package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.swap.SwapLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Potion ids saved as NBT strings ({@code Potion:"creativebrewing:resistance"} on a potion, tipped arrow, potion
 * bucket or fluid) are read through {@code Potion.byName} (m_43489_), which the registry remap of {@link
 * com.SaintAlfonzo.creativecommon.swap.SwapRemapper} never sees. An unregistered name that a swap list renames is read
 * as its new id, so potions made before a rename (kubejs:, creativeengineering:, creativebrewing: to creativecommon:,
 * plan_cb_polish.md 2) keep their effect. The stack's NBT keeps the old string until something rewrites it; that is
 * harmless, every read goes through here.
 */
@Mixin(Potion.class)
public class PotionByNameRemapMixin {

	@ModifyVariable(method = "m_43489_(Ljava/lang/String;)Lnet/minecraft/world/item/alchemy/Potion;",
			at = @At("HEAD"), argsOnly = true, remap = false)
	private static String creativecommon$renamedPotion(String name) {
		if (name == null || name.isEmpty()) return name;
		ResourceLocation id = ResourceLocation.tryParse(name);
		if (id == null || ForgeRegistries.POTIONS.containsKey(id)) return name;
		ResourceLocation to = SwapLists.get().target(id);
		return to != null && ForgeRegistries.POTIONS.containsKey(to) ? to.toString() : name;
	}
}
