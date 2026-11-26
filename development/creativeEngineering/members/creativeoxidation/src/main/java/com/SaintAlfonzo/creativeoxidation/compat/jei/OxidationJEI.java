// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeoxidation.compat.jei;

import com.SaintAlfonzo.creativeoxidation.CreativeOxidation;
import com.SaintAlfonzo.creativeoxidation.OxidationCreativeTab;
import com.SaintAlfonzo.creativeoxidation.OxidationItemData;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

/**
 * JEI subtypes for the staged copper items (P8, R16). Without an interpreter JEI merges stacks that differ only in NBT,
 * so the 7 variants per target item (OxidationCreativeTab) would show as one. The subtype is the stage and wax ("exposed",
 * "waxed_unaffected", ...); the plain item has none, so it stays the item JEI already knows. Same subtype in the
 * ingredient and recipe contexts: a staged item looks up the spout and fan recipes that make or take that stage.
 * Loaded by JEI only (optional dependency).
 */
@JeiPlugin
public class OxidationJEI implements IModPlugin {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation ID = new ResourceLocation(CreativeOxidation.MOD_ID, "subtypes");

	/** Stage and wax as a subtype string; NONE for a plain stack. */
	public static final IIngredientSubtypeInterpreter<ItemStack> INTERPRETER = (ItemStack stack, UidContext context) ->
			OxidationItemData.read(stack)
					.map(s -> (s.waxed() ? "waxed_" : "") + s.stage().getSerializedName())
					.orElse(IIngredientSubtypeInterpreter.NONE);

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		int n = 0;
		for (Item item : OxidationCreativeTab.targetItems()) {
			try {
				registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, INTERPRETER);
				n++;
			} catch (RuntimeException e) {
				LOGGER.warn("[creativeoxidation] JEI subtype for {} not registered: {}", item, e.toString());
			}
		}
		LOGGER.info("[creativeoxidation] JEI subtypes on {} items", n);
	}
}
