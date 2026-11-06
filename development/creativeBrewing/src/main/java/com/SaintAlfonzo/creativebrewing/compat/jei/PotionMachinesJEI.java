package com.SaintAlfonzo.creativebrewing.compat.jei;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * The IE potion machine pages: Automated Potion Mixing (IE Mixer potion brews) and Lingerification (IE Refinery
 * lingering conversions). Registered by patcherMod's DragonBreathingJEI until 2026-11-01, when they moved here with the
 * potion code (creative_brewing.md 4.2 phase 2). mixin/immersiveengineering/JEIHelperMixin keeps these recipes out of
 * IE's own Mixer and Refinery pages while {@link AutomatedPotionMixingCategory#ACTIVE} /
 * {@link LingerificationCategory#ACTIVE} are set.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class PotionMachinesJEI implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(CreativeBrewing.MOD_ID, "potion_machines");

    private AutomatedPotionMixingCategory potionMixing;
    private LingerificationCategory lingerification;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (!ModList.get().isLoaded("immersiveengineering")) return;
        try {
            potionMixing = new AutomatedPotionMixingCategory(registration.getJeiHelpers().getGuiHelper());
            registration.addRecipeCategories(potionMixing);
            AutomatedPotionMixingCategory.ACTIVE = true;
        } catch (Throwable t) {
            potionMixing = null;
            AutomatedPotionMixingCategory.ACTIVE = false;
            CreativeBrewing.LOGGER.error("[creativebrewing] Automated Potion Mixing JEI category disabled - potion "
                    + "recipes stay in the IE Mixer category", t);
        }

        try {
            lingerification = new LingerificationCategory(registration.getJeiHelpers().getGuiHelper());
            registration.addRecipeCategories(lingerification);
            LingerificationCategory.ACTIVE = true;
        } catch (Throwable t) {
            lingerification = null;
            LingerificationCategory.ACTIVE = false;
            CreativeBrewing.LOGGER.error("[creativebrewing] Lingerification JEI category disabled - refinery lingering "
                    + "recipes stay in the IE Refinery category", t);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        net.minecraft.world.level.Level level = Minecraft.getInstance().level;
        if (level == null) return;
        if (potionMixing != null) {
            List<MixerRecipe> potions = MixerRecipe.RECIPES.getRecipes(level).stream()
                    .filter(AutomatedPotionMixingCategory::isPotionMixer)
                    .toList();
            registration.addRecipes(AutomatedPotionMixingCategory.TYPE, potions);
        }
        if (lingerification != null) {
            List<RefineryRecipe> lingering = RefineryRecipe.RECIPES.getRecipes(level).stream()
                    .filter(LingerificationCategory::isLingerification)
                    .toList();
            registration.addRecipes(LingerificationCategory.TYPE, lingering);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (potionMixing != null) {
            registration.addRecipeCatalyst(new ItemStack(IEBlocks.Multiblocks.MIXER), AutomatedPotionMixingCategory.TYPE);
        }
        if (lingerification != null) {
            registration.addRecipeCatalyst(new ItemStack(IEBlocks.Multiblocks.REFINERY), LingerificationCategory.TYPE);
        }
    }
}
