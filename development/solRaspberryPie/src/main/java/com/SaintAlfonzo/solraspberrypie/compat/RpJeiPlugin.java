package com.SaintAlfonzo.solraspberrypie.compat;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.client.InventoryWidget;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI moves its overlay out of the inventory widget's way (11.1), and does not list the diet marker (the internal
 * curative-item marker on diet effects, 9.1; never a real item for players).
 */
@JeiPlugin
public class RpJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(SolRaspberryPie.MOD_ID, "jei");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(InventoryScreen.class, new IGuiContainerHandler<InventoryScreen>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(InventoryScreen screen) {
				Rect2i r = InventoryWidget.lastRect();
				return r == null ? List.of() : List.of(r);
			}
		});
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(new ItemStack(SolRaspberryPie.MARKER.get())));
	}
}
