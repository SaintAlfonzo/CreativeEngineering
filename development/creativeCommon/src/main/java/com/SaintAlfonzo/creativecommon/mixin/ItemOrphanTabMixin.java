// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.OrphanTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives listed orphaned items a creative tab (jei/jei-coverage #29, {@link OrphanTabs}). {@code Item.getItemCategory()}
 * ({@code m_41471_}, final) is what {@code allowedIn}, Forge's {@code getCreativeTabs()} and so {@code fillItemCategory}
 * read, so a non-null answer here puts the item in that tab, the creative search and JEI's item list. Items with a
 * category of their own are untouched.
 */
@Mixin(value = Item.class, remap = false)
public abstract class ItemOrphanTabMixin {

	@Inject(method = "m_41471_", at = @At("RETURN"), cancellable = true)
	private void creativecommon$orphanTab(CallbackInfoReturnable<CreativeModeTab> cir) {
		if (cir.getReturnValue() != null) return;
		CreativeModeTab tab = OrphanTabs.tabFor((Item) (Object) this);
		if (tab != null) cir.setReturnValue(tab);
	}
}
