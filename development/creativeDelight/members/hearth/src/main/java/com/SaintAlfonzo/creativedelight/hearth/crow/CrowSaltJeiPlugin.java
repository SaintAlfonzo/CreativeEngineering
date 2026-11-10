package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.nest.HHNestRegistry;
import com.SaintAlfonzo.creativedelight.hearth.salt.HHSaltRegistry;
import com.SaintAlfonzo.creativedelight.hearth.salt.SaltLick;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** JEI info pages for the salt lick, the scarecrow and the nest. */
@JeiPlugin
public class CrowSaltJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(HearthAndHarvestMod.MODID, "jei_crow_salt");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> licks = new ArrayList<>();
        licks.add(new ItemStack(SaltLick.saltBlock()));
        licks.add(new ItemStack(HHSaltRegistry.LIGHTLY_LICKED.get()));
        licks.add(new ItemStack(HHSaltRegistry.WELL_LICKED.get()));
        licks.add(new ItemStack(HHSaltRegistry.HEAVILY_LICKED.get()));
        registration.addIngredientInfo(licks, VanillaTypes.ITEM_STACK, Component.translatable(HearthAndHarvestMod.MODID + ".jei.info.salt_block"));
        registration.addIngredientInfo(new ItemStack(HHCrowRegistry.SCARECROW_ITEM.get()), VanillaTypes.ITEM_STACK, Component.translatable(HearthAndHarvestMod.MODID + ".jei.info.scarecrow"));
        registration.addIngredientInfo(new ItemStack(HHNestRegistry.NEST_ITEM.get()), VanillaTypes.ITEM_STACK, Component.translatable(HearthAndHarvestMod.MODID + ".jei.info.nest"));
    }
}
