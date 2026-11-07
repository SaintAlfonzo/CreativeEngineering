package com.SaintAlfonzo.creativedelight.mixin.upgrade_aquatic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Jams are mason jars ({@code dishes/jams #1}, decided 2026-11-02): Upgrade Aquatic 5.1.0's {@code MulberryJamBottleItem}
 * hands back a hard-coded glass bottle when eaten ({@code finishUsingItem}, two {@code Items.GLASS_BOTTLE} reads: the
 * last-item case and the add-to-inventory case). Both now read Vintage Delight's empty mason jar, falling back to the
 * glass bottle when that item is not registered. The crafting remainder and the recipes are KubeJS (CD startup
 * vintagedelight/vintage_items.js, server vintagedelight/jams.js).
 */
@Pseudo
@Mixin(targets = "com.teamabnormals.upgrade_aquatic.common.item.MulberryJamBottleItem", remap = false)
public abstract class MulberryJamMasonJarMixin {

	private static final ResourceLocation CD_EMPTY_MASON_JAR = new ResourceLocation("vintagedelight", "empty_mason_jar");

	@Redirect(method = "m_5922_", remap = false, require = 1, allow = 2,
			at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.GETSTATIC, remap = false,
					target = "Lnet/minecraft/world/item/Items;f_42590_:Lnet/minecraft/world/item/Item;"))
	private Item ce$masonJar() {
		Item jar = ForgeRegistries.ITEMS.getValue(CD_EMPTY_MASON_JAR);
		return jar == null || jar == Items.AIR ? Items.GLASS_BOTTLE : jar;
	}
}
