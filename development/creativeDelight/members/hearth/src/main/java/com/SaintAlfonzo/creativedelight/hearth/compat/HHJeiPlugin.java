package com.SaintAlfonzo.creativedelight.hearth.compat;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.item.HHItems;
import com.SaintAlfonzo.creativedelight.hearth.item.WateringCanItem;
import com.SaintAlfonzo.creativedelight.hearth.item.WateringCanTier;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

/** JEI info pages of the ported Hearth and Harvest content: each watering can, with its tier's numbers. */
@JeiPlugin
public class HHJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(HearthAndHarvestMod.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(mezz.jei.api.registration.IRecipeCategoryRegistration registration) {
        com.SaintAlfonzo.creativedelight.hearth.machine.client.MachineJei.categories(registration);
    }

    @Override
    public void registerRecipeCatalysts(mezz.jei.api.registration.IRecipeCatalystRegistration registration) {
        com.SaintAlfonzo.creativedelight.hearth.machine.client.MachineJei.catalysts(registration);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        com.SaintAlfonzo.creativedelight.hearth.machine.client.MachineJei.recipes(registration);
        for (RegistryObject<Item> can : HHItems.ITEMS.getEntries()) {
            if (!(can.get() instanceof WateringCanItem item)) continue;
            WateringCanTier t = item.getTier();
            registration.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK,
                    Component.translatable("hearthandharvest.jei.info.watering_can",
                            t.capacity, t.size, t.size, t.cauldronLevels));
        }
    }
}
