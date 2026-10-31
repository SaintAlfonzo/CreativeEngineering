package com.SaintAlfonzo.creativeengineering.wateringcan;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** H&H's JEI info page for the Watering Can (lang key kept from upstream). */
@JeiPlugin
public class WateringCanJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(CreativeEngineering.MODID, "watering_can");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(new ItemStack(WateringCanItems.WATERING_CAN.get()), VanillaTypes.ITEM_STACK,
                Component.translatable("hearthandharvest.jei.info.watering_can"));
    }
}
