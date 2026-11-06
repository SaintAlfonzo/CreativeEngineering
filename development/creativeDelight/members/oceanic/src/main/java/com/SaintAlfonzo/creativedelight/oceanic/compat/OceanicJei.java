package com.SaintAlfonzo.creativedelight.oceanic.compat;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** The information pages upstream had in JEI (its lang holds the texts; the pack rewrites two of them). */
@JeiPlugin
public class OceanicJei implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return OceanicDelightMod.path("information");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        info(registration, "salmon_eggs_information", OceanicItems.SALMON_EGGS);
        info(registration, "elder_eye_information", OceanicItems.ELDER_GUARDIAN_EYE);
        info(registration, "squid_tentacles_information", OceanicItems.SQUID_TENTACLES);
        info(registration, "glow_squid_tentacles_information", OceanicItems.GLOW_SQUID_TENTACLES);
        info(registration, "sea_grape_information", OceanicItems.SEA_GRAPE, OceanicItems.WILD_SEA_GRAPE);
        info(registration, "shrimp_information", OceanicItems.SHRIMP, OceanicItems.FISHING_NET);
    }

    @SafeVarargs
    private static void info(IRecipeRegistration registration, String key, RegistryObject<? extends net.minecraft.world.item.Item>... items) {
        List<ItemStack> stacks = java.util.Arrays.stream(items).map(i -> new ItemStack(i.get())).toList();
        registration.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, Component.translatable("jei.oceanic_delight." + key));
    }
}
